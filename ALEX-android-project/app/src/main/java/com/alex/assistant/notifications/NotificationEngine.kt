package com.alex.assistant.notifications

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CapturedNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val body: String,
    val postedAtMillis: Long
)

object NotificationBuffer {
    private val _recent = MutableStateFlow<List<CapturedNotification>>(emptyList())
    val recent: StateFlow<List<CapturedNotification>> = _recent.asStateFlow()

    fun add(item: CapturedNotification) {
        _recent.value = (listOf(item) + _recent.value.filterNot { it.key == item.key }).take(25)
    }

    fun remove(key: String) {
        _recent.value = _recent.value.filterNot { it.key == key }
    }
}

class AlexNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val extras = sbn?.notification?.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val body = extras.getCharSequence("android.text")?.toString().orEmpty()
        if (title.isBlank() && body.isBlank()) return
        NotificationBuffer.add(
            CapturedNotification(
                key = sbn.key,
                packageName = sbn.packageName,
                title = title,
                body = body,
                postedAtMillis = sbn.postTime
            )
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.key?.let { NotificationBuffer.remove(it) }
    }
}

class NotificationEngine(private val context: Context) {
    fun isAccessGranted(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }

    fun getUnreadSummaries(): List<CapturedNotification> {
        if (!isAccessGranted()) return emptyList()
        return NotificationBuffer.recent.value
    }
}
