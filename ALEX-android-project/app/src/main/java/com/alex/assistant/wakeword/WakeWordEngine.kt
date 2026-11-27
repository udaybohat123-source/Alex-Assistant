package com.alex.assistant.wakeword

import com.alex.assistant.settings.SettingsManager
import kotlinx.coroutines.flow.first

/**
 * Local, privacy-preserving wake-phrase detector gate.
 * Does NOT stream continuous microphone audio to cloud servers.
 */
class WakeWordEngine(private val settingsManager: SettingsManager) {

    suspend fun matchesWakePhrase(transcript: String): Boolean {
        val phrase = settingsManager.settingsFlow.first().wakePhrase.trim().lowercase()
        val normalized = transcript.trim().lowercase()
        return normalized.startsWith("hey $phrase") ||
            normalized.startsWith("ok $phrase") ||
            normalized.startsWith(phrase)
    }

    suspend fun stripWakePhrase(transcript: String): String {
        val phrase = settingsManager.settingsFlow.first().wakePhrase.trim()
        val regex = Regex("^(hey\\s+|ok\\s+|hi\\s+)?${Regex.escape(phrase)}[,:\\s]*", RegexOption.IGNORE_CASE)
        return transcript.replace(regex, "").trim()
    }
}
