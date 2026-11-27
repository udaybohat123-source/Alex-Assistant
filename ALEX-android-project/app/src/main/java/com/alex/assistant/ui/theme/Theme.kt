package com.alex.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AlexColors {
    val DeepBlack = Color(0xFF07090D)
    val CharcoalSurface = Color(0xFF0F141C)
    val ElevatedSurface = Color(0xFF161D28)
    val HairlineBorder = Color(0xFF232C3B)
    val ElectricBlue = Color(0xFF2F6FED)
    val CyanGlow = Color(0xFF22D3EE)
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val NominalGreen = Color(0xFF10B981)
    val WarningAmber = Color(0xFFF59E0B)
    val AlertRed = Color(0xFFEF4444)
}

private val AlexDarkScheme = darkColorScheme(
    primary = AlexColors.ElectricBlue,
    secondary = AlexColors.CyanGlow,
    background = AlexColors.DeepBlack,
    surface = AlexColors.CharcoalSurface,
    surfaceVariant = AlexColors.ElevatedSurface,
    onPrimary = Color.White,
    onSecondary = AlexColors.DeepBlack,
    onBackground = AlexColors.TextPrimary,
    onSurface = AlexColors.TextPrimary,
    onSurfaceVariant = AlexColors.TextSecondary,
    outline = AlexColors.HairlineBorder
)

@Composable
fun AlexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlexDarkScheme,
        content = content
    )
}
