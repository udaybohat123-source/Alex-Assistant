package com.alex.assistant.di

import android.content.Context
import com.alex.assistant.ai.AiEngine
import com.alex.assistant.automation.AutomationEngine
import com.alex.assistant.memory.AlexDatabase
import com.alex.assistant.memory.MemoryEngine
import com.alex.assistant.notifications.NotificationEngine
import com.alex.assistant.permissions.PermissionsManager
import com.alex.assistant.scheduler.TaskScheduler
import com.alex.assistant.settings.SettingsManager
import com.alex.assistant.voice.VoiceEngine
import com.alex.assistant.wakeword.WakeWordEngine

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsManager: SettingsManager by lazy { SettingsManager(appContext) }
    val permissionsManager: PermissionsManager by lazy { PermissionsManager(appContext) }
    val database: AlexDatabase by lazy { AlexDatabase.build(appContext) }
    val memoryEngine: MemoryEngine by lazy { MemoryEngine(database.memoryDao()) }
    val automationEngine: AutomationEngine by lazy { AutomationEngine(appContext, permissionsManager) }
    val voiceEngine: VoiceEngine by lazy { VoiceEngine(appContext, settingsManager) }
    val wakeWordEngine: WakeWordEngine by lazy { WakeWordEngine(settingsManager) }
    val notificationEngine: NotificationEngine by lazy { NotificationEngine(appContext) }
    val taskScheduler: TaskScheduler by lazy { TaskScheduler(appContext) }
    val aiEngine: AiEngine by lazy { AiEngine(automationEngine, memoryEngine) }
}
