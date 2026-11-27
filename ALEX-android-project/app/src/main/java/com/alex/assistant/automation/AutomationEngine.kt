package com.alex.assistant.automation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import com.alex.assistant.core.model.DeviceTelemetry
import com.alex.assistant.permissions.PermissionsManager
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AutomationResult(
    val success: Boolean,
    val verifiedAction: String,
    val message: String,
    val fallbackOpenedSettings: Boolean = false
)

class AutomationEngine(
    private val context: Context,
    private val permissionsManager: PermissionsManager
) {
    private var torchEnabled = false

    fun readTelemetry(): DeviceTelemetry {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val volPct = (currentVol * 100) / maxVol

        val statFs = StatFs(Environment.getDataDirectory().path)
        val availableGb = (statFs.availableBytes / (1024f * 1024f * 1024f))

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            mediaVolumePercent = volPct,
            isOnline = isNetworkAvailable(),
            isFlashlightOn = torchEnabled,
            availableStorageGb = availableGb
        )
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun openInstalledApp(appNameQuery: String): AutomationResult {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val apps = pm.queryIntentActivities(launcherIntent, 0)
        val match = apps.firstOrNull { resolveInfo ->
            val label = resolveInfo.loadLabel(pm).toString()
            label.contains(appNameQuery, ignoreCase = true)
        } ?: return AutomationResult(
            success = false,
            verifiedAction = "PackageManager.queryIntentActivities",
            message = "Could not find an installed application matching '$appNameQuery'."
        )

        val launchIntent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return AutomationResult(
            success = false,
            verifiedAction = "PackageManager.getLaunchIntentForPackage",
            message = "Application '${match.loadLabel(pm)}' cannot be launched directly."
        )

        context.startActivity(launchIntent)
        return AutomationResult(
            success = true,
            verifiedAction = "Launched ${match.activityInfo.packageName}",
            message = "Opened ${match.loadLabel(pm)}."
        )
    }

    fun openSystemSettings(target: String): AutomationResult {
        val action = when (target.lowercase(Locale.ROOT)) {
            "wifi", "wi-fi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "battery" -> Intent.ACTION_POWER_USAGE_SUMMARY
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            AutomationResult(
                success = true,
                verifiedAction = "Started $action",
                message = "Opened $target settings.",
                fallbackOpenedSettings = true
            )
        }.getOrElse { err ->
            AutomationResult(
                success = false,
                verifiedAction = action,
                message = "Unable to open $target settings: ${err.message}"
            )
        }
    }

    fun setMediaVolumePercent(percent: Int): AutomationResult {
        val clamped = percent.coerceIn(0, 100)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = ((clamped / 100f) * maxVol).toInt().coerceIn(0, maxVol)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, AudioManager.FLAG_SHOW_UI)
        val verifiedVol = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100) / maxVol.coerceAtLeast(1)
        return AutomationResult(
            success = true,
            verifiedAction = "AudioManager.STREAM_MUSIC=$verifiedVol%",
            message = "Media volume set to $verifiedVol%."
        )
    }

    fun toggleFlashlight(enable: Boolean): AutomationResult {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return AutomationResult(false, "CameraFlashCheck", "This device does not have a camera flash.")
        }
        if (!permissionsManager.hasPermission(Manifest.permission.CAMERA)) {
            return AutomationResult(false, "PermissionCheck(CAMERA)", "Camera permission is required to control the flashlight.")
        }
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return AutomationResult(false, "CameraManager", "No flash unit found.")

        return runCatching {
            cameraManager.setTorchMode(cameraId, enable)
            torchEnabled = enable
            AutomationResult(true, "CameraManager.setTorchMode($enable)", if (enable) "Flashlight turned on." else "Flashlight turned off.")
        }.getOrElse { err ->
            AutomationResult(false, "CameraManager.setTorchMode", "Flashlight control failed: ${err.message}")
        }
    }

    fun setAlarm(hour: Int, minute: Int, label: String): AutomationResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour.coerceIn(0, 23))
            putExtra(AlarmClock.EXTRA_MINUTES, minute.coerceIn(0, 59))
            putExtra(AlarmClock.EXTRA_MESSAGE, label.ifBlank { "ALEX Alarm" })
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            AutomationResult(true, "AlarmClock.ACTION_SET_ALARM", "Alarm scheduled for %02d:%02d.".format(hour, minute))
        }.getOrElse { err ->
            AutomationResult(false, "AlarmClock.ACTION_SET_ALARM", "Could not open alarm app: ${err.message}")
        }
    }

    fun openDialer(phoneNumber: String): AutomationResult {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return AutomationResult(true, "Intent.ACTION_DIAL", "Opened phone dialer for $phoneNumber.")
    }

    fun openWebSearch(query: String): AutomationResult {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return AutomationResult(true, "Intent.ACTION_VIEW", "Opened Google search for '$query'.")
    }

    fun readCurrentDateTime(): String {
        val fmt = SimpleDateFormat("EEEE, d MMM yyyy, h:mm a", Locale.getDefault())
        return fmt.format(Date())
    }
}
