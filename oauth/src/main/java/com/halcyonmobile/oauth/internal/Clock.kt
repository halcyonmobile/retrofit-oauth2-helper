package com.halcyonmobile.oauth.internal

interface Clock {

    object ProductionClock : Clock {
        override fun currentTimeMillis() = System.currentTimeMillis()
    }

    fun currentTimeMillis(): Long

    companion object {

        private var instance: Clock = ProductionClock

        fun get() = instance

        fun swap(clock: Clock) {
            instance = clock
        }

        fun reset() {
            swap(ProductionClock)
        }

    }
}

fun Clock() = Clock.get()
