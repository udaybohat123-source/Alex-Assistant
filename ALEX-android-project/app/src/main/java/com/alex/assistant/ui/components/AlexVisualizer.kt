package com.alex.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.assistant.core.model.AssistantStatus
import com.alex.assistant.ui.theme.AlexColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AlexVisualizer(
    status: AssistantStatus,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "alex_core")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    AssistantStatus.THINKING, AssistantStatus.EXECUTING -> 2200
                    AssistantStatus.LISTENING, AssistantStatus.SPEAKING -> 4000
                    else -> 9000
                },
                easing = LinearEasing
            )
        ),
        label = "ring_rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_pulse"
    )

    val activeColor = when (status) {
        AssistantStatus.LISTENING -> AlexColors.CyanGlow
        AssistantStatus.THINKING, AssistantStatus.EXECUTING -> AlexColors.ElectricBlue
        AssistantStatus.SPEAKING -> Color(0xFF38BDF8)
        AssistantStatus.OFFLINE -> AlexColors.WarningAmber
        AssistantStatus.SLEEPING -> AlexColors.ElectricBlue.copy(alpha = 0.6f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(230.dp)
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.34f * pulse + (audioLevel * 18f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeColor.copy(alpha = 0.24f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.65f
                ),
                radius = baseRadius * 1.65f,
                center = center
            )

            drawCircle(
                color = activeColor.copy(alpha = 0.35f),
                radius = baseRadius * 1.18f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            drawArc(
                color = activeColor,
                startAngle = rotation,
                sweepAngle = 210f,
                useCenter = false,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = AlexColors.CyanGlow.copy(alpha = 0.7f),
                startAngle = -rotation * 1.4f,
                sweepAngle = 110f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Reactive voice harmonic ticks
            val tickCount = 32
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((i * (360.0 / tickCount)) + rotation * 0.25)
                val innerR = baseRadius * 0.85f
                val amp = if (status == AssistantStatus.LISTENING || status == AssistantStatus.SPEAKING) {
                    (audioLevel.coerceAtLeast(0.18f)) * 16f * ((i % 3) + 1)
                } else {
                    4f
                }
                val outerR = innerR + amp
                drawLine(
                    color = activeColor.copy(alpha = 0.65f),
                    start = Offset(
                        center.x + (innerR * cos(angleRad)).toFloat(),
                        center.y + (innerR * sin(angleRad)).toFloat()
                    ),
                    end = Offset(
                        center.x + (outerR * cos(angleRad)).toFloat(),
                        center.y + (outerR * sin(angleRad)).toFloat()
                    ),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ALEX",
                color = AlexColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp
            )
            Text(
                text = status.label,
                color = activeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
