package com.halcyonmobile.oauth.internal

import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
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
            val updated = refreshTokenForRequest(request) ?: request
            chain.proceed(updated)
        } else {
            chain.proceed(chain.request())
        }
    }

    private fun refreshTokenForRequest(request: Request) =
        authenticator.authenticate(request)
}