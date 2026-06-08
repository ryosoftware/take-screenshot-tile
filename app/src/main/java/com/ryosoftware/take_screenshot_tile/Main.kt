package com.ryosoftware.take_screenshot_tile

import android.app.Application

class Main : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        ApplicationPreferences.initialize(this)
    }

    companion object {
        lateinit var instance: Main
            private set
    }
}
