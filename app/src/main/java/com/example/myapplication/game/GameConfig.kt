package com.example.myapplication.game

enum class GameMode {
    PVP, PVC
}

enum class Difficulty(val depth: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3)
}

data class GameConfig(
    val mode: GameMode = GameMode.PVP,
    val difficulty: Difficulty = Difficulty.MEDIUM
)
