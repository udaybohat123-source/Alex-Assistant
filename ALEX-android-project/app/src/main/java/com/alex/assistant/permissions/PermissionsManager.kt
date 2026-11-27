package com.alex.assistant.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alex.assistant.core.model.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsManager(private val context: Context) {

    private val _permissions = MutableStateFlow<List<PermissionStatus>>(emptyList())
    val permissions: StateFlow<List<PermissionStatus>> = _permissions.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val list = mutableListOf(
            PermissionStatus(
                key = Manifest.permission.RECORD_AUDIO,
                title = "Microphone Access",
                rationale = "Required for voice commands, wake-word detection, and real-time voice conversations. Never records silently.",
                isGranted = hasPermission(Manifest.permission.RECORD_AUDIO)
            ),
            PermissionStatus(
                key = Manifest.permission.READ_CONTACTS,
                title = "Contacts Access",
                rationale = "Allows ALEX to match spoken names like 'Rahul' or 'Mummy' accurately before initiating calls or messages.",
                isGranted = hasPermission(Manifest.permission.READ_CONTACTS)
            ),
            PermissionStatus(
                key = Manifest.permission.CAMERA,
                title = "Camera / Flashlight Control",
                rationale = "Required on Android to toggle the device torch/flashlight and launch camera intents.",
                isGranted = hasPermission(Manifest.permission.CAMERA)
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(
                PermissionStatus(
                    key = Manifest.permission.POST_NOTIFICATIONS,
                    title = "Notifications",
                    rationale = "Displays the persistent microphone privacy indicator and proactive briefings.",
                    isGranted = hasPermission(Manifest.permission.POST_NOTIFICATIONS)
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            list.add(
                PermissionStatus(
                    key = "exact_alarm",
                    title = "Alarms & Reminders",
                    rationale = "Schedules exact alarms and time-critical user reminders reliably.",
                    isGranted = alarmManager.canScheduleExactAlarms(),
                    isSpecialSystemSetting = true
                )
            )
        }

        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        list.add(
            PermissionStatus(
                key = "notification_listener",
                title = "Notification Summary Access",
                rationale = "Optional. Enables ALEX to summarize unread WhatsApp or system notifications when asked.",
                isGranted = enabledListeners.contains(context.packageName),
                isSpecialSystemSetting = true
            )
        )

        _permissions.value = list
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
