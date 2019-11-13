/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.core

import retrofit2.Call
import retrofit2.Response

class ExampleRemoteSource internal constructor(
    private val sessionExampleService: SessionExampleService,
    private val sessionlessExampleService: SessionlessExampleService){

    fun session(callback: Callback) {
        sessionExampleService.get().enqueue(object: retrofit2.Callback<Any?>{
            override fun onFailure(call: Call<Any?>, t: Throwable) {
                callback.error()
            }

            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
                callback.success()
            }
        })
    }

    fun nonsession(callback: Callback) {
        sessionlessExampleService.get().enqueue(object: retrofit2.Callback<Any?>{
            override fun onFailure(call: Call<Any?>, t: Throwable) {
                callback.error()
            }

            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
                callback.success()
            }
        })
    }

    interface Callback{
        fun success()
        fun error()
    }
}