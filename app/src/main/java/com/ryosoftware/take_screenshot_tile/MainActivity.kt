package com.ryosoftware.take_screenshot_tile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorScheme = if (isSystemInDarkTheme()) {
                darkColorScheme(primary = Color(0xFFEF6C00), onPrimary = Color.White)
            } else {
                lightColorScheme(primary = Color(0xFFEF6C00), onPrimary = Color.White)
            }
            MaterialTheme(colorScheme = colorScheme) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen() {
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isAccessibilityGranted by remember { mutableStateOf(MainAccessibilityService.isAccessibilityServiceEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityGranted = MainAccessibilityService.isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val savedDelay = ApplicationPreferences.getLong(
        context,
        ApplicationPreferences.DELAY_BEFORE_TAKE_SCREENSHOT_KEY,
        ApplicationPreferences.DELAY_BEFORE_TAKE_SCREENSHOT_DEFAULT
    )
    var delayValue by remember { mutableFloatStateOf(savedDelay.toFloat()) }

    val savedVibrate = ApplicationPreferences.getBoolean(
        context,
        ApplicationPreferences.VIBRATE_ON_CAPTURE_KEY,
        true
    )
    var vibrateEnabled by remember { mutableStateOf(savedVibrate) }

    val savedTileActiveBg = ApplicationPreferences.getBoolean(
        context,
        ApplicationPreferences.TILE_ACTIVE_BACKGROUND_KEY,
        true
    )
    var tileActiveBg by remember { mutableStateOf(savedTileActiveBg) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(stringResource(R.string.information)) },
            text = { Text(stringResource(R.string.added_tile_info)) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(R.string.accept_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.information))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TilePreview(
                isEnabled = isAccessibilityGranted,
                onClick = {
                    MainAccessibilityService.takeScreenshot(context)
                }
            )

            Spacer(Modifier.height(24.dp))

            AccessibilityServiceSection(
                isGranted = isAccessibilityGranted,
                onRequestPermission = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )

            Spacer(Modifier.height(24.dp))

            DelaySection(
                currentDelay = delayValue.toLong(),
                onDelayChange = { newValue ->
                    delayValue = newValue
                    ApplicationPreferences.putLong(
                        context,
                        ApplicationPreferences.DELAY_BEFORE_TAKE_SCREENSHOT_KEY,
                        newValue.toLong()
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            VibrationSection(
                enabled = vibrateEnabled,
                onEnabledChange = { newValue ->
                    vibrateEnabled = newValue
                    ApplicationPreferences.putBoolean(
                        context,
                        ApplicationPreferences.VIBRATE_ON_CAPTURE_KEY,
                        newValue
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            TileBackgroundSection(
                enabled = tileActiveBg,
                onEnabledChange = { newValue ->
                    tileActiveBg = newValue
                    ApplicationPreferences.putBoolean(
                        context,
                        ApplicationPreferences.TILE_ACTIVE_BACKGROUND_KEY,
                        newValue
                    )
                    MainTileService.requestListeningState(context)
                }
            )

            Spacer(Modifier.weight(1f))

            val repoUrl = stringResource(R.string.github_repo)

            Text(
                text = stringResource(R.string.app_version_title, BuildConfig.versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl)))
                }
            )
        }
    }
}

@Composable
private fun TilePreview(isEnabled: Boolean, onClick: () -> Unit) {
    val disabledAlpha = if (isEnabled) 1f else 0.38f

    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable(enabled = isEnabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(if (isEnabled) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_screenshot),
                    contentDescription = stringResource(R.string.take_screenshot),
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = disabledAlpha),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.take_screenshot),
                fontSize = 14.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
            )
        }
    }
}

@Composable
private fun AccessibilityServiceSection(isGranted: Boolean, onRequestPermission: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.accessibility_service_title),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.accessibility_service_description),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGranted
        ) {
            Text(stringResource(R.string.allow_accessibility_service_permission))
        }
    }
}

@Composable
private fun DelaySection(currentDelay: Long, onDelayChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.delay_before_start_screen_capture_title),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.delay_before_start_screen_capture_description),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = currentDelay.toFloat(),
                onValueChange = onDelayChange,
                valueRange = 0f..5000f,
                steps = 50,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.milliseconds_value, currentDelay.toInt()),
                fontSize = 14.sp,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun VibrationSection(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.vibrate_on_capture_title),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.vibrate_on_capture_description),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TileBackgroundSection(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.tile_active_background_title),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }

        Text(
            text = stringResource(R.string.tile_active_background_description),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

