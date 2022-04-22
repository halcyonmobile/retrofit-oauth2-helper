/*
 * Copyright (c) 2020 Halcyon Mobile.
 * https://www.halcyonmobile.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.halcyonmobile.oauthmoshi

import com.halcyonmobile.oauth.IsSessionExpiredException as DeprecatedIsSessionExpiredException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.halcyonmobile.oauth.SessionDataResponse
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthgson.OauthRetrofitWithGsonContainerBuilder
import com.halcyonmobile.oauthparsing.RefreshServiceFieldParameterProvider
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.create
import retrofit2.http.GET

@Suppress("TestFunctionName")
class OauthRetrofitWithGsonContainerBuilderTest {

    private lateinit var basePath: String
    private lateinit var mockAuthenticationLocalStorage: AuthenticationLocalStorage
    private lateinit var mockSessionExpiredEventHandler: SessionExpiredEventHandler
    private lateinit var builder: OauthRetrofitWithGsonContainerBuilder
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        basePath = "/google.com/test/"
        mockWebServer = MockWebServer()
        mockAuthenticationLocalStorage = mock()
        mockSessionExpiredEventHandler = mock()
        builder = OauthRetrofitWithGsonContainerBuilder("clientId", mockAuthenticationLocalStorage, mockSessionExpiredEventHandler)
            .configureRetrofit {
                baseUrl(mockWebServer.url(basePath))
            }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun GIVEN_default_setup_WHEN_a_refresh_request_is_run_THEN_it_has_proper_default_setup() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.setUnauthorizedThenProperRefreshThenSuccessRequests()
        val service = builder.build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        service.request().execute()

        val originalRequests = mockWebServer.takeRequest()
        val refreshRequest = mockWebServer.takeRequest()

        Assert.assertEquals("POST", refreshRequest.method)
        Assert.assertEquals("${basePath}oauth/token", refreshRequest.requestUrl?.encodedPath)
        Assert.assertEquals(setOf("grant_type=refresh_token", "refresh_token=alma"), refreshRequest.body.readString(Charsets.UTF_8).split("&").toSet())
        verifyZeroInteractions(mockSessionExpiredEventHandler)
    }

    @Test
    fun GIVEN_configured_grant_type_WHEN_a_refresh_request_is_run_THEN_it_has_proper_default_setup() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.setUnauthorizedThenProperRefreshThenSuccessRequests()
        val service = builder.setGrantType("my_grant").build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        service.request().execute()

        val originalRequests = mockWebServer.takeRequest()
        val refreshRequest = mockWebServer.takeRequest()

        Assert.assertEquals(setOf("grant_type=my_grant", "refresh_token=alma"), refreshRequest.body.readString(Charsets.UTF_8).split("&").toSet())
    }

    @Test
    fun GIVEN_configured_refresh_token_field_name_WHEN_a_refresh_request_is_run_THEN_it_has_proper_default_setup() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.setUnauthorizedThenProperRefreshThenSuccessRequests()
        val service = builder.setRefreshTokenFieldName("new_refresh_token_field_name").build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        service.request().execute()

        val originalRequests = mockWebServer.takeRequest()
        val refreshRequest = mockWebServer.takeRequest()

        Assert.assertEquals(setOf("grant_type=refresh_token", "new_refresh_token_field_name=alma"), refreshRequest.body.readString(Charsets.UTF_8).split("&").toSet())
    }

    @Test
    fun GIVEN_configured_service_path_WHEN_a_refresh_request_is_run_THEN_it_has_proper_default_setup() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.setUnauthorizedThenProperRefreshThenSuccessRequests()
        val service = builder.setRefreshServicePath("banan").build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        service.request().execute()

        val originalRequests = mockWebServer.takeRequest()
        val refreshRequest = mockWebServer.takeRequest()

        Assert.assertEquals("${basePath}banan", refreshRequest.requestUrl?.encodedPath)
    }

    @Test
    fun GIVEN_configured_additional_fields_WHEN_a_refresh_request_is_run_THEN_it_has_proper_default_setup() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.setUnauthorizedThenProperRefreshThenSuccessRequests()
        val service = builder.setRefreshServiceFieldParameterProvider(object : RefreshServiceFieldParameterProvider {
            override fun get(token: String): Map<String, Any?> =
                mapOf("a" to token, "grant" to "token")

        }).build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        service.request().execute()

        val originalRequests = mockWebServer.takeRequest()
        val refreshRequest = mockWebServer.takeRequest()

        Assert.assertEquals(setOf("grant=token", "a=alma", "grant_type=refresh_token", "refresh_token=alma"), refreshRequest.body.readString(Charsets.UTF_8).split("&").toSet())
    }

    @Test
    fun GIVEN_custom_session_expiration_exception_decider_WHEN_a_refresh_request_is_run_THEN_the_exception_is_delegated_properly_and_the_decistion_is_respected() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(401)
            setBody("")
        })
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            setBody(REFRESH_RESPONSE)
        })
        var caughtHttpException: HttpException? = null
        val service = builder.setIsSessionExpiredExceptionDecider(object : IsSessionExpiredException {
            override fun invoke(throwable: Throwable): Boolean {
                caughtHttpException = throwable as HttpException
                return true
            }

        }).build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        try {
            service.request().execute()
        } catch (httpException: HttpException) {
            Assert.assertEquals(401, httpException.code())
        }

        Assert.assertEquals(400, caughtHttpException?.code())
        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)
    }

    @Test
    fun GIVEN_custom_deprecated_session_expiration_exception_decider_WHEN_a_refresh_request_is_run_THEN_the_exception_is_delegated_properly_and_the_decistion_is_respected() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(401)
            setBody("")
        })
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            setBody(REFRESH_RESPONSE)
        })
        var caughtHttpException: HttpException? = null
        val service = builder.setIsSessionExpiredExceptionDecider(object : DeprecatedIsSessionExpiredException {
            override fun invoke(httpException: HttpException): Boolean {
                caughtHttpException = httpException
                return true
            }

        }).build().oauthRetrofitContainer.sessionRetrofit.create<Service>()

        try {
            service.request().execute()
        } catch (httpException: HttpException) {
            Assert.assertEquals(401, httpException.code())
        }

        Assert.assertEquals(400, caughtHttpException?.code())
        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)
    }

    @Test
    fun GIVEN_disabled_default_parsing_and_custom_implementation_WHEN_a_refresh_request_is_run_THEN_that_is_used() {
        whenever(mockAuthenticationLocalStorage.refreshToken).doReturn("alma")
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(401)
            setBody("")
        })
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(200)
            setBody("{}")
        })
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(200)
            setBody("{}")
        })
        val service = builder.disableDefaultParsing()
            .configureGson {
                registerTypeAdapter(SessionDataResponse::class.java, object : TypeAdapter<SessionDataResponse>() {
                    override fun write(out: JsonWriter?, value: SessionDataResponse?) {
                        TODO()
                    }

                    override fun read(jsonReader: JsonReader): SessionDataResponse =
                        object : SessionDataResponse {
                            override val userId: String get() = "a"
                            override val token: String get() = "b"
                            override val refreshToken: String get() = "c"
                            override val tokenType: String get() = "d"
                        }.also {
                            jsonReader.beginObject()
                            jsonReader.endObject()
                        }

                })
            }
            .build()
            .oauthRetrofitContainer.sessionRetrofit.create<Service>()

        service.request().execute()

        verify(mockAuthenticationLocalStorage, times(1)).refreshToken = "c"
        verify(mockAuthenticationLocalStorage, times(1)).accessToken = "b"
        verify(mockAuthenticationLocalStorage, times(1)).tokenType = "d"
        verify(mockAuthenticationLocalStorage, times(1)).userId = "a"
    }


    interface Service {
        @GET("service")
        fun request(): Call<Any?>
    }

    companion object {

        private fun MockWebServer.setUnauthorizedThenProperRefreshThenSuccessRequests() {
            enqueue(MockResponse().apply {
                setResponseCode(401)
                setBody("")
            })
            enqueue(MockResponse().apply {
                setResponseCode(200)
                setBody(REFRESH_RESPONSE)
            })
            enqueue(MockResponse().apply {
                setResponseCode(200)
                setBody("{}")
            })
        }

        val REFRESH_RESPONSE = """
            {
                "access_token": "best-token",
                "token_type": "bearer",
                "refresh_token": "new-refresh-token",
                "expires_in": 299,
                "scope": "trust",
                "user_id": "user-id",
                "jti": "82015e11-da47-4fb9-aa0a-6b518f402ed7"
            }
        """.trimIndent()
    }
}