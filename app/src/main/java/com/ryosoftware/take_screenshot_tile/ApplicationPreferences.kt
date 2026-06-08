package com.ryosoftware.take_screenshot_tile

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.properties.Delegates
import androidx.core.content.edit

object ApplicationPreferences {
    const val DELAY_BEFORE_TAKE_SCREENSHOT_KEY = "delay-before-take-screenshot"
    const val VIBRATE_ON_CAPTURE_KEY = "vibrate-on-capture"
    const val TILE_ACTIVE_BACKGROUND_KEY = "tile-active-background"
    var DELAY_BEFORE_TAKE_SCREENSHOT_DEFAULT: Long by Delegates.notNull()
    private const val VERSION_KEY = "version"
    private const val VERSION_VALUE = 1.0f

    @SuppressLint("CommitPrefEdits")
    private fun upgrade(context: Context, preferences: SharedPreferences, from: Float): SharedPreferences.Editor? = null

    private fun initializeConstants(context: Context) {
        DELAY_BEFORE_TAKE_SCREENSHOT_DEFAULT = context.getString(R.string.delay_before_take_screenshot_default).toLong()
    }

    fun initialize(context: Context) {
        val preferences = getPreferences(context)
        initializeConstants(context)
        if (preferences.getFloat(VERSION_KEY, 0f) != VERSION_VALUE) {
            var editor: SharedPreferences.Editor? = null
            if (preferences.getFloat(VERSION_KEY, 0f) < VERSION_VALUE) {
                editor = upgrade(context, preferences, preferences.getFloat(VERSION_KEY, 0f))
            }
            if (editor == null) editor = preferences.edit()
            editor!!.putFloat(VERSION_KEY, VERSION_VALUE)
            editor.apply()
        }
    }

    fun getPreferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getBoolean(context: Context, name: String, defaultValue: Boolean): Boolean = getPreferences(context).getBoolean(name, defaultValue)

    fun putBoolean(context: Context, name: String, value: Boolean) = getPreferences(context).edit { putBoolean(name, value) }

    fun getInteger(context: Context, name: String, defaultValue: Int): Int = getPreferences(context).getInt(name, defaultValue)

    fun putInteger(context: Context, name: String, value: Int) = getPreferences(context).edit { putInt(name, value) }

    fun getLong(context: Context, name: String, defaultValue: Long): Long = getPreferences(context).getLong(name, defaultValue)

    fun putLong(context: Context, name: String, value: Long) = getPreferences(context).edit { putLong(name, value) }

    fun getString(context: Context, name: String, defaultValue: String?): String? = getPreferences(context).getString(name, defaultValue)

    fun putString(context: Context, name: String, value: String) = getPreferences(context).edit { putString(name, value) }

    fun getStrings(context: Context, name: String, defaultValues: Set<String>?): Set<String>? = getPreferences(context).getStringSet(name, defaultValues)

    fun putStrings(context: Context, name: String, values: Set<String>) = getPreferences(context).edit { putStringSet(name, values) }

    fun getFloat(context: Context, name: String, defaultValue: Float): Float = getPreferences(context).getFloat(name, defaultValue)

    fun putFloat(context: Context, name: String, value: Float) = getPreferences(context).edit { putFloat(name, value) }

    fun hasKey(context: Context, name: String): Boolean = getPreferences(context).contains(name)

    fun removeKey(context: Context, name: String) = getPreferences(context).edit { remove(name) }

    fun getPreferencesVersion(context: Context): String = getPreferences(context).getFloat(VERSION_KEY, VERSION_VALUE).toString()
}
