package com.halcyonmobile.oauth.internal

import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

internal class TokenExpirationInterceptor(
    private val authenticator: Authenticator,
    private val tokenExpirationStorage: TokenExpirationStorage,
    private val clock: Clock = Clock()
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenExpiration = tokenExpirationStorage.accessTokenExpiresAt
        return if (tokenExpiration < clock.currentTimeMillis()) {
            val request = chain.request()
            val updatedRequest = refreshTokenForRequest(request)
                ?: return sessionExpirationErrorResponse(request)
            chain.proceed(updatedRequest)
        } else {
            chain.proceed(chain.request())
        }
    }

    private fun sessionExpirationErrorResponse(request: Request) =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .message("Session expired!")
            .body("".toResponseBody("application/json".toMediaTypeOrNull()))
            .code(401)
            .build()

    private fun refreshTokenForRequest(request: Request) =
        authenticator.authenticate(request)
}