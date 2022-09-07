package com.halcyonmobile.oauthsecurestorage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorageTest
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage.Companion.createEncryptedSharedPreferences
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationSecureSharedPreferencesStorageTest : AuthenticationLocalStorageTest<AuthenticationSecureSharedPreferencesStorage>() {

    @Before
    fun setUp() {
        sut = AuthenticationSecureSharedPreferencesStorage.create(
            context = ApplicationProvider.getApplicationContext()
        )
    }

    @After
    fun tearDown() {
        createEncryptedSharedPreferences(ApplicationProvider.getApplicationContext())
            .edit()
            .clear()
            .apply()
    }
}