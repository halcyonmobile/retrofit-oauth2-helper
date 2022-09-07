package com.halcyonmobile.core.util

import com.halcyonmobile.oauth.dependencies.TokenExpirationStorageTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeTokenExpirationStorageTest : TokenExpirationStorageTest<FakeTokenExpirationStorage>() {

    @Before
    fun setup() {
        sut = FakeTokenExpirationStorage()
    }

    @Test
    fun clearCountIsZeroByDefault() {
        assertEquals(0, sut.clearCount)
    }

    @Test
    fun callingClearIncreasesCount() {
        val expected = 2

        repeat(expected) {
            sut.clear()
        }

        assertEquals(2, sut.clearCount)
    }
}