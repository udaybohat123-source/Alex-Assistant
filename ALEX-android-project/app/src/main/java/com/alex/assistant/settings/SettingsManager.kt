package com.alex.assistant.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alex.assistant.core.model.LanguagePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.alexDataStore: DataStore<Preferences> by preferencesDataStore(name = "alex_settings")

data class AlexSettings(
    val onboardingCompleted: Boolean = false,
    val wakePhrase: String = "Alex",
    val backgroundListeningEnabled: Boolean = false,
    val continuousConversation: Boolean = true,
    val conversationTimeoutSeconds: Int = 15,
    val speechRate: Float = 1.02f,
    val languagePreference: LanguagePreference = LanguagePreference.AUTO,
    val proactiveBriefingsEnabled: Boolean = true,
    val confirmSensitiveActions: Boolean = true
)

class SettingsManager(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val WAKE_PHRASE = stringPreferencesKey("wake_phrase")
        val BACKGROUND_LISTENING = booleanPreferencesKey("background_listening")
        val CONTINUOUS_CONVERSATION = booleanPreferencesKey("continuous_conversation")
        val CONVERSATION_TIMEOUT = intPreferencesKey("conversation_timeout")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val LANGUAGE = stringPreferencesKey("language_preference")
        val PROACTIVE_BRIEFINGS = booleanPreferencesKey("proactive_briefings")
        val CONFIRM_SENSITIVE = booleanPreferencesKey("confirm_sensitive")
    }

    val settingsFlow: Flow<AlexSettings> = context.alexDataStore.data.map { prefs ->
        val langName = prefs[Keys.LANGUAGE] ?: LanguagePreference.AUTO.name
        val lang = runCatching { LanguagePreference.valueOf(langName) }.getOrDefault(LanguagePreference.AUTO)
        AlexSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            wakePhrase = prefs[Keys.WAKE_PHRASE] ?: "Alex",
            backgroundListeningEnabled = prefs[Keys.BACKGROUND_LISTENING] ?: false,
            continuousConversation = prefs[Keys.CONTINUOUS_CONVERSATION] ?: true,
            conversationTimeoutSeconds = prefs[Keys.CONVERSATION_TIMEOUT] ?: 15,
            speechRate = prefs[Keys.SPEECH_RATE] ?: 1.02f,
            languagePreference = lang,
            proactiveBriefingsEnabled = prefs[Keys.PROACTIVE_BRIEFINGS] ?: true,
            confirmSensitiveActions = prefs[Keys.CONFIRM_SENSITIVE] ?: true
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.alexDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setBackgroundListening(enabled: Boolean) {
        context.alexDataStore.edit { it[Keys.BACKGROUND_LISTENING] = enabled }
    }

    suspend fun setLanguagePreference(pref: LanguagePreference) {
        context.alexDataStore.edit { it[Keys.LANGUAGE] = pref.name }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.alexDataStore.edit { it[Keys.SPEECH_RATE] = rate.coerceIn(0.6f, 1.6f) }
    }

    suspend fun setWakePhrase(phrase: String) {
        context.alexDataStore.edit { it[Keys.WAKE_PHRASE] = phrase.ifBlank { "Alex" } }
    }

    suspend fun setProactiveBriefings(enabled: Boolean) {
        context.alexDataStore.edit { it[Keys.PROACTIVE_BRIEFINGS] = enabled }
    }
}
