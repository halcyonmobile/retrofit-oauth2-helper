package com.halcyonmobile.oauthsecurestoragecompat

import android.content.Context
import android.os.Build
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage
import com.halcyonmobile.oauthstorage.CombinedSharedPreferencesStorage

object CombinedSecureSharedPreferencesStorageCompat {

    fun create(
        context: Context,
        encryptedFileName: String = AuthenticationSecureSharedPreferencesStorage.ENCRYPTED_FILE_NAME,
        preferenceKey: String = AuthenticationSharedPreferencesStorage.PREFERENCES_KEY
    ): CombinedSharedPreferencesStorage {
        val nonEncryptedSharedPreferences = AuthenticationSharedPreferencesStorage.createSharedPreferences(context, preferenceKey)

        val preference = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedSharedPreferences = AuthenticationSecureSharedPreferencesStorage.createEncryptedSharedPreferences(context, encryptedFileName)
            AuthenticationSecureSharedPreferencesStorageCompat.migrateIfNeeded(nonEncryptedSharedPreferences, encryptedSharedPreferences)

            encryptedSharedPreferences
        } else {
            nonEncryptedSharedPreferences
        }

        return CombinedSharedPreferencesStorage.create(preference)
    }
}