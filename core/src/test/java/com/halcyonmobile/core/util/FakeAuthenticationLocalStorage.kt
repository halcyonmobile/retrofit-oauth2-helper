package com.halcyonmobile.core.util

import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage

open class FakeAuthenticationLocalStorage : AuthenticationLocalStorage {

    var accessTokenHistory = mutableListOf<String>()
        private set
    var clearCount: Int = 0
        private set
    override var userId: String = ""
    override var accessToken: String = ""
        set(value) {
            accessTokenHistory.add(value)
            field = value
        }
    override var tokenType: String = ""
    override var refreshToken: String = ""

    override fun clear() {
        userId = ""
        accessToken = ""
        tokenType = ""
        refreshToken = ""
        clearCount++
    }

    fun resetAccessTokenHistory() {
        accessTokenHistory.clear()
    }
}