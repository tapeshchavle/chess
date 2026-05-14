package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.game.Difficulty

@Composable
fun SettingsScreen(
    currentDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Difficulty",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Difficulty.values().forEach { diff ->
            val isSelected = currentDifficulty == diff
            Button(
                onClick = { onDifficultySelected(diff) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF7B61FF) else Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(diff.name, fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back", fontSize = 18.sp)
        }
    }
}
