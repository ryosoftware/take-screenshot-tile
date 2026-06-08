package com.ryosoftware.take_screenshot_tile

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("AccessibilityPolicy")
class MainAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onServiceConnected() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            Log.d(BuildConfig.TAG, "Received event '$action'")
            when (action) {
                ACTION_TAKE_SCREENSHOT -> {
                    val delay = intent.getLongExtra(EXTRA_DELAY, 0L)
                    val vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, false)

                    val actionBlock: () -> Unit = {
                        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                        if (vibrate) vibrate()
                    }

                    if (delay > 0) {
                        scope.launch {
                            Log.d(BuildConfig.TAG, "Waiting for $delay ms before taking screenshot")
                            delay(delay)
                            actionBlock()
                        }
                    } else {
                        actionBlock()
                    }
                }
                ACTION_DISMISS_NOTIFICATION_SHADE -> {
                    performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_TAKE_SCREENSHOT = BuildConfig.APPLICATION_ID + ".TAKE_SCREENSHOT"
        private const val ACTION_DISMISS_NOTIFICATION_SHADE = BuildConfig.APPLICATION_ID + ".DISMISS_NOTIFICATION_SHADE"
        private const val EXTRA_DELAY = "delay"
        private const val EXTRA_VIBRATE = "vibrate"

        fun takeScreenshot(context: Context) {
            val delay = ApplicationPreferences.getLong(
                context,
                ApplicationPreferences.DELAY_BEFORE_TAKE_SCREENSHOT_KEY,
                ApplicationPreferences.DELAY_BEFORE_TAKE_SCREENSHOT_DEFAULT
            )
            val vibrate = ApplicationPreferences.getBoolean(
                context,
                ApplicationPreferences.VIBRATE_ON_CAPTURE_KEY,
                true
            )
            context.startService(
                Intent(context, MainAccessibilityService::class.java)
                    .setAction(ACTION_TAKE_SCREENSHOT)
                    .putExtra(EXTRA_DELAY, delay)
                    .putExtra(EXTRA_VIBRATE, vibrate)
            )
        }

        fun dismissNotificationShade(context: Context) {
            context.startService(
                Intent(context, MainAccessibilityService::class.java)
                    .setAction(ACTION_DISMISS_NOTIFICATION_SHADE)
            )
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val pref = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return pref.contains("${context.packageName}/${MainAccessibilityService::class.java.name}")
        }
    }
}
