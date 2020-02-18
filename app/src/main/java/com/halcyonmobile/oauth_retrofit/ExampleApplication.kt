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

import android.app.Application
import com.halcyonmobile.core.oauthgson.createNetworkModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@ExampleApplication)
            modules(createModules())
        }
    }

    fun createModules(): List<Module> =
        listOf(
            module {
                single { SharedPreferencesManager(get(), encrypted = false, compat = true) }
            }
        )
            .plus(
                createNetworkModules(
                clientId = "CLIENT-ID",
                baseUrl = "https://google.com/",
                provideAuthenticationLocalStorage = { get<SharedPreferencesManager>() },
                provideSessionExpiredEventHandler = { SessionExpiredEventHandlerImpl(get()) }
            ))
}