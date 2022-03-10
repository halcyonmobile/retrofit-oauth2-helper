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
package com.halcyonmobile.core

import com.halcyonmobile.core.oauthgson.createNetworkModules
import com.halcyonmobile.oauth.INVALIDATION_AFTER_REFRESH_HEADER_NAME
import com.halcyonmobile.oauth.INVALIDATION_AFTER_REFRESH_HEADER_VALUE
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauth.runCatchingCausedByAuthFinishedInvalidation
import com.halcyonmobile.oauth.runCatchingCausedByAuthFinishedInvalidationSuspend
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import java.net.HttpURLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class AuthenticationTest {

    private lateinit var path: String
    private lateinit var mockSessionExpiredEventHandler: SessionExpiredEventHandler
    private lateinit var mockAuthenticationLocalStorage: AuthenticationLocalStorage
    private lateinit var mockWebServer: MockWebServer
    private lateinit var sut: AuthService

    @Before
    fun setup() {
        path = "path/test/"
        mockWebServer = MockWebServer()
        mockAuthenticationLocalStorage = mock()
        mockSessionExpiredEventHandler = mock()
        sut = startKoin {
            modules(
                createNetworkModules(
                    baseUrl = mockWebServer.url(path).toString(),
                    clientId = "clientId",
                    provideSessionExpiredEventHandler = { mockSessionExpiredEventHandler },
                    provideAuthenticationLocalStorage = { mockAuthenticationLocalStorage }
                )
                    .plus(module {
                        factory { get<Retrofit>(SESSION_RETROFIT).create(AuthService::class.java) }
                    })
            )
        }.koin.get()
    }

    @After
    fun tearDown() {
        stopKoin()
        mockWebServer.shutdown()
    }

    @Test(timeout = 20000L)
    fun `GIVEN failing request with unauthorized WHEN the refresh token request succeeds THEN it's saved into the local storage and the request header is updated`() {
        var tokenType = ""
        var accessToken = ""
        var refreshToken = "something"
        whenever(mockAuthenticationLocalStorage.tokenType).thenAnswer { tokenType }
        whenever(mockAuthenticationLocalStorage.accessToken).thenAnswer { accessToken }
        whenever(mockAuthenticationLocalStorage.refreshToken).thenAnswer { refreshToken }
        doAnswer { tokenType = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).tokenType = any()
        doAnswer { accessToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).accessToken = any()
        doAnswer { refreshToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).refreshToken = any()
        mockWebServer.enqueueRequest(401, "{}")
        mockWebServer.enqueueRequest(200, readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))
        mockWebServer.enqueueRequest(200, "{}")

        sut.sessionRelatedRequest().execute()

        mockWebServer.takeRequest()
        mockWebServer.takeRequest()
        val requestAfterAuthorization = mockWebServer.takeRequest()
        verifyZeroInteractions(mockSessionExpiredEventHandler)
        verify(mockAuthenticationLocalStorage, times(1)).userId = "user-id"
        verify(mockAuthenticationLocalStorage, times(1)).accessToken = "best-token"
        verify(mockAuthenticationLocalStorage, times(1)).tokenType = "bearer"
        verify(mockAuthenticationLocalStorage, times(1)).refreshToken = "new-refresh-token"
        assertEquals("bearer best-token", requestAfterAuthorization.getHeader("Authorization"))
    }

    @Test(timeout = 20000L)
    fun `GIVEN failing request with unauthorized WHEN the refresh token expired THEN sessionExpiration is invoked`() {
        var tokenType = ""
        var accessToken = ""
        var refreshToken = "something"
        whenever(mockAuthenticationLocalStorage.tokenType).thenAnswer { tokenType }
        whenever(mockAuthenticationLocalStorage.accessToken).thenAnswer { accessToken }
        whenever(mockAuthenticationLocalStorage.refreshToken).thenAnswer { refreshToken }
        doAnswer { tokenType = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).tokenType = any()
        doAnswer { accessToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).accessToken = any()
        doAnswer { refreshToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).refreshToken = any()
        mockWebServer.enqueueRequest(401, "{}")
        mockWebServer.enqueueRequest(401, readJsonResourceFileToString("authentication_service/refresh_token_expired.json"))

        try {
            sut.sessionRelatedRequest().execute()
        } catch (httpException: HttpException) {
        }

        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        verify(mockAuthenticationLocalStorage, times(1)).clear()
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)
    }

    @Test(timeout = 20000L)
    fun `GIVEN failing request with unauthorized WHEN the refresh token is invalid THEN sessionExpiration is invoked`() {
        var tokenType = ""
        var accessToken = ""
        var refreshToken = "something"
        whenever(mockAuthenticationLocalStorage.tokenType).thenAnswer { tokenType }
        whenever(mockAuthenticationLocalStorage.accessToken).thenAnswer { accessToken }
        whenever(mockAuthenticationLocalStorage.refreshToken).thenAnswer { refreshToken }
        doAnswer { tokenType = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).tokenType = any()
        doAnswer { accessToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).accessToken = any()
        doAnswer { refreshToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).refreshToken = any()
        mockWebServer.enqueueRequest(401, "{}")
        mockWebServer.enqueueRequest(400, readJsonResourceFileToString("authentication_service/refresh_token_bad_token.json"))

        try {
            sut.sessionRelatedRequest().execute()
        } catch (httpException: HttpException) {
        }

        verify(mockAuthenticationLocalStorage, times(1)).clear()
        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)
    }

    @Test(timeout = 20000L)
    fun `GIVEN failing request with unauthorized WHEN the refresh token is fails first times but succeeds the second time THEN the request is resend`() {
        var tokenType = ""
        var accessToken = ""
        var refreshToken = "something"
        whenever(mockAuthenticationLocalStorage.tokenType).thenAnswer { tokenType }
        whenever(mockAuthenticationLocalStorage.accessToken).thenAnswer { accessToken }
        whenever(mockAuthenticationLocalStorage.refreshToken).thenAnswer { refreshToken }
        doAnswer { tokenType = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).tokenType = any()
        doAnswer { accessToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).accessToken = any()
        doAnswer { refreshToken = it.arguments.first() as String }.whenever(mockAuthenticationLocalStorage).refreshToken = any()
        whenever(mockAuthenticationLocalStorage.refreshToken).thenReturn("something")
        mockWebServer.enqueueRequest(401, "{}")
        mockWebServer.enqueueRequest(500, "{}")
        mockWebServer.enqueueRequest(200, readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))
        mockWebServer.enqueueRequest(200, "{}")

        sut.sessionRelatedRequest().execute()

        val firstRequest = mockWebServer.takeRequest()
        val authRequest = mockWebServer.takeRequest()
        val authRequest2 = mockWebServer.takeRequest()
        val retriedRequest = mockWebServer.takeRequest()

        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest.body.readUtf8())
        assertEquals("POST", authRequest.method)
        assertEquals("/${path}oauth/token", authRequest.requestUrl?.encodedPath)
        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest2.body.readUtf8())
        assertEquals("POST", authRequest2.method)
        assertEquals("/${path}oauth/token", authRequest2.requestUrl?.encodedPath)
        verify(mockAuthenticationLocalStorage, times(0)).clear()
        verifyZeroInteractions(mockSessionExpiredEventHandler)
        assertEquals(firstRequest.requestUrl, retriedRequest.requestUrl)

        fun RecordedRequest.toHeadersWithoutAuthorization() = headers.toMultimap().filterNot { it.key.equals("Authorization", true) }

        assertEquals(firstRequest.toHeadersWithoutAuthorization(), retriedRequest.toHeadersWithoutAuthorization())
        assertEquals("bearer best-token", retriedRequest.getHeader("Authorization"))
    }

    @Test(timeout = 20000L)
    fun `GIVEN failing request with unauthorized WHEN the refresh token is fails 3 times THEN the request is failed with 401`() {
        whenever(mockAuthenticationLocalStorage.refreshToken).thenReturn("something")
        mockWebServer.enqueueRequest(401, "{}")
        mockWebServer.enqueueRequest(500, "{}")
        mockWebServer.enqueueRequest(500, "{}")
        mockWebServer.enqueueRequest(500, "{}")

        val response = sut.sessionRelatedRequest().execute()

        val firstRequest = mockWebServer.takeRequest()
        val authRequest1 = mockWebServer.takeRequest()
        val authRequest2 = mockWebServer.takeRequest()
        val authRequest3 = mockWebServer.takeRequest()

        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest1.body.readUtf8())
        assertEquals("POST", authRequest1.method)
        assertEquals("/${path}oauth/token", authRequest1.requestUrl?.encodedPath)
        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest2.body.readUtf8())
        assertEquals("POST", authRequest2.method)
        assertEquals("/${path}oauth/token", authRequest2.requestUrl?.encodedPath)
        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest3.body.readUtf8())
        assertEquals("POST", authRequest3.method)
        assertEquals("/${path}oauth/token", authRequest3.requestUrl?.encodedPath)
        verify(mockAuthenticationLocalStorage, times(0)).clear()
        verifyZeroInteractions(mockSessionExpiredEventHandler)
        assertEquals(firstRequest.requestUrl, firstRequest.requestUrl)
        assertEquals(firstRequest.headers, firstRequest.headers)
        assertEquals(false, response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code())
    }

    @Test(timeout = 20000L)
    fun `GIVEN failing request with unauthorized and marked via header WHEN the refresh token succeeds THEN the request is failed with the specific exception`() {
        whenever(mockAuthenticationLocalStorage.refreshToken).thenReturn("something")
        mockWebServer.enqueueRequest(401, "{}")
        mockWebServer.enqueueRequest(200, readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))

        var wasCaught = false
        runCatchingCausedByAuthFinishedInvalidation<Unit>({
            sut.authInvalidTest().execute()
        }, {
            wasCaught = true
        })

        assertEquals("The specific exception wasn't shown", true, wasCaught)
    }

    @Test(timeout = 20000L)
    fun `GIVEN suspending failing request with unauthorized and marked via header WHEN the refresh token succeeds THEN the request is failed with the specific exception`() =
        runBlocking<Unit> {
            whenever(mockAuthenticationLocalStorage.refreshToken).thenReturn("something")
            mockWebServer.enqueueRequest(401, "{}")
            mockWebServer.enqueueRequest(200, readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))

            var wasCaught = false
            runCatchingCausedByAuthFinishedInvalidationSuspend({
                sut.authInvalidTestSuspend()
            }, {
                wasCaught = true
            })

            assertEquals("The specific exception wasn't shown", true, wasCaught)
        }

    interface AuthService {

        @GET("test/service")
        fun sessionRelatedRequest(): Call<Unit>

        @GET("test/service")
        fun authInvalidTest(
            @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader : String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
        ) : Call<Unit>

        @GET("test/service")
        suspend fun authInvalidTestSuspend(
            @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader : String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
        ) : Unit
    }

    companion object {
        /**
         * Enqueue a [MockResponse] with the given [bodyJson] as [MockResponse.body] and given [responseCode] as [MockResponse.setResponseCode]
         */
        fun MockWebServer.enqueueRequest(responseCode: Int = 200, bodyJson: String) =
                enqueue(MockResponse().apply {
                    setBody(bodyJson)
                    setResponseCode(responseCode)
                })

        /**
         * Reads content of the given [fileName] resource file into a String
         */
        fun readJsonResourceFileToString(fileName: String): String {
            val uri =  ClassLoader.getSystemResource(fileName).toURI()
            val mainPath: String = Paths.get(uri).toString()
            return Files.lines(Paths.get(mainPath)).collect(Collectors.joining())
        }
    }
}