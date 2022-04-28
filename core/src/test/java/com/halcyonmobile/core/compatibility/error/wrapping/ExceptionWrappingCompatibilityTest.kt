package com.halcyonmobile.core.compatibility.error.wrapping

import com.halcyonmobile.errorparsing2.ErrorWrappingAndParserCallAdapterFactory
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthgson.OauthRetrofitWithGsonContainerBuilder
import com.halcyonmobile.oauthmoshi.OauthRetrofitWithMoshiContainerBuilder
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET

@Suppress("TestFunctionName")
@RunWith(Parameterized::class)
class ExceptionWrappingCompatibilityTest(
    private val additionalRetrofitConfiguration: (Retrofit.Builder) -> Retrofit.Builder
) {

    private lateinit var basePath: String
    private lateinit var mockAuthenticationLocalStorage: AuthenticationLocalStorage
    private lateinit var mockSessionExpiredEventHandler: SessionExpiredEventHandler
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        basePath = "/google.com/test/"
        mockWebServer = MockWebServer()
        mockAuthenticationLocalStorage = mock()
        mockSessionExpiredEventHandler = mock()
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test//(timeout = 5000L)
    fun GIVEN_moshi_and_standard_sessionExpiration_exception_WHEN_a_refresh_request_is_run_THEN_the_session_expiration_is_Called() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(401)
            setBody("")
        })
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            setBody("{\"Invalid refresh token: unrecognized\"}")
        })

        val service = OauthRetrofitWithMoshiContainerBuilder(
            clientId = "clientId",
            authenticationLocalStorage = mockAuthenticationLocalStorage,
            sessionExpiredEventHandler = mockSessionExpiredEventHandler
        )
            .configureRetrofit {
                additionalRetrofitConfiguration(baseUrl(mockWebServer.url(basePath)))
            }
            .build()
            .oauthRetrofitContainer
            .sessionRetrofit
            .create<Service>()

        try {
            service.request().execute()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
            verifyNoMoreInteractions(mockSessionExpiredEventHandler)
        }
    }

    @Test(timeout = 5000L)
    fun GIVEN_gson_and_standard_sessionExpiration_exception_WHEN_a_refresh_request_is_run_THEN_the_session_expiration_is_Called() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(401)
            setBody("")
        })
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            setBody("{\"Invalid refresh token: unrecognized\"}")
        })

        val service = OauthRetrofitWithGsonContainerBuilder(
            clientId = "clientId",
            authenticationLocalStorage = mockAuthenticationLocalStorage,
            sessionExpiredEventHandler = mockSessionExpiredEventHandler
        )
            .configureRetrofit {
                additionalRetrofitConfiguration(baseUrl(mockWebServer.url(basePath)))
            }
            .build()
            .oauthRetrofitContainer
            .sessionRetrofit
            .create<Service>()

        try {
            service.request().execute()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
            verifyNoMoreInteractions(mockSessionExpiredEventHandler)
        }
    }

    interface Service {
        @GET("service")
        fun request(): Call<Any?>
    }

    companion object {

        private fun standard(builder: Retrofit.Builder): Retrofit.Builder = builder

        private fun errorWrapping(builder: Retrofit.Builder): Retrofit.Builder =
            builder.addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory(workWithoutAnnotation = true))

        // library is incompatible with 2.0.0 of error wrapper
//        private fun errorHandler(builder: Retrofit.Builder): Retrofit.Builder =
//            builder.addCallAdapterFactory(RestHandlerCallAdapter.Builder().build())

        @Parameterized.Parameters
        @JvmStatic
        fun setupParameters(): Collection<Array<Any>> {
            return listOf(
                arrayOf(::standard),
                arrayOf(::errorWrapping),
//                arrayOf(::errorHandler)
            )
        }
    }
}