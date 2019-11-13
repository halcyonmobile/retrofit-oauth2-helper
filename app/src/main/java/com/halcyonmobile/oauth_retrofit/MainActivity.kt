/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth_retrofit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.halcyonmobile.core.ExampleRemoteSource
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val authenticationLocalStorage by inject<AuthenticationLocalStorage>()
    private val exampleRemoteSource by inject<ExampleRemoteSource>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authenticationLocalStorage.tokenType = "type"
        authenticationLocalStorage.accessToken = "accessToken"
        authenticationLocalStorage.refreshToken = "accessToken"
        authenticationLocalStorage.userId = "1234"
        exampleRemoteSource.nonsession(object : ExampleRemoteSource.Callback {
            override fun success() = runSessionService()
            override fun error() = runSessionService()
        })
    }

    fun runSessionService() {
        exampleRemoteSource.session(object : ExampleRemoteSource.Callback {
            override fun success() {
            }

            override fun error() {
            }
        })
    }
}
