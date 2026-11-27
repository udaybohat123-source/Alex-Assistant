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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.alex.assistant.core.model.PermissionStatus
import com.alex.assistant.ui.theme.AlexColors

@Composable
fun PermissionsScreen(
    permissions: List<PermissionStatus>,
    onRequestPermission: (PermissionStatus) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlexColors.DeepBlack)
            .padding(20.dp)
    ) {
        Text(
            text = "System Permissions",
            color = AlexColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Grant only what you want ALEX to use. Restricted actions automatically fall back to Android system settings.",
            color = AlexColors.TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(permissions, key = { it.key }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AlexColors.CharcoalSurface, RoundedCornerShape(16.dp))
                        .border(1.dp, AlexColors.HairlineBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            color = AlexColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (item.isGranted) "Granted" else "Not granted",
                            color = if (item.isGranted) AlexColors.NominalGreen else AlexColors.WarningAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.rationale,
                        color = AlexColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    if (!item.isGranted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onRequestPermission(item) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (item.isSpecialSystemSetting) "Open System Settings" else "Grant Permission",
                                color = AlexColors.CyanGlow,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlexColors.ElectricBlue)
        ) {
            Text("Enter ALEX Console", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
