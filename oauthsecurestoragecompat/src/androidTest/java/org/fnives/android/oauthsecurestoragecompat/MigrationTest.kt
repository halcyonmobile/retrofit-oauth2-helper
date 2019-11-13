/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package org.fnives.android.oauthsecurestoragecompat

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage
import com.halcyonmobile.oauthsecurestoragecompat.AuthenticationSecureSharedPreferencesStorageCompat
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedValue = mutableMapOf<String, Any?>(
            "INT" to 1,
            "BOOLEAN" to true,
            "FLOAT" to 123.4f,
            "LONG" to 123L,
            "STRING" to "example",
            "STRING_SET" to setOf("A", "B")
        )

        AuthenticationSecureSharedPreferencesStorage(appContext).sharedPreferences.edit().clear().apply()
        val sharedPreferences = AuthenticationSharedPreferencesStorage.create(appContext)
        sharedPreferences.sharedPreferences.edit()
            .putInt("INT", 1)
            .putBoolean("BOOLEAN", true)
            .putFloat("FLOAT", 123.4f)
            .putLong("LONG", 123L)
            .putString("STRING", "example")
            .putStringSet("STRING_SET", setOf("A", "B"))
            .apply()

        val createAuthStorage = AuthenticationSecureSharedPreferencesStorageCompat(appContext)

        Assert.assertEquals(true, createAuthStorage.sharedPreferences is EncryptedSharedPreferences)
        Assert.assertEquals(expectedValue, createAuthStorage.sharedPreferences.all)
        Assert.assertEquals(emptyMap<String, Any?>(), sharedPreferences.sharedPreferences.all)
    }
}
