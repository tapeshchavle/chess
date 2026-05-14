package com.example.myapplication.game

data class Position(val row: Int, val col: Int) {
    fun isValid() = row in 0..7 && col in 0..7
}
