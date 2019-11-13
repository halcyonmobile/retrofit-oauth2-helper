/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthparsing

/**
 * This interface enables to add any additional field parameter to the [RefreshTokenService] call.
 */
interface RefreshServiceFieldParameterProvider {

    fun get(token: String): Map<String, Any?>
}