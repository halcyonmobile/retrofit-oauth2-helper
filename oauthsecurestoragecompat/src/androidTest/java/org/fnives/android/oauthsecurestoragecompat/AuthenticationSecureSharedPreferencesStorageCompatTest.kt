package org.fnives.android.oauthsecurestoragecompat

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorageTest
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage.Companion.createEncryptedSharedPreferences
import com.halcyonmobile.oauthsecurestoragecompat.AuthenticationSecureSharedPreferencesStorageCompat
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationSecureSharedPreferencesStorageCompatTest : AuthenticationLocalStorageTest<AuthenticationSecureSharedPreferencesStorageCompat>() {

    @Before
    fun setUp() {
        sut = AuthenticationSecureSharedPreferencesStorageCompat(
            context = ApplicationProvider.getApplicationContext()
        )
    }

    @After
    fun tearDown() {
        createEncryptedSharedPreferences(context = ApplicationProvider.getApplicationContext())
            .edit()
            .clear()
            .apply()
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences(AuthenticationSharedPreferencesStorage.PREFERENCES_KEY, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

    }
}