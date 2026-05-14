package com.example.myapplication.game

data class GameState(
    val board: Map<Position, Piece> = emptyMap(),
    val currentTurn: PieceColor = PieceColor.WHITE,
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false,
    val isTimeOut: Boolean = false,
    val isGameStarted: Boolean = false,
    val moveHistory: List<Move> = emptyList(),
    // Castling rights
    val whiteKingMoved: Boolean = false,
    val blackKingMoved: Boolean = false,
    val whiteKingsideRookMoved: Boolean = false,
    val whiteQueensideRookMoved: Boolean = false,
    val blackKingsideRookMoved: Boolean = false,
    val blackQueensideRookMoved: Boolean = false,
    // Timers
    val whiteTimeMs: Long = 10 * 60 * 1000L,
    val blackTimeMs: Long = 10 * 60 * 1000L
) {
    companion object {
        fun initial(): GameState {
            val board = mutableMapOf<Position, Piece>()
            
            // Pawns
            for (col in 0..7) {
                board[Position(1, col)] = Piece(PieceType.PAWN, PieceColor.BLACK)
                board[Position(6, col)] = Piece(PieceType.PAWN, PieceColor.WHITE)
            }
            
            // Major pieces
            val backRank = listOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )
            
            for (col in 0..7) {
                board[Position(0, col)] = Piece(backRank[col], PieceColor.BLACK)
                board[Position(7, col)] = Piece(backRank[col], PieceColor.WHITE)
            }
            
            return GameState(board = board)
        }
    }
}
