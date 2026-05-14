package com.example.myapplication.game

class ChessAI(private val engine: ChessEngine) {

    fun getBestMove(state: GameState, difficulty: Difficulty): Move? {
        val depth = difficulty.depth
        var bestMove: Move? = null
        var bestScore = Int.MIN_VALUE
        
        val validMoves = engine.getValidMovesForPlayer(state, state.currentTurn)
        if (validMoves.isEmpty()) return null

        for (move in validMoves) {
            val newState = engine.applyMove(state, move)
            val score = minimax(newState, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false, state.currentTurn)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        
        // Fallback to random if something goes wrong
        return bestMove ?: validMoves.random()
    }

    private fun minimax(
        state: GameState,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        aiColor: PieceColor
    ): Int {
        if (depth == 0 || state.isCheckmate || state.isStalemate) {
            return evaluate(state, aiColor)
        }

        var newAlpha = alpha
        var newBeta = beta

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            val moves = engine.getValidMovesForPlayer(state, state.currentTurn)
            for (move in moves) {
                val newState = engine.applyMove(state, move)
                val eval = minimax(newState, depth - 1, newAlpha, newBeta, false, aiColor)
                maxEval = maxOf(maxEval, eval)
                newAlpha = maxOf(newAlpha, eval)
                if (newBeta <= newAlpha) break // Beta cutoff
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            val moves = engine.getValidMovesForPlayer(state, state.currentTurn)
            for (move in moves) {
                val newState = engine.applyMove(state, move)
                val eval = minimax(newState, depth - 1, newAlpha, newBeta, true, aiColor)
                minEval = minOf(minEval, eval)
                newBeta = minOf(newBeta, eval)
                if (newBeta <= newAlpha) break // Alpha cutoff
            }
            return minEval
        }
    }

    private fun evaluate(state: GameState, aiColor: PieceColor): Int {
        if (state.isCheckmate) {
            return if (state.currentTurn == aiColor) -10000 else 10000
        }
        if (state.isStalemate) {
            return 0
        }

        var score = 0
        for ((_, piece) in state.board) {
            val value = getPieceValue(piece.type)
            if (piece.color == aiColor) {
                score += value
            } else {
                score -= value
            }
        }
        return score
    }

    private fun getPieceValue(type: PieceType): Int {
        return when (type) {
            PieceType.PAWN -> 10
            PieceType.KNIGHT -> 30
            PieceType.BISHOP -> 30
            PieceType.ROOK -> 50
            PieceType.QUEEN -> 90
            PieceType.KING -> 900
        }
    }
}
