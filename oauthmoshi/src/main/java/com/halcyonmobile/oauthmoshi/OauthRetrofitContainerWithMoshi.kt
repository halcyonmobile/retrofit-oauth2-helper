/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthmoshi

import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauthparsing.OauthRetrofitContainerWithParser
import com.squareup.moshi.Moshi

class OauthRetrofitContainerWithMoshi(
    override val oauthRetrofitContainer: OauthRetrofitContainer,
    override val parser: Moshi
) : OauthRetrofitContainerWithParser<Moshi> {
    val moshi = parser
}