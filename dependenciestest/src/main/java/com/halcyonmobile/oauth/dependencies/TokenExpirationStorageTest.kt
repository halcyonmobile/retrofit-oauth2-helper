package com.halcyonmobile.oauth.dependencies

import org.junit.Assert
import org.junit.Test

abstract class TokenExpirationStorageTest<T: TokenExpirationStorage> {

    protected lateinit var sut: T

    @Test
    fun defaultValueIsCorrect() {
        Assert.assertEquals(Long.MIN_VALUE, sut.accessTokenExpiresAt)
    }

    @Test
    fun settingValueSavesIt() {
        val expected = 5000L
        sut.accessTokenExpiresAt = expected

        Assert.assertEquals(expected, sut.accessTokenExpiresAt)
    }

    @Test
    fun clearSetsDefault() {
        sut.accessTokenExpiresAt = 5000L

        sut.clear()

        Assert.assertEquals(Long.MIN_VALUE, sut.accessTokenExpiresAt)
    }
}