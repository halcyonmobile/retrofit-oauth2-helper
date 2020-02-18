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