package com.halcyonmobile.oauth.dependencies

import org.junit.Assert
import org.junit.Test

abstract class AuthenticationLocalStorageTest<T : AuthenticationLocalStorage> {

    protected lateinit var sut: T

    @Test
    fun defaultValuesAreCorrect() {
        Assert.assertEquals("", sut.userId)
        Assert.assertEquals("", sut.accessToken)
        Assert.assertEquals("", sut.tokenType)
        Assert.assertEquals("", sut.refreshToken)
    }

    @Test
    fun settingUserIdIsSaved() {
        val expected = "alma"
        sut.userId = expected

        Assert.assertEquals(expected, sut.userId)
    }

    @Test
    fun settingAccessTokenIsSaved() {
        val expected = "alma"
        sut.accessToken = expected

        Assert.assertEquals(expected, sut.accessToken)
    }

    @Test
    fun settingTokenTypeIsSaved() {
        val expected = "alma"
        sut.tokenType = expected

        Assert.assertEquals(expected, sut.tokenType)
    }

    @Test
    fun settingRefreshTokenIsSaved() {
        val expected = "alma"
        sut.refreshToken = expected

        Assert.assertEquals(expected, sut.refreshToken)
    }

    @Test
    fun clearClearsEverything() {
        sut.userId = "expected0"
        sut.accessToken = "expected1"
        sut.tokenType = "expected2"
        sut.refreshToken = "expected3"

        sut.clear()

        Assert.assertEquals("", sut.userId)
        Assert.assertEquals("", sut.accessToken)
        Assert.assertEquals("", sut.tokenType)
        Assert.assertEquals("", sut.refreshToken)
    }
}