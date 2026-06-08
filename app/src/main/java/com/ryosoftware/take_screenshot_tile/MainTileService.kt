package com.ryosoftware.take_screenshot_tile

import android.content.ComponentName
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class MainTileService : TileService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(BuildConfig.TAG, "Tile Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(BuildConfig.TAG, "Tile Service Destroy")
    }

    override fun onTileAdded() {
        super.onTileAdded()
        requestListeningState(this, ComponentName(packageName, MainTileService::class.java.name))
        Log.d(BuildConfig.TAG, "Tile Added")
    }

    override fun onTileRemoved() {
        Log.d(BuildConfig.TAG, "Tile Removed")
        super.onTileRemoved()
    }

    override fun onStartListening() {
        super.onStartListening()
        val allowActive = ApplicationPreferences.getBoolean(this, ApplicationPreferences.TILE_ACTIVE_BACKGROUND_KEY, true)
        qsTile?.state =
            if (MainAccessibilityService.isAccessibilityServiceEnabled(this)) {
                if (allowActive) Tile.STATE_ACTIVE
                else Tile.STATE_INACTIVE
            }
            else Tile.STATE_UNAVAILABLE
        qsTile?.updateTile()
        Log.d(BuildConfig.TAG, "Tile Start Listening")
    }

    override fun onStopListening() {
        Log.d(BuildConfig.TAG, "Tile Stop Listening")
        super.onStopListening()
    }

    override fun onClick() {
        if (MainAccessibilityService.isAccessibilityServiceEnabled(this)) {
            MainAccessibilityService.dismissNotificationShade(this)
            MainAccessibilityService.takeScreenshot(this)
        }
    }
}
