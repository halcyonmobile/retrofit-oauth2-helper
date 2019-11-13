/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth

import retrofit2.HttpException

/**
 * Defines what [HttpException] should be considered as SessionExpiration.
 */
interface IsSessionExpiredException {

    /**
     * @return true if the given [httpException] comming from the refresh-token-request should be considered as SessionExpiration.
     */
    operator fun invoke(httpException: HttpException): Boolean
}