package com.alex.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alex.assistant.core.model.AssistantStatus
import com.alex.assistant.core.model.ChatMessage
import com.alex.assistant.core.model.DeviceTelemetry
import com.alex.assistant.core.model.ExecutionStep
import com.alex.assistant.core.model.LanguagePreference
import com.alex.assistant.core.model.MemoryRecord
import com.alex.assistant.core.model.MessageRole
import com.alex.assistant.core.model.PendingConfirmation
import com.alex.assistant.core.model.PermissionStatus
import com.alex.assistant.di.AppContainer
import com.alex.assistant.settings.AlexSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlexUiState(
    val status: AssistantStatus = AssistantStatus.SLEEPING,
    val messages: List<ChatMessage> = emptyList(),
    val telemetry: DeviceTelemetry = DeviceTelemetry(),
    val pendingConfirmation: PendingConfirmation? = null,
    val audioLevel: Float = 0f,
    val inputText: String = "",
    val settings: AlexSettings = AlexSettings(),
    val permissions: List<PermissionStatus> = emptyList(),
    val memories: List<MemoryRecord> = emptyList()
)

class AlexViewModel(private val container: AppContainer) : ViewModel() {

    private val _status = MutableStateFlow(AssistantStatus.SLEEPING)
    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                role = MessageRole.ALEX,
                text = "Good evening. I'm ALEX — ready when you are. Ask me in English, Hindi, or Hinglish."
            )
        )
    )
    private val _telemetry = MutableStateFlow(container.automationEngine.readTelemetry())
    private val _pendingConfirmation = MutableStateFlow<PendingConfirmation?>(null)
    private val _inputText = MutableStateFlow("")

    val uiState: StateFlow<AlexUiState> = combine(
        _status,
        _messages,
        _telemetry,
        _pendingConfirmation,
        container.voiceEngine.audioLevel,
        _inputText,
        container.settingsManager.settingsFlow,
        container.permissionsManager.permissions,
        container.memoryEngine.observeMemories()
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AlexUiState(
            status = values[0] as AssistantStatus,
            messages = values[1] as List<ChatMessage>,
            telemetry = values[2] as DeviceTelemetry,
            pendingConfirmation = values[3] as PendingConfirmation?,
            audioLevel = values[4] as Float,
            inputText = values[5] as String,
            settings = values[6] as AlexSettings,
            permissions = values[7] as List<PermissionStatus>,
            memories = values[8] as List<MemoryRecord>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlexUiState())

    init {
        viewModelScope.launch {
            container.voiceEngine.isSpeaking.collect { speaking ->
                if (speaking) {
                    _status.value = AssistantStatus.SPEAKING
                } else if (_status.value == AssistantStatus.SPEAKING) {
                    _status.value = AssistantStatus.SLEEPING
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun refreshTelemetryAndPermissions() {
        _telemetry.value = container.automationEngine.readTelemetry()
        container.permissionsManager.refreshPermissions()
        if (!_telemetry.value.isOnline) {
            _status.value = AssistantStatus.OFFLINE
        }
    }

    fun toggleVoiceInput() {
        if (container.voiceEngine.isListening.value) {
            container.voiceEngine.stopListening()
            _status.value = AssistantStatus.SLEEPING
            return
        }
        _status.value = AssistantStatus.LISTENING
        container.voiceEngine.startListening(
            onResult = { spoken ->
                sendCommand(spoken)
            },
            onError = { errMsg ->
                _status.value = AssistantStatus.SLEEPING
                appendMessage(MessageRole.SYSTEM, errMsg)
            }
        )
    }

    fun sendCommand(rawInput: String = _inputText.value) {
        val text = rawInput.trim()
        if (text.isEmpty()) return
        _inputText.value = ""
        appendMessage(MessageRole.USER, text)

        viewModelScope.launch {
            _status.value = AssistantStatus.THINKING
            val cleaned = if (container.wakeWordEngine.matchesWakePhrase(text)) {
                container.wakeWordEngine.stripWakePhrase(text).ifBlank {
                    "Yes, I'm here. How can I help?"
                }
            } else {
                text
            }

            if (cleaned == "Yes, I'm here. How can I help?") {
                appendMessage(MessageRole.ALEX, cleaned)
                container.voiceEngine.speak(cleaned)
                return@launch
            }

            val history = _messages.value
                .filter { it.role != MessageRole.SYSTEM }
                .chunked(2)
                .mapNotNull { pair ->
                    if (pair.size == 2) pair[0].text to pair[1].text else null
                }

            val result = container.aiEngine.processUserCommand(cleaned, history)
            _status.value = AssistantStatus.EXECUTING
            refreshTelemetryAndPermissions()
            appendMessage(MessageRole.ALEX, result.replyText, result.steps)
            container.voiceEngine.speak(result.replyText)
        }
    }

    fun confirmPendingAction() {
        val pending = _pendingConfirmation.value ?: return
        _pendingConfirmation.value = null
        val res = when (pending.actionType) {
            "DIAL" -> container.automationEngine.openDialer(pending.targetAppOrContact)
            else -> container.automationEngine.openInstalledApp(pending.targetAppOrContact)
        }
        appendMessage(
            MessageRole.ALEX,
            res.message,
            listOf(ExecutionStep(7, "Verify Result", res.verifiedAction, res.success))
        )
        container.voiceEngine.speak(res.message)
    }

    fun cancelPendingAction() {
        _pendingConfirmation.value = null
        appendMessage(MessageRole.ALEX, "Cancelled. Nothing was sent or executed.")
    }

    fun completeOnboarding() {
        viewModelScope.launch { container.settingsManager.setOnboardingCompleted(true) }
    }

    fun setLanguagePreference(pref: LanguagePreference) {
        viewModelScope.launch { container.settingsManager.setLanguagePreference(pref) }
    }

    fun setBackgroundListening(enabled: Boolean) {
        viewModelScope.launch { container.settingsManager.setBackgroundListening(enabled) }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { container.settingsManager.setSpeechRate(rate) }
    }

    fun saveMemory(category: String, content: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            container.memoryEngine.saveMemory(category, content)
                .onFailure { onError(it.message ?: "Unable to save memory.") }
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch { container.memoryEngine.deleteMemory(id) }
    }

    private fun appendMessage(
        role: MessageRole,
        text: String,
        steps: List<ExecutionStep> = emptyList()
    ) {
        _messages.value = _messages.value + ChatMessage(role = role, text = text, steps = steps)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AlexViewModel(container) as T
            }
        }
    }
}
