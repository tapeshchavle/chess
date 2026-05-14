package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.game.ChessAI
import com.example.myapplication.game.ChessEngine
import com.example.myapplication.game.GameConfig
import com.example.myapplication.game.GameMode
import com.example.myapplication.game.GameState
import com.example.myapplication.game.Move
import com.example.myapplication.game.PieceColor
import com.example.myapplication.game.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChessViewModel : ViewModel() {
    private val engine = ChessEngine()
    private val ai = ChessAI(engine)

    private var timerJob: kotlinx.coroutines.Job? = null

    private val _gameConfig = MutableStateFlow(GameConfig())
    val gameConfig: StateFlow<GameConfig> = _gameConfig.asStateFlow()

    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedPosition = MutableStateFlow<Position?>(null)
    val selectedPosition: StateFlow<Position?> = _selectedPosition.asStateFlow()

    private val _validMoves = MutableStateFlow<List<Move>>(emptyList())
    val validMoves: StateFlow<List<Move>> = _validMoves.asStateFlow()
    
    private val _isComputerThinking = MutableStateFlow(false)
    val isComputerThinking: StateFlow<Boolean> = _isComputerThinking.asStateFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _gameState.value
                if (!state.isGameStarted || state.isCheckmate || state.isStalemate || state.isTimeOut) continue
                
                var wTime = state.whiteTimeMs
                var bTime = state.blackTimeMs
                var timeOut = false
                
                if (state.currentTurn == PieceColor.WHITE) {
                    wTime -= 1000
                    if (wTime <= 0) {
                        wTime = 0
                        timeOut = true
                    }
                } else {
                    bTime -= 1000
                    if (bTime <= 0) {
                        bTime = 0
                        timeOut = true
                    }
                }
                
                _gameState.value = state.copy(
                    whiteTimeMs = wTime,
                    blackTimeMs = bTime,
                    isTimeOut = timeOut
                )
            }
        }
    }

    fun setGameConfig(config: GameConfig) {
        _gameConfig.value = config
        resetGame()
    }

    fun setDifficulty(difficulty: com.example.myapplication.game.Difficulty) {
        _gameConfig.value = _gameConfig.value.copy(difficulty = difficulty)
    }

    fun onSquareClicked(position: Position) {
        if (_isComputerThinking.value) return
        
        val state = _gameState.value
        val config = _gameConfig.value
        
        if (state.isCheckmate || state.isStalemate || state.isTimeOut) return

        // In PVC mode, ignore clicks when it's the computer's turn (Black)
        if (config.mode == GameMode.PVC && state.currentTurn == PieceColor.BLACK) return

        val selected = _selectedPosition.value

        if (selected == null) {
            val piece = state.board[position]
            if (piece != null && piece.color == state.currentTurn) {
                _selectedPosition.value = position
                _validMoves.value = engine.generateValidMoves(state, position)
            }
        } else {
            val move = _validMoves.value.find { it.to == position }
            if (move != null) {
                applyMoveAndCheckAI(state, move, config)
            } else {
                val piece = state.board[position]
                if (piece != null && piece.color == state.currentTurn) {
                    _selectedPosition.value = position
                    _validMoves.value = engine.generateValidMoves(state, position)
                } else {
                    _selectedPosition.value = null
                    _validMoves.value = emptyList()
                }
            }
        }
    }
    
    private fun applyMoveAndCheckAI(state: GameState, move: Move, config: GameConfig) {
        val nextState = engine.applyMove(state, move).copy(isGameStarted = true)
        _gameState.value = nextState
        _selectedPosition.value = null
        _validMoves.value = emptyList()

        if (config.mode == GameMode.PVC && nextState.currentTurn == PieceColor.BLACK && !nextState.isCheckmate && !nextState.isStalemate) {
            triggerAIMove(nextState, config)
        }
    }

    private fun triggerAIMove(state: GameState, config: GameConfig) {
        _isComputerThinking.value = true
        viewModelScope.launch {
            delay(1000) // 1 second delay
            val bestMove = withContext(Dispatchers.Default) {
                ai.getBestMove(state, config.difficulty)
            }
            if (bestMove != null) {
                val nextState = engine.applyMove(state, bestMove).copy(isGameStarted = true)
                _gameState.value = nextState
            }
            _isComputerThinking.value = false
        }
    }

    fun resetGame() {
        _gameState.value = GameState.initial()
        _selectedPosition.value = null
        _validMoves.value = emptyList()
        _isComputerThinking.value = false
    }
}
