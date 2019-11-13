/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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