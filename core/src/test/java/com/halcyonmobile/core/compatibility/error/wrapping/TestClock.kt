package com.halcyonmobile.core.compatibility.error.wrapping

import com.halcyonmobile.oauth.internal.Clock

class TestClock(var value: Long) : Clock {

    override fun currentTimeMillis(): Long = value
}