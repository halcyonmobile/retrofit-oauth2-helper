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
