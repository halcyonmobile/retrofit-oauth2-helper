/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthgson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.halcyonmobile.oauth.SessionDataResponse
import java.lang.reflect.Type

class SessionDataResponseDeserializer : JsonDeserializer<SessionDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SessionDataResponse =
        context.deserialize(jsonElement.asJsonObject, RefreshTokenResponse::class.java)

}