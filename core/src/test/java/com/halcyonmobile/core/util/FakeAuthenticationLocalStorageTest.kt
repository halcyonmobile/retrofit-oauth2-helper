package com.halcyonmobile.core.util

import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorageTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FakeAuthenticationLocalStorageTest : AuthenticationLocalStorageTest<FakeAuthenticationLocalStorage>() {

    @Before
    fun setup() {
        sut = FakeAuthenticationLocalStorage()
    }

    @Test
    fun clearCountIsZeroByDefault() {
        Assert.assertEquals(0, sut.clearCount)
    }

    @Test
    fun callingClearIncreasesCount() {
        val expected = 2

        repeat(expected) {
            sut.clear()
        }

        Assert.assertEquals(2, sut.clearCount)
    }
}