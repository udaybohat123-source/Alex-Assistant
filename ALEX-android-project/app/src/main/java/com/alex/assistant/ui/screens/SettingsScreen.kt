package com.alex.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.assistant.settings.AlexSettings
import com.alex.assistant.ui.theme.AlexColors

@Composable
fun SettingsScreen(
    settings: AlexSettings,
    onToggleBackgroundListening: (Boolean) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onOpenPermissions: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlexColors.DeepBlack)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AlexColors.TextPrimary)
            }
            Text("Assistant Settings", color = AlexColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AlexColors.CharcoalSurface, RoundedCornerShape(16.dp))
                .border(1.dp, AlexColors.HairlineBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Background Wake Listening", color = AlexColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Runs transparent foreground service with persistent microphone notification.",
                    color = AlexColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = settings.backgroundListeningEnabled,
                onCheckedChange = onToggleBackgroundListening
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AlexColors.CharcoalSurface, RoundedCornerShape(16.dp))
                .border(1.dp, AlexColors.HairlineBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Voice Speaking Speed (${String.format("%.2f", settings.speechRate)}x)",
                color = AlexColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = settings.speechRate,
                onValueChange = onSpeechRateChange,
                valueRange = 0.7f..1.5f
            )
        }

        OutlinedButton(
            onClick = onOpenPermissions,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Manage System Permissions", color = AlexColors.CyanGlow)
        }
    }
}
