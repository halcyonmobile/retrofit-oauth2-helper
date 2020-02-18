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