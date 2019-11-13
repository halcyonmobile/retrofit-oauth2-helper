/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.core

import retrofit2.Call
import retrofit2.http.GET

internal interface SessionExampleService{

    @GET("program")
    fun get() : Call<Any?>
}