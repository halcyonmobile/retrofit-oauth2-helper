/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthgson

import com.google.gson.Gson
import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauthparsing.OauthRetrofitContainerWithParser

class OauthRetrofitContainerWithGson(
    override val oauthRetrofitContainer: OauthRetrofitContainer,
    override val parser: Gson
) : OauthRetrofitContainerWithParser<Gson> {
    val gson: Gson = parser
}