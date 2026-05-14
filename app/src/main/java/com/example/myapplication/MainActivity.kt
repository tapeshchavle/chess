package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.ChessScreen
import com.example.myapplication.ui.ChessViewModel
import com.example.myapplication.ui.SettingsScreen
import com.example.myapplication.ui.StartScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: ChessViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "start") {
                        composable("start") {
                            StartScreen(
                                onStartGame = { config ->
                                    viewModel.setGameConfig(config)
                                    navController.navigate("game")
                                },
                                onNavigateSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            val config by viewModel.gameConfig.collectAsState()
                            SettingsScreen(
                                currentDifficulty = config.difficulty,
                                onDifficultySelected = { diff ->
                                    viewModel.setDifficulty(diff)
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("game") {
                            ChessScreen(
                                viewModel = viewModel,
                                onExit = {
                                    viewModel.resetGame()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}