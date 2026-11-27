package com.alex.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.assistant.core.model.AssistantStatus
import com.alex.assistant.core.model.MessageRole
import com.alex.assistant.ui.AlexUiState
import com.alex.assistant.ui.components.AlexVisualizer
import com.alex.assistant.ui.theme.AlexColors

@Composable
fun HomeScreen(
    uiState: AlexUiState,
    onInputChanged: (String) -> Unit,
    onSendCommand: (String) -> Unit,
    onToggleMic: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val quickActions = listOf(
        "Battery status?",
        "Flashlight on",
        "Set alarm 6 AM",
        "Open Wi-Fi settings",
        "Aaj ka schedule bata"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlexColors.DeepBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top HUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ALEX",
                    color = AlexColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${if (uiState.telemetry.isOnline) "Connected" else "Offline"} · Battery ${uiState.telemetry.batteryPercent}%",
                    color = AlexColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Row {
                IconButton(onClick = onOpenMemory) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Memories", tint = AlexColors.TextSecondary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = AlexColors.TextSecondary)
                }
            }
        }

        // Center Circular Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            AlexVisualizer(
                status = uiState.status,
                audioLevel = uiState.audioLevel
            )
        }

        // Conversation History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                val isUser = msg.role == MessageRole.USER
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isUser) AlexColors.ElectricBlue else AlexColors.CharcoalSurface)
                            .border(
                                1.dp,
                                if (isUser) AlexColors.ElectricBlue else AlexColors.HairlineBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = AlexColors.TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Quick Actions Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(quickActions) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AlexColors.CharcoalSurface)
                        .border(1.dp, AlexColors.HairlineBorder, RoundedCornerShape(10.dp))
                        .clickable { onSendCommand(prompt) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = prompt, color = AlexColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Voice & Text Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = onInputChanged,
                placeholder = { Text("Ask Alex in English, Hindi, or Hinglish...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AlexColors.ElectricBlue,
                    unfocusedBorderColor = AlexColors.HairlineBorder,
                    focusedTextColor = AlexColors.TextPrimary,
                    unfocusedTextColor = AlexColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onSendCommand(uiState.inputText) },
                modifier = Modifier
                    .size(48.dp)
                    .background(AlexColors.CharcoalSurface, CircleShape)
                    .border(1.dp, AlexColors.HairlineBorder, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AlexColors.TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            val isListening = uiState.status == AssistantStatus.LISTENING
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isListening) AlexColors.CyanGlow else AlexColors.ElectricBlue,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Speak to ALEX",
                    tint = if (isListening) AlexColors.DeepBlack else AlexColors.TextPrimary
                )
            }
        }
    }
}
