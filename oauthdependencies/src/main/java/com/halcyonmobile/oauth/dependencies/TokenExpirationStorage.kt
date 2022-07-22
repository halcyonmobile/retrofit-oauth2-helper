package com.halcyonmobile.oauth.dependencies

import androidx.annotation.WorkerThread

/**
 * Simple storage for the access token's expiration time.
 *
 * Note: workerThread annotation is just a suggestion
 */
interface TokenExpirationStorage  {

    /**
     * The expiration time of the access token in unix timestamp.
     */
    @set:WorkerThread
    @get:WorkerThread
    var accessTokenExpiresAt: Long
}