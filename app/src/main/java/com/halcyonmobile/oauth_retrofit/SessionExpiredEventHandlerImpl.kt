/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth_retrofit

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler

class SessionExpiredEventHandlerImpl(private val context: Context) : SessionExpiredEventHandler {
    override fun onSessionExpired() {
        Handler(Looper.getMainLooper()).post {
            // todo navigate to sign in
        }
    }

}