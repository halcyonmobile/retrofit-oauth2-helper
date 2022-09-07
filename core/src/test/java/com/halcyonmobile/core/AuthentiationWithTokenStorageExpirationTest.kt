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
import com.halcyonmobile.core.util.FakeAuthenticationLocalStorage
import com.halcyonmobile.core.util.FakeTokenExpirationStorage
import com.halcyonmobile.core.util.TestClock
import com.halcyonmobile.oauth.INVALIDATION_AFTER_REFRESH_HEADER_NAME
import com.halcyonmobile.oauth.INVALIDATION_AFTER_REFRESH_HEADER_VALUE
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage
import com.halcyonmobile.oauth.internal.Clock
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert
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
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Specific expiration tests given [TokenExpirationStorage].
 *
 */
class AuthentiationWithTokenStorageExpirationTest {

    private lateinit var path: String
    private lateinit var mockSessionExpiredEventHandler: SessionExpiredEventHandler
    private lateinit var fakeAuthenticationLocalStorage: FakeAuthenticationLocalStorage
    private lateinit var fakeTokenExpirationStorage: TokenExpirationStorage
    private lateinit var mockWebServer: MockWebServer
    private lateinit var sut: AuthService
    private lateinit var testClock: TestClock

    @Before
    fun setup() {
        path = "path/test/"
        mockWebServer = MockWebServer()
        fakeAuthenticationLocalStorage = FakeAuthenticationLocalStorage()
        mockSessionExpiredEventHandler = mock()
        fakeTokenExpirationStorage = FakeTokenExpirationStorage()
        testClock = TestClock(5000L)
        Clock.swap(testClock)
        sut = startKoin {
            modules(
                createNetworkModules(
                    baseUrl = mockWebServer.url(path).toString(),
                    clientId = "clientId",
                    provideSessionExpiredEventHandler = { mockSessionExpiredEventHandler },
                    provideAuthenticationLocalStorage = { fakeAuthenticationLocalStorage },
                    provideTokenExpirationStorage = { fakeTokenExpirationStorage }
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

    /** GIVEN non expired token
     * WHEN the request goes out
     * THEN it's using the token and no changes
     */
    @Test(timeout = 20000L)
    fun nonExpiredTokenRequestJustGoesOut() {
        val tokenType = "bearer"
        val accessToken = "access"
        val refreshToken = "something"
        fakeAuthenticationLocalStorage.tokenType = tokenType
        fakeAuthenticationLocalStorage.accessToken = accessToken
        fakeAuthenticationLocalStorage.refreshToken = refreshToken
        fakeTokenExpirationStorage.accessTokenExpiresAt = Long.MAX_VALUE
        mockWebServer.enqueueRequest(200, "{}")

        sut.sessionRelatedRequest().execute()

        val request = mockWebServer.takeRequest()
        verifyZeroInteractions(mockSessionExpiredEventHandler)
        assertEquals("bearer", tokenType)
        assertEquals("access", accessToken)
        assertEquals("something", refreshToken)
        assertEquals("bearer access", request.getHeader("Authorization"))
    }

    /** GIVEN expired token and succeeding refresh-token request
     * WHEN the request goes out
     * THEN before it's sent, the refresh request is called and token is updated
     */
    @Test(timeout = 20000L)
    fun expiredTokenMeansRefreshBeforeRequest() {
        fakeAuthenticationLocalStorage.tokenType = "bearerx"
        fakeAuthenticationLocalStorage.accessToken = "access"
        fakeAuthenticationLocalStorage.refreshToken = "something"
        fakeTokenExpirationStorage.accessTokenExpiresAt = Long.MIN_VALUE

        mockWebServer.enqueueRequest(200, AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))
        mockWebServer.enqueueRequest(200, "{}")

        sut.sessionRelatedRequest().execute()

        val refreshRequest = mockWebServer.takeRequest()
        val sessionRequest = mockWebServer.takeRequest()
        verifyZeroInteractions(mockSessionExpiredEventHandler)
        assertEquals("user-id", fakeAuthenticationLocalStorage.userId)
        assertEquals("best-token", fakeAuthenticationLocalStorage.accessToken)
        assertEquals("bearer", fakeAuthenticationLocalStorage.tokenType)
        assertEquals("new-refresh-token", fakeAuthenticationLocalStorage.refreshToken)
        assertEquals(testClock.currentTimeMillis() + TimeUnit.SECONDS.toMillis(299), fakeTokenExpirationStorage.accessTokenExpiresAt)
        assertEquals("bearer best-token", sessionRequest.getHeader("Authorization"))
    }

    interface AuthService {

        @GET("test/service")
        fun sessionRelatedRequest(): Call<Unit>

        @GET("test/service")
        fun authInvalidTest(
            @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader: String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
        ): Call<Unit>

        @GET("test/service")
        suspend fun authInvalidTestSuspend(
            @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader: String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
        ): Unit
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
            val uri = ClassLoader.getSystemResource(fileName).toURI()
            val mainPath: String = Paths.get(uri).toString()
            return Files.lines(Paths.get(mainPath)).collect(Collectors.joining())
        }
    }
}