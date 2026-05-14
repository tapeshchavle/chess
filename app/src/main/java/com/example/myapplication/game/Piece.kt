package com.example.myapplication.game

import java.util.UUID

enum class PieceColor { WHITE, BLACK }

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }

data class Piece(val type: PieceType, val color: PieceColor, val id: String = UUID.randomUUID().toString()) {
    val isWhite get() = color == PieceColor.WHITE
    val isBlack get() = color == PieceColor.BLACK
}
