package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.game.GameConfig
import com.example.myapplication.game.GameMode

@Composable
fun StartScreen(
    onStartGame: (GameConfig) -> Unit,
    onNavigateSettings: () -> Unit
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
            text = androidx.compose.ui.res.stringResource(com.example.myapplication.R.string.app_name),
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = { onStartGame(GameConfig(mode = GameMode.PVP)) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Play 1v1", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onStartGame(GameConfig(mode = GameMode.PVC)) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Play Computer", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateSettings,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Computer Settings", fontSize = 18.sp)
        }
    }
}
