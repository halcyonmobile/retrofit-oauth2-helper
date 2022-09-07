package com.halcyonmobile.core.util

import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage

open class FakeAuthenticationLocalStorage : AuthenticationLocalStorage {

    var clearCount: Int = 0
        private set
    override var userId: String = ""
    override var accessToken: String = ""
    override var tokenType: String = ""
    override var refreshToken: String = ""

    override fun clear() {
        userId = ""
        accessToken = ""
        tokenType = ""
        refreshToken = ""
        clearCount++
    }
}