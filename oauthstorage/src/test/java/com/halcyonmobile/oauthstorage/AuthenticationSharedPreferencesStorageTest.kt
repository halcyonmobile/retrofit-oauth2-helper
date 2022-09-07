package com.halcyonmobile.oauthstorage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorageTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationSharedPreferencesStorageTest : AuthenticationLocalStorageTest<AuthenticationSharedPreferencesStorage>() {

    @Before
    fun setUp() {
        sut = AuthenticationSharedPreferencesStorage.create(
            context = ApplicationProvider.getApplicationContext()
        )
    }

    @After
    fun tearDown() {
    }
}