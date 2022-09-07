package com.halcyonmobile.oauthsecurestorage

import android.content.Context
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage.Companion.createEncryptedSharedPreferences
import com.halcyonmobile.oauthstorage.CombinedSharedPreferencesStorage

object CombinedSecureSharedPreferencesStorage {

    fun create(context: Context, encryptedFileName: String = AuthenticationSecureSharedPreferencesStorage.ENCRYPTED_FILE_NAME) =
        CombinedSharedPreferencesStorage.create(
            sharedPreferences = createEncryptedSharedPreferences(context, encryptedFileName)
        )
}