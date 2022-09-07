package com.halcyonmobile.core.util

import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage

class FakeTokenExpirationStorage : TokenExpirationStorage {

    var clearCount: Int = 0
        private set
    override var accessTokenExpiresAt: Long = Long.MIN_VALUE

    override fun clear() {
        accessTokenExpiresAt = Long.MIN_VALUE
        clearCount++
    }
}