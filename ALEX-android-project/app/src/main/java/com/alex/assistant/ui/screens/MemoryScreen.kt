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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.assistant.core.model.MemoryRecord
import com.alex.assistant.ui.theme.AlexColors

@Composable
fun MemoryScreen(
    memories: List<MemoryRecord>,
    onSaveMemory: (category: String, content: String, onError: (String) -> Unit) -> Unit,
    onDeleteMemory: (String) -> Unit,
    onBack: () -> Unit
) {
    var draft by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlexColors.DeepBlack)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AlexColors.TextPrimary)
            }
            Text("Personal Memory", color = AlexColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = draft,
            onValueChange = {
                draft = it
                errorMsg = null
            },
            placeholder = { Text("Add a preference or detail to remember...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        errorMsg?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(it, color = AlexColors.AlertRed, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                onSaveMemory("Preference", draft) { err -> errorMsg = err }
                if (errorMsg == null && draft.isNotBlank()) draft = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = AlexColors.ElectricBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Memory")
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(memories, key = { it.id }) { mem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AlexColors.CharcoalSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, AlexColors.HairlineBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mem.category, color = AlexColors.CyanGlow, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(mem.content, color = AlexColors.TextPrimary, fontSize = 14.sp)
                    }
                    IconButton(onClick = { onDeleteMemory(mem.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlexColors.TextSecondary)
                    }
                }
            }
        }
    }
}
