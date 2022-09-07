package com.halcyonmobile.oauth.internal

import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage

/**
 * TokenExpirationStorage that is disabled and states that the session data is never expired
 */
class NeverExpiredTokenExpirationStorage: TokenExpirationStorage {

    override var accessTokenExpiresAt: Long
        get() = Long.MAX_VALUE
        set(value) = Unit

    override fun clear() = Unit
}