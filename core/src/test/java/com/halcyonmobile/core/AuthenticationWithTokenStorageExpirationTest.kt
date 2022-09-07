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
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage
import com.halcyonmobile.oauth.internal.Clock
import com.halcyonmobile.oauth.internal.DefaultIsSessionExpiredException
import com.halcyonmobile.oauth.runCatchingCausedByAuthFinishedInvalidation
import com.halcyonmobile.oauth.runCatchingCausedByAuthFinishedInvalidationSuspend
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
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
import org.mockito.Mockito.spy
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Specific expiration tests given [TokenExpirationStorage].
 *
 */
class AuthenticationWithTokenStorageExpirationTest {

    private lateinit var path: String
    private lateinit var mockSessionExpiredEventHandler: SessionExpiredEventHandler
    private lateinit var fakeAuthenticationLocalStorage: FakeAuthenticationLocalStorage
    private lateinit var fakeTokenExpirationStorage: FakeTokenExpirationStorage
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

    /** GIVEN expired token and succeeding refresh-token request but without exipres_in response
     * WHEN the request goes out
     * THEN before it's sent, the refresh request is called and token is updated
     */
    @Test(timeout = 20000L)
    fun noExpiresInProvidedMeansNoSaving() {
        val tokenType = "bearerx"
        val accessToken = "access"
        val refreshToken = "something"
        val expirationTime = 5L
        fakeAuthenticationLocalStorage.tokenType = tokenType
        fakeAuthenticationLocalStorage.accessToken = accessToken
        fakeAuthenticationLocalStorage.refreshToken = refreshToken
        fakeTokenExpirationStorage.accessTokenExpiresAt = expirationTime

        val response = AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_positive.json")
        val updatedResponse = response.replace("expires_in", "something_not_relevant")
        mockWebServer.enqueueRequest(200, updatedResponse)
        mockWebServer.enqueueRequest(200, "{}")

        sut.sessionRelatedRequest().execute()

        val refreshRequest = mockWebServer.takeRequest()
        val sessionRequest = mockWebServer.takeRequest()
        verifyZeroInteractions(mockSessionExpiredEventHandler)
        assertEquals("user-id", fakeAuthenticationLocalStorage.userId)
        assertEquals("best-token", fakeAuthenticationLocalStorage.accessToken)
        assertEquals("bearer", fakeAuthenticationLocalStorage.tokenType)
        assertEquals("new-refresh-token", fakeAuthenticationLocalStorage.refreshToken)
        assertEquals(5L, fakeTokenExpirationStorage.accessTokenExpiresAt)
        assertEquals("bearer best-token", sessionRequest.getHeader("Authorization"))
    }

    /** GIVEN expired token and expired refresh-token request
     * WHEN the request goes out
     * THEN before it's sent, the refresh request is called and session is expired
     */
    @Test(timeout = 20000L)
    fun sessionExpirationBecauseOfUnauthenticated() {
        val tokenType = "bearerx"
        val accessToken = "access"
        val refreshToken = "something"
        val expirationTime = Long.MIN_VALUE + 5
        fakeAuthenticationLocalStorage.tokenType = tokenType
        fakeAuthenticationLocalStorage.accessToken = accessToken
        fakeAuthenticationLocalStorage.refreshToken = refreshToken
        fakeTokenExpirationStorage.accessTokenExpiresAt = expirationTime

        mockWebServer.enqueueRequest(401, AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_expired.json"))

        val response = sut.sessionRelatedRequest().execute()

        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        assertEquals(1, fakeAuthenticationLocalStorage.clearCount)
        assertEquals(Long.MIN_VALUE, fakeTokenExpirationStorage.accessTokenExpiresAt)
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)

        Assert.assertFalse("Request should have failed!", response.isSuccessful)
        assertEquals(401, response.code())
    }

    /** GIVEN invalid token and succeeding refresh-token request but without exipres_in response
     * WHEN the request goes out
     * THEN before it's sent, the refresh request is called and token is updated
     */
    @Test(timeout = 20000L)
    fun sessionExpirationBecauseOfInvalid() {
        val tokenType = "bearerx"
        val accessToken = "access"
        val refreshToken = "something"
        val expirationTime = Long.MIN_VALUE
        fakeAuthenticationLocalStorage.tokenType = tokenType
        fakeAuthenticationLocalStorage.accessToken = accessToken
        fakeAuthenticationLocalStorage.refreshToken = refreshToken
        fakeTokenExpirationStorage.accessTokenExpiresAt = expirationTime

        mockWebServer.enqueueRequest(400, AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_bad_token.json"))

        val response = sut.sessionRelatedRequest().execute()

        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        assertEquals(1, fakeAuthenticationLocalStorage.clearCount)
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)

        Assert.assertFalse("Request should have failed!", response.isSuccessful)
        assertEquals(401, response.code())
    }

    @Test(timeout = 20000L)
    fun sessionExpirationHandledWithoutDataRequest() {
        val expirationTime = Long.MIN_VALUE
        fakeAuthenticationLocalStorage.tokenType = "bear"
        fakeAuthenticationLocalStorage.accessToken = "accessToken"
        fakeAuthenticationLocalStorage.refreshToken = "something"
        fakeTokenExpirationStorage.accessTokenExpiresAt = expirationTime

        // session expired response
        mockWebServer.enqueueRequest(400, AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_bad_token.json"))

        val response = sut.sessionRelatedRequest().execute()

        val refreshRequest = mockWebServer.takeRequest()

        assertEquals("refresh_token=something&grant_type=refresh_token", refreshRequest.body.readUtf8())
        assertEquals("POST", refreshRequest.method)
        assertEquals("/${path}oauth/token", refreshRequest.requestUrl?.encodedPath)
        assertEquals(1, fakeAuthenticationLocalStorage.clearCount)
        assertEquals(1, fakeTokenExpirationStorage.clearCount)
        verify(mockSessionExpiredEventHandler, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpiredEventHandler)

        Assert.assertFalse("Request should have failed!", response.isSuccessful)
        assertEquals(401, response.code())
    }

    @Test(timeout = 20000L)
    fun sessionRefreshFailsLetsRequestRun() {
        val expirationTime = Long.MIN_VALUE
        fakeAuthenticationLocalStorage.tokenType = "bear"
        fakeAuthenticationLocalStorage.accessToken = "accessToken"
        fakeAuthenticationLocalStorage.refreshToken = "something"
        fakeTokenExpirationStorage.accessTokenExpiresAt = expirationTime

        // request error, no session expiration
        mockWebServer.enqueueRequest(500, "{}")
        mockWebServer.enqueueRequest(500, "{}")
        mockWebServer.enqueueRequest(500, "{}")
        // data request's response
        mockWebServer.enqueueRequest(464, "{}")

        val response = sut.sessionRelatedRequest().execute()

        val refreshRequest1 = mockWebServer.takeRequest() // fail
        val refreshRequest2 = mockWebServer.takeRequest() // fail
        val refreshRequest3 = mockWebServer.takeRequest() // fail
        val dataRequest = mockWebServer.takeRequest() // let run for proper error


        assertEquals(0, fakeAuthenticationLocalStorage.clearCount)
        assertEquals(0, fakeTokenExpirationStorage.clearCount)
        verifyZeroInteractions(mockSessionExpiredEventHandler)

        assertEquals(464, response.code())
    }

    /**
     * GIVEN failing request with unauthorized
     * WHEN the refresh token is fails first times but succeeds the second time
     * THEN the request is resend
     */
    @Test(timeout = 20000L)
    fun refreshTokenRequestIsRetried() {
        val tokenType = ""
        val accessToken = ""
        val refreshToken = "something"
        val expirationTime = Long.MIN_VALUE
        fakeAuthenticationLocalStorage.tokenType = tokenType
        fakeAuthenticationLocalStorage.accessToken = accessToken
        fakeAuthenticationLocalStorage.refreshToken = refreshToken
        fakeTokenExpirationStorage.accessTokenExpiresAt = expirationTime
        mockWebServer.enqueueRequest(500, "{}")
        mockWebServer.enqueueRequest(200, AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))
        mockWebServer.enqueueRequest(200, "{}")

        sut.sessionRelatedRequest().execute()

        val authRequest = mockWebServer.takeRequest()
        val authRequest2 = mockWebServer.takeRequest()
        val actualRequest = mockWebServer.takeRequest()


        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest.body.readUtf8())
        assertEquals("POST", authRequest.method)
        assertEquals("/${path}oauth/token", authRequest.requestUrl?.encodedPath)
        assertEquals("refresh_token=something&grant_type=refresh_token", authRequest2.body.readUtf8())
        assertEquals("POST", authRequest2.method)
        assertEquals("/${path}oauth/token", authRequest2.requestUrl?.encodedPath)
        assertEquals(0, fakeAuthenticationLocalStorage.clearCount)
        verifyZeroInteractions(mockSessionExpiredEventHandler)

        assertEquals("bearer best-token", actualRequest.getHeader("Authorization"))
    }

    /**
     * GIVEN First request is refreshing
     * WHEN second request going out before First finishes
     * THEN second awaits the refreshing and uses the returned tokens
     */
    @Test
    fun synchronization() {
        fakeAuthenticationLocalStorage.accessToken = "initial"
        fakeAuthenticationLocalStorage.resetAccessTokenHistory()
        fakeAuthenticationLocalStorage.refreshToken = "firstRefreshToken"
        fakeTokenExpirationStorage.accessTokenExpiresAt = Long.MIN_VALUE
        val captured = mutableListOf<RecordedRequest>()

        val firstRequestStarts = CountDownLatch(1)
        val firstRequestFinishes = CountDownLatch(1)
        val firstRefreshBody = AuthenticationWithoutTokenStorageTest.readJsonResourceFileToString("authentication_service/refresh_token_positive.json")
        val secondRefreshBody = firstRefreshBody.replace("best-token", "should-never-be-saved")
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                captured.add(request)
                firstRequestStarts.countDown()
                if (request.isRefreshRequest("firstRefreshToken")) {
                    Thread.sleep(3000L) // await second request starting
                    return MockResponse(bodyJson = firstRefreshBody)
                }
                return MockResponse(bodyJson = secondRefreshBody)
            }
        }

        Executors.newSingleThreadExecutor().execute {
            sut.sessionRelatedRequest().execute()
            firstRequestFinishes.countDown()
        }
        firstRequestStarts.await()
        fakeAuthenticationLocalStorage.refreshToken = "secondRefreshToken"
        sut.sessionRelatedRequest().execute()
        firstRequestFinishes.await()

        try {
            assertEquals(listOf("best-token"), fakeAuthenticationLocalStorage.accessTokenHistory)
            assertEquals(3, captured.size)
            val refreshRequest = captured[0]
            val firstRequest = captured[1]
            val secondRequest = captured[2]
            assertEquals("bearer best-token", firstRequest.getHeader("Authorization"))
            assertEquals("bearer best-token", secondRequest.getHeader("Authorization"))
        } catch (throwable: Throwable) {
            System.err.println(captured)
            throw throwable
        }
    }

    /**
     * GIVEN failing request with expired_token and marked via header
     * WHEN the refresh token succeeds
     * THEN the request is failed with the specific exception
     */
    @Test(timeout = 20000L)
    fun invalidatedRequestIsNotRetriedOnSessionFailure() {
        fakeAuthenticationLocalStorage.refreshToken = "something"
        fakeTokenExpirationStorage.accessTokenExpiresAt = Long.MIN_VALUE
        mockWebServer.enqueueRequest(200, AuthenticationWithTokenStorageLocallyNotExpiredTest.readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))
        mockWebServer.enqueueRequest(200, "{}")

        var wasCaught = false
        runCatchingCausedByAuthFinishedInvalidation<Unit>({
            sut.authInvalidTest().execute()
        }, {
            wasCaught = true
        })

        assertEquals("The specific exception wasn't shown", true, wasCaught)
    }

    /** GIVEN suspending failing request with expired_token and marked via header
     * WHEN the refresh token succeeds
     * THEN the request is failed with the specific exception
     */
    @Test(timeout = 20000L)
    fun suspendInvalidatedRequestIsNotRetriedOnSessionFailure() = runBlocking<Unit> {
        fakeAuthenticationLocalStorage.refreshToken = "something"
        fakeTokenExpirationStorage.accessTokenExpiresAt = Long.MIN_VALUE
        mockWebServer.enqueueRequest(200, AuthenticationWithTokenStorageLocallyNotExpiredTest.readJsonResourceFileToString("authentication_service/refresh_token_positive.json"))
        mockWebServer.enqueueRequest(200, "{}")

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
            @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader: String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
        ): Call<Unit>

        @GET("test/service")
        suspend fun authInvalidTestSuspend(
            @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader: String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
        ): Unit
    }

    companion object {

        private fun RecordedRequest.isRefreshRequest(token: String) =
            method == "POST" && requestUrl?.encodedPath == "/path/test/oauth/token" && body.readUtf8().contains("refresh_token=$token")

        /**
         * Enqueue a [MockResponse] with the given [bodyJson] as [MockResponse.body] and given [responseCode] as [MockResponse.setResponseCode]
         */
        fun MockWebServer.enqueueRequest(responseCode: Int = 200, bodyJson: String) =
            enqueue(MockResponse(responseCode = responseCode, bodyJson = bodyJson))

        fun MockResponse(responseCode: Int = 200, bodyJson: String) =
            MockResponse().apply {
                setBody(bodyJson)
                setResponseCode(responseCode)
            }

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