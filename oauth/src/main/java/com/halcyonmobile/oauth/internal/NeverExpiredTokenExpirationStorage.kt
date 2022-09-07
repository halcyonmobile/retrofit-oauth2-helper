package com.halcyonmobile.oauth.internal

import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage

class NeverExpiredTokenExpirationStorage: TokenExpirationStorage {

    override var accessTokenExpiresAt: Long
        get() = Long.MAX_VALUE
        set(value) = Unit

    override fun clear() = Unit
}