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
package com.halcyonmobile.core.basicsetup

import com.halcyonmobile.oauth.SessionDataResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @field:Json(name = "user_id") override val userId: String,
    @field:Json(name = "access_token") override val token: String,
    @field:Json(name = "refresh_token") override val refreshToken: String,
    @field:Json(name = "token_type") override val tokenType: String
) : SessionDataResponse