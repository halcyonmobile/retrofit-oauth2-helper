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