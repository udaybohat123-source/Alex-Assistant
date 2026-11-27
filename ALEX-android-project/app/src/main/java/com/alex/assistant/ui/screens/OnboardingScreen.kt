package com.alex.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.assistant.core.model.LanguagePreference
import com.alex.assistant.ui.theme.AlexColors

@Composable
fun OnboardingScreen(
    selectedLanguage: LanguagePreference,
    onSelectLanguage: (LanguagePreference) -> Unit,
    onContinueToPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlexColors.DeepBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ALEX",
                color = AlexColors.CyanGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Advanced Learning & Execution Assistant",
                color = AlexColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "A calm, bilingual operating layer for your Android phone. Understands English, Hindi, and Hinglish, remembers your preferences, and executes supported phone actions transparently.",
                color = AlexColors.TextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Preferred Voice & Conversation Mode",
                color = AlexColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            LanguagePreference.entries.forEach { option ->
                val isSelected = option == selectedLanguage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) AlexColors.ElevatedSurface else AlexColors.CharcoalSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AlexColors.ElectricBlue else AlexColors.HairlineBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectLanguage(option) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = option.displayName, color = AlexColors.TextPrimary, fontSize = 14.sp)
                    Text(
                        text = if (isSelected) "Selected" else "Choose",
                        color = if (isSelected) AlexColors.CyanGlow else AlexColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Column {
            Text(
                text = "Privacy pledge: ALEX never records audio silently, never stores passwords or OTPs, and asks for confirmation before sending messages or placing calls.",
                color = AlexColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinueToPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlexColors.ElectricBlue)
            ) {
                Text("Review Permissions & Continue", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
