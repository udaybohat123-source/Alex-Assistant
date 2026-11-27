package com.alex.assistant.core.model

import java.util.UUID

enum class AssistantStatus(val label: String) {
    SLEEPING("Sleeping"),
    LISTENING("Listening"),
    THINKING("Thinking"),
    EXECUTING("Executing"),
    SPEAKING("Speaking"),
    OFFLINE("Offline")
}

enum class LanguagePreference(val displayName: String, val localeTag: String) {
    AUTO("Auto (EN / HI / Hinglish)", "en-IN"),
    ENGLISH("English", "en-IN"),
    HINDI("Hindi", "hi-IN"),
    HINGLISH("Hinglish", "en-IN")
}

enum class MessageRole {
    USER,
    ALEX,
    SYSTEM
}

data class ExecutionStep(
    val stepNumber: Int,
    val title: String,
    val detail: String,
    val verified: Boolean
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val steps: List<ExecutionStep> = emptyList(),
    val requiresConfirmation: Boolean = false,
    val pendingActionId: String? = null
)

data class PendingConfirmation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetAppOrContact: String,
    val payload: String,
    val actionType: String
)

data class DeviceTelemetry(
    val batteryPercent: Int = 0,
    val isCharging: Boolean = false,
    val mediaVolumePercent: Int = 50,
    val isOnline: Boolean = true,
    val isFlashlightOn: Boolean = false,
    val availableStorageGb: Float = 0f
)

data class PermissionStatus(
    val key: String,
    val title: String,
    val rationale: String,
    val isGranted: Boolean,
    val isSpecialSystemSetting: Boolean = false
)

data class MemoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val content: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
