package com.example.myapplication.game

import kotlin.math.abs

class ChessEngine {

    fun generateValidMoves(state: GameState, position: Position): List<Move> {
        val piece = state.board[position] ?: return emptyList()
        if (piece.color != state.currentTurn) return emptyList()

        val pseudoLegalMoves = generatePseudoLegalMoves(state, position, piece)
        
        // Filter out moves that leave the king in check
        return pseudoLegalMoves.filter { move ->
            val newState = applyMove(state, move, computeCheck = false) // avoid infinite recursion
            !isKingInCheck(newState, piece.color)
        }
    }

    private fun generatePseudoLegalMoves(state: GameState, pos: Position, piece: Piece, includeCastling: Boolean = true): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> generatePawnMoves(state, pos, piece)
            PieceType.KNIGHT -> generateKnightMoves(state, pos, piece)
            PieceType.BISHOP -> generateSlidingMoves(state, pos, piece, bishopDirections)
            PieceType.ROOK -> generateSlidingMoves(state, pos, piece, rookDirections)
            PieceType.QUEEN -> generateSlidingMoves(state, pos, piece, queenDirections)
            PieceType.KING -> generateKingMoves(state, pos, piece, includeCastling)
        }
    }

    private fun generatePawnMoves(state: GameState, pos: Position, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val direction = if (piece.isWhite) -1 else 1
        val startRow = if (piece.isWhite) 6 else 1

        // Forward 1
        val forward1 = Position(pos.row + direction, pos.col)
        if (forward1.isValid() && state.board[forward1] == null) {
            addPawnMove(moves, pos, forward1, piece)
            // Forward 2
            if (pos.row == startRow) {
                val forward2 = Position(pos.row + 2 * direction, pos.col)
                if (state.board[forward2] == null) {
                    moves.add(Move(pos, forward2, piece))
                }
            }
        }

        // Captures
        val captureOffsets = listOf(-1, 1)
        for (offset in captureOffsets) {
            val capturePos = Position(pos.row + direction, pos.col + offset)
            if (capturePos.isValid()) {
                val targetPiece = state.board[capturePos]
                if (targetPiece != null && targetPiece.color != piece.color) {
                    addPawnMove(moves, pos, capturePos, piece, targetPiece)
                } else if (isEnPassant(state, pos, capturePos, piece)) {
                    moves.add(Move(pos, capturePos, piece, Piece(PieceType.PAWN, if(piece.isWhite) PieceColor.BLACK else PieceColor.WHITE), isEnPassant = true))
                }
            }
        }
        return moves
    }

    private fun addPawnMove(moves: MutableList<Move>, from: Position, to: Position, piece: Piece, capturedPiece: Piece? = null) {
        val promotionRow = if (piece.isWhite) 0 else 7
        if (to.row == promotionRow) {
            moves.add(Move(from, to, piece, capturedPiece, promotionTo = PieceType.QUEEN))
            moves.add(Move(from, to, piece, capturedPiece, promotionTo = PieceType.ROOK))
            moves.add(Move(from, to, piece, capturedPiece, promotionTo = PieceType.BISHOP))
            moves.add(Move(from, to, piece, capturedPiece, promotionTo = PieceType.KNIGHT))
        } else {
            moves.add(Move(from, to, piece, capturedPiece))
        }
    }

    private fun isEnPassant(state: GameState, from: Position, to: Position, piece: Piece): Boolean {
        if (state.moveHistory.isEmpty()) return false
        val lastMove = state.moveHistory.last()
        val expectedRow = if (piece.isWhite) 3 else 4
        if (from.row != expectedRow) return false
        
        return lastMove.piece.type == PieceType.PAWN &&
               lastMove.to.row == from.row &&
               lastMove.to.col == to.col &&
               abs(lastMove.from.row - lastMove.to.row) == 2
    }

    private fun generateKnightMoves(state: GameState, pos: Position, piece: Piece): List<Move> {
        val offsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        return offsets.map { Position(pos.row + it.first, pos.col + it.second) }
            .filter { it.isValid() }
            .filter { state.board[it]?.color != piece.color }
            .map { Move(pos, it, piece, state.board[it]) }
    }

    private fun generateSlidingMoves(state: GameState, pos: Position, piece: Piece, directions: List<Pair<Int, Int>>): List<Move> {
        val moves = mutableListOf<Move>()
        for (dir in directions) {
            var currentPos = Position(pos.row + dir.first, pos.col + dir.second)
            while (currentPos.isValid()) {
                val targetPiece = state.board[currentPos]
                if (targetPiece == null) {
                    moves.add(Move(pos, currentPos, piece))
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(Move(pos, currentPos, piece, targetPiece))
                    }
                    break
                }
                currentPos = Position(currentPos.row + dir.first, currentPos.col + dir.second)
            }
        }
        return moves
    }

    private fun generateKingMoves(state: GameState, pos: Position, piece: Piece, includeCastling: Boolean = true): List<Move> {
        val moves = queenDirections.map { Position(pos.row + it.first, pos.col + it.second) }
            .filter { it.isValid() }
            .filter { state.board[it]?.color != piece.color }
            .map { Move(pos, it, piece, state.board[it]) }
            .toMutableList()

        if (!includeCastling) return moves

        // Castling
        val isKingMoved = if (piece.isWhite) state.whiteKingMoved else state.blackKingMoved
        if (!isKingMoved && !isKingInCheck(state, piece.color)) {
            val row = if (piece.isWhite) 7 else 0
            // Kingside
            val isKingsideRookMoved = if (piece.isWhite) state.whiteKingsideRookMoved else state.blackKingsideRookMoved
            if (!isKingsideRookMoved && state.board[Position(row, 5)] == null && state.board[Position(row, 6)] == null) {
                if (!isSquareAttacked(state, Position(row, 5), piece.color) && !isSquareAttacked(state, Position(row, 6), piece.color)) {
                    moves.add(Move(pos, Position(row, 6), piece, isCastling = true))
                }
            }
            // Queenside
            val isQueensideRookMoved = if (piece.isWhite) state.whiteQueensideRookMoved else state.blackQueensideRookMoved
            if (!isQueensideRookMoved && state.board[Position(row, 1)] == null && state.board[Position(row, 2)] == null && state.board[Position(row, 3)] == null) {
                if (!isSquareAttacked(state, Position(row, 2), piece.color) && !isSquareAttacked(state, Position(row, 3), piece.color)) {
                    moves.add(Move(pos, Position(row, 2), piece, isCastling = true))
                }
            }
        }
        return moves
    }

    fun applyMove(state: GameState, move: Move, computeCheck: Boolean = true): GameState {
        val newBoard = state.board.toMutableMap()
        newBoard.remove(move.from)
        
        var pieceToPlace = move.piece
        if (move.promotionTo != null) {
            pieceToPlace = Piece(move.promotionTo, move.piece.color)
        }
        newBoard[move.to] = pieceToPlace

        if (move.isEnPassant) {
            val direction = if (move.piece.isWhite) 1 else -1
            newBoard.remove(Position(move.to.row + direction, move.to.col))
        }

        if (move.isCastling) {
            val row = move.from.row
            if (move.to.col == 6) { // Kingside
                val rook = newBoard.remove(Position(row, 7))!!
                newBoard[Position(row, 5)] = rook
            } else if (move.to.col == 2) { // Queenside
                val rook = newBoard.remove(Position(row, 0))!!
                newBoard[Position(row, 3)] = rook
            }
        }

        val newTurn = if (state.currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        
        var wKingMoved = state.whiteKingMoved
        var bKingMoved = state.blackKingMoved
        var wKRookMoved = state.whiteKingsideRookMoved
        var wQRookMoved = state.whiteQueensideRookMoved
        var bKRookMoved = state.blackKingsideRookMoved
        var bQRookMoved = state.blackQueensideRookMoved

        when (move.piece.type) {
            PieceType.KING -> {
                if (move.piece.isWhite) wKingMoved = true else bKingMoved = true
            }
            PieceType.ROOK -> {
                if (move.piece.isWhite) {
                    if (move.from.row == 7 && move.from.col == 7) wKRookMoved = true
                    if (move.from.row == 7 && move.from.col == 0) wQRookMoved = true
                } else {
                    if (move.from.row == 0 && move.from.col == 7) bKRookMoved = true
                    if (move.from.row == 0 && move.from.col == 0) bQRookMoved = true
                }
            }
            else -> {}
        }

        val nextState = state.copy(
            board = newBoard,
            currentTurn = newTurn,
            moveHistory = state.moveHistory + move,
            whiteKingMoved = wKingMoved,
            blackKingMoved = bKingMoved,
            whiteKingsideRookMoved = wKRookMoved,
            whiteQueensideRookMoved = wQRookMoved,
            blackKingsideRookMoved = bKRookMoved,
            blackQueensideRookMoved = bQRookMoved
        )

        if (!computeCheck) return nextState

        val isCheck = isKingInCheck(nextState, newTurn)
        val hasValidMoves = hasAnyValidMove(nextState, newTurn)

        return nextState.copy(
            isCheck = isCheck,
            isCheckmate = isCheck && !hasValidMoves,
            isStalemate = !isCheck && !hasValidMoves
        )
    }

    private fun isKingInCheck(state: GameState, color: PieceColor): Boolean {
        val kingPos = state.board.entries.find { it.value.type == PieceType.KING && it.value.color == color }?.key ?: return false
        return isSquareAttacked(state, kingPos, color)
    }

    private fun isSquareAttacked(state: GameState, pos: Position, byColor: PieceColor): Boolean {
        val opponentColor = if (byColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for ((enemyPos, piece) in state.board) {
            if (piece.color == opponentColor) {
                // For pawns, we only check attack diagonals, not forward moves
                if (piece.type == PieceType.PAWN) {
                    val direction = if (piece.isWhite) -1 else 1
                    if ((enemyPos.row + direction == pos.row && enemyPos.col - 1 == pos.col) ||
                        (enemyPos.row + direction == pos.row && enemyPos.col + 1 == pos.col)) {
                        return true
                    }
                } else {
                    val moves = generatePseudoLegalMoves(state, enemyPos, piece, includeCastling = false)
                    if (moves.any { it.to == pos }) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getValidMovesForPlayer(state: GameState, color: PieceColor): List<Move> {
        val allMoves = mutableListOf<Move>()
        for ((pos, piece) in state.board) {
            if (piece.color == color) {
                allMoves.addAll(generateValidMoves(state, pos))
            }
        }
        return allMoves
    }

    private fun hasAnyValidMove(state: GameState, color: PieceColor): Boolean {
        for ((pos, piece) in state.board) {
            if (piece.color == color) {
                val moves = generateValidMoves(state, pos)
                if (moves.isNotEmpty()) return true
            }
        }
        return false
    }

    companion object {
        val bishopDirections = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        val rookDirections = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val queenDirections = bishopDirections + rookDirections
    }
}
