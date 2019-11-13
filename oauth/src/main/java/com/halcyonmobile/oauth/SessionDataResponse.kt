/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth

/**
 * Interface defining the session response.
 *
 * When creating your refresh-token retrofit service, you should return a [retrofit2.Call] containing a subtype of this
 * class, which can be parsed properly.
 * This approach will make it easier to implement the [AuthenticationServiceAdapter]
 */
interface SessionDataResponse {

    val userId: String
    val token: String
    val refreshToken: String
    val tokenType: String
}