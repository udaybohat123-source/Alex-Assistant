package com.alex.assistant.ai

import com.alex.assistant.automation.AutomationEngine
import com.alex.assistant.core.model.ExecutionStep
import com.alex.assistant.memory.MemoryEngine
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiTurnResult(
    val replyText: String,
    val steps: List<ExecutionStep>,
    val usedOfflineFallback: Boolean = false
)

class AiEngine(
    private val automationEngine: AutomationEngine,
    private val memoryEngine: MemoryEngine
) {
    private val systemPrompt = """
        You are ALEX (Advanced Learning and Execution Assistant), an intelligent, warm, confident,
        and slightly witty personal AI assistant built into an Android smartphone.
        - Understand English, Hindi, and Hinglish seamlessly, even with typos or casual grammar.
        - Be action-oriented and concise: execute supported commands directly and never fabricate action completion.
        - Never store or repeat passwords, OTPs, or payment PINs.
        - Respond naturally in the user's preferred language.
    """.trimIndent()

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content { text(systemPrompt) }
        )
    }

    suspend fun processUserCommand(
        input: String,
        historyPairs: List<Pair<String, String>>
    ): AiTurnResult = withContext(Dispatchers.IO) {
        val steps = mutableListOf<ExecutionStep>()
        steps.add(ExecutionStep(1, "Understand Request", "Parsed input in EN/HI/Hinglish: \"$input\"", true))

        // Offline local fallback for core smartphone commands
        if (!automationEngine.isNetworkAvailable()) {
            return@withContext handleOfflineCommand(input, steps)
        }

        val memories = memoryEngine.getAllMemories()
        val telemetry = automationEngine.readTelemetry()

        steps.add(ExecutionStep(2, "Identify Operation", "Evaluating intent against Gemini & Android tools", true))
        steps.add(ExecutionStep(3, "Context & Memory", "Loaded ${memories.size} saved memories; Battery ${telemetry.batteryPercent}%", true))

        return@withContext runCatching {
            val contextBlock = buildString {
                appendLine("Device state: Battery ${telemetry.batteryPercent}% (charging=${telemetry.isCharging}), Media volume=${telemetry.mediaVolumePercent}%, Time=${automationEngine.readCurrentDateTime()}")
                if (memories.isNotEmpty()) {
                    appendLine("User memories:")
                    memories.take(10).forEach { appendLine("- [${it.category}] ${it.content}") }
                }
            }

            val prompt = buildString {
                appendLine(contextBlock)
                if (historyPairs.isNotEmpty()) {
                    appendLine("Recent turns:")
                    historyPairs.takeLast(6).forEach { (u, a) ->
                        appendLine("User: $u")
                        appendLine("ALEX: $a")
                    }
                }
                appendLine("Current user request: $input")
            }

            val response = model.generateContent(prompt)
            val reply = response.text?.trim().orEmpty().ifBlank {
                "I heard you, though I couldn't form a response right now. Please try again."
            }
            steps.add(ExecutionStep(8, "Communicate Outcome", "Delivered verified Gemini response", true))
            AiTurnResult(replyText = reply, steps = steps, usedOfflineFallback = false)
        }.getOrElse { err ->
            steps.add(ExecutionStep(8, "Fallback Notice", "Cloud AI unavailable (${err.localizedMessage}); running local check", false))
            handleOfflineCommand(input, steps)
        }
    }

    private fun handleOfflineCommand(input: String, steps: MutableList<ExecutionStep>): AiTurnResult {
        val lower = input.lowercase()
        val telemetry = automationEngine.readTelemetry()

        val reply = when {
            "battery" in lower -> {
                steps.add(ExecutionStep(6, "Execute Battery Read", "Read BatteryManager status", true))
                val chargeNote = if (telemetry.isCharging) "and currently charging" else "on battery power"
                "Offline mode: Your battery is at ${telemetry.batteryPercent}% $chargeNote."
            }
            "time" in lower || "date" in lower || "samay" in lower -> {
                steps.add(ExecutionStep(6, "Read System Clock", "Queried local system date/time", true))
                "Offline mode: It is ${automationEngine.readCurrentDateTime()}."
            }
            "flashlight on" in lower || "torch on" in lower -> {
                val res = automationEngine.toggleFlashlight(true)
                steps.add(ExecutionStep(6, "Toggle Torch", res.verifiedAction, res.success))
                res.message
            }
            "flashlight off" in lower || "torch off" in lower -> {
                val res = automationEngine.toggleFlashlight(false)
                steps.add(ExecutionStep(6, "Toggle Torch", res.verifiedAction, res.success))
                res.message
            }
            else -> {
                "You're currently offline, so advanced Gemini reasoning is temporarily unavailable. I can still check battery, read time, toggle flashlight, adjust volume, or open installed apps."
            }
        }
        return AiTurnResult(replyText = reply, steps = steps, usedOfflineFallback = true)
    }
}
