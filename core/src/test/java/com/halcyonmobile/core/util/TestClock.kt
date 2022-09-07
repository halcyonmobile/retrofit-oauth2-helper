package com.halcyonmobile.core.util

import com.halcyonmobile.oauth.internal.Clock

class TestClock(var value: Long) : Clock {

    override fun currentTimeMillis(): Long = value
}