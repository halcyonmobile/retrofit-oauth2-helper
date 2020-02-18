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
package com.halcyonmobile.oauth.dependencies

/**
 * Interfaces annotated with this class will generate a [com.halcyonmobile.oauth.AuthenticationServiceAdapter].
 * The generated adapter should be used in the [com.halcyonmobile.oauth.OauthRetrofitContainerBuilder]
 *
 * Restrictions:
 * - the annotated class must be an interface.
 * - it must contain only one function, which has only one parameter without default value and it's the first one
 * - the function has to return a [retrofit2.Call] with a typeParameter which extends [SessionDataResponse]
 *
 * Example:
 *
 * Given the following file:
 * ```
 * @RefreshService
 * interface RefreshTokenService {
 *
 * @POST("oauth/token")
 * @FormUrlEncoded
 * fun refresh(@Field("refresh_token") refreshToken: String, @Field("grant_type") grantType: String = "refresh_token"): Call<RefreshTokenResponse>
 * }
 * ```
 *
 * Where RefreshTokenResponse looks lke this:
 * ```
 * data class RefreshTokenResponse(
 *   override val userId: String,
 *   override val token: String,
 *   override val refreshToken: String,
 *   override val tokenType: String
 * ) : SessionDataResponse
 * ```
 *
 * Will generate the following adapter:
 * ```
 * package com.halcyonmobile.oauth
 *
 * import com.halcyonmobile.core.RefreshTokenService
 * import kotlin.String
 * import retrofit2.Call
 *
 * /**
 *  * [AuthenticationServiceAdapter] implementation generated for
 *  * [com.halcyonmobile.core.RefreshTokenService] class annotated with [com.halcyonmobile.oauth.dependencies.RefreshService]
 *  */
 * class RefreshTokenServiceAuthenticationServiceAdapter :
 * AuthenticationServiceAdapter<com.halcyonmobile.core.RefreshTokenService> {
 *
 * override fun adapt(service: RefreshTokenService): AuthenticationService = AuthenticationServiceImpl(service)
 *
 *   class AuthenticationServiceImpl(private val service: RefreshTokenService) : AuthenticationService {
 *
 *     override fun refreshToken(refreshToken: String): Call<out SessionDataResponse> = service.refresh(refreshToken)
 *   }
 * }
 * ```
*/
@Target(AnnotationTarget.CLASS)
annotation class RefreshService