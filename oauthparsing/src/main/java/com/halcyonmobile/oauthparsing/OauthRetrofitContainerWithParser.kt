/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthparsing

import com.halcyonmobile.oauth.OauthRetrofitContainer

/**
 * Interface defining OauthRetrofitContainers which can also do parsing for [RefreshTokenService]'s response.
 */
interface OauthRetrofitContainerWithParser<Parser> {
    val oauthRetrofitContainer: OauthRetrofitContainer
    val parser: Parser
}