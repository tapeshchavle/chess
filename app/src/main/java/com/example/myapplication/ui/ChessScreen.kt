package com.example.myapplication.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.game.GameState
import com.example.myapplication.game.Move
import com.example.myapplication.game.Piece
import com.example.myapplication.game.PieceColor
import com.example.myapplication.game.PieceType
import com.example.myapplication.game.Position
import java.util.concurrent.TimeUnit
import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun ChessScreen(viewModel: ChessViewModel, onExit: () -> Unit) {
    val gameState by viewModel.gameState.collectAsState()
    val selectedPosition by viewModel.selectedPosition.collectAsState()
    val validMoves by viewModel.validMoves.collectAsState()
    val isComputerThinking by viewModel.isComputerThinking.collectAsState()
    
    val context = LocalContext.current
    var lastMoveCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameState.moveHistory.size) {
        if (gameState.moveHistory.size > lastMoveCount) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 1.0f)
            lastMoveCount = gameState.moveHistory.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Quit", color = Color.White)
            }
            GameStatusHeader(gameState)
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Opponent Timer (Black)
        PlayerCard(
            playerName = "Opponent",
            isActive = gameState.currentTurn == PieceColor.BLACK && gameState.isGameStarted,
            timeMs = gameState.blackTimeMs,
            isBottom = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isComputerThinking) {
            Text("Thinking...", color = Color(0xFFAAAAAA), fontSize = 14.sp)
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The Board
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            ChessBoardWithLabels(
                gameState = gameState,
                selectedPosition = selectedPosition,
                validMoves = validMoves,
                onSquareClicked = { viewModel.onSquareClicked(it) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Player Timer (White)
        PlayerCard(
            playerName = "You",
            isActive = gameState.currentTurn == PieceColor.WHITE && gameState.isGameStarted,
            timeMs = gameState.whiteTimeMs,
            isBottom = true
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        if (gameState.isCheckmate || gameState.isStalemate || gameState.isTimeOut) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Game Over", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                text = {
                    val resultText = when {
                        gameState.isTimeOut -> "Time Out! ${if (gameState.currentTurn == PieceColor.WHITE) "Black" else "White"} wins!"
                        gameState.isCheckmate -> "Checkmate! ${if (gameState.currentTurn == PieceColor.WHITE) "Black" else "White"} wins!"
                        else -> "Stalemate! It's a draw."
                    }
                    Text(resultText, fontSize = 18.sp)
                },
                confirmButton = {
                    Button(onClick = { viewModel.resetGame() }) {
                        Text("Play Again")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onExit) {
                        Text("Exit to Menu")
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerCard(playerName: String, isActive: Boolean, timeMs: Long, isBottom: Boolean) {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isBottom) "👤" else "🤖", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(playerName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Box(
            modifier = Modifier
                .background(
                    if (isActive) Color(0xFFE8F5E9) else Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                color = if (isActive) Color(0xFF2E7D32) else Color(0xFFAAAAAA),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun GameStatusHeader(gameState: GameState) {
    val statusText = when {
        gameState.isTimeOut -> "Time Out"
        gameState.isCheckmate -> "Checkmate"
        gameState.isStalemate -> "Draw"
        gameState.isCheck -> "Check!"
        else -> "Playing"
    }

    Text(
        text = statusText,
        color = Color(0xFFAAAAAA),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun ChessBoardWithLabels(
    gameState: GameState,
    selectedPosition: Position?,
    validMoves: List<Move>,
    onSquareClicked: (Position) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val squareSizeDp = maxWidth / 8

        // 1. Draw Squares and Background Elements
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..7) {
                        val pos = Position(row, col)
                        val isLightSquare = (row + col) % 2 == 0
                        val squareColor = if (isLightSquare) Color(0xFFEBEBEB) else Color(0xFF739552)
                        
                        val isSelected = pos == selectedPosition
                        val isValidMove = validMoves.any { it.to == pos }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isSelected) Color(0xFFB9CA43) else squareColor)
                                .clickable { onSquareClicked(pos) },
                            contentAlignment = Alignment.Center
                        ) {
                            // Rank label (on the a-file, col == 0)
                            if (col == 0) {
                                Text(
                                    text = (8 - row).toString(),
                                    color = if (isLightSquare) Color(0xFF739552) else Color(0xFFEBEBEB),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 4.dp, top = 2.dp)
                                )
                            }
                            
                            // File label (on the 1st rank, row == 7)
                            if (row == 7) {
                                val files = listOf("a", "b", "c", "d", "e", "f", "g", "h")
                                Text(
                                    text = files[col],
                                    color = if (isLightSquare) Color(0xFF739552) else Color(0xFFEBEBEB),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 4.dp, bottom = 0.dp)
                                )
                            }

                            if (isValidMove) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0x66000000), shape = RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 2. Draw Pieces as overlays with animated offsets
        for ((pos, piece) in gameState.board) {
            key(piece.id) {
                val targetOffsetX = squareSizeDp * pos.col
                val targetOffsetY = squareSizeDp * pos.row
                
                val animatedOffsetX by animateDpAsState(
                    targetValue = targetOffsetX, 
                    animationSpec = tween(durationMillis = 300),
                    label = "x"
                )
                val animatedOffsetY by animateDpAsState(
                    targetValue = targetOffsetY, 
                    animationSpec = tween(durationMillis = 300),
                    label = "y"
                )

                Box(
                    modifier = Modifier
                        .size(squareSizeDp)
                        .offset(x = animatedOffsetX, y = animatedOffsetY),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = piece.getSymbol(),
                        fontSize = 38.sp,
                        color = if (piece.isWhite) Color(0xFFFFFDD0) else Color(0xFF2C2C2C)
                    )
                }
            }
        }
    }
}

fun Piece.getSymbol(): String {
    // Use the solid (filled) unicode characters for all pieces,
    // we will rely on the Text color to distinguish White vs Black.
    return when (type) {
        PieceType.KING -> "♚"
        PieceType.QUEEN -> "♛"
        PieceType.ROOK -> "♜"
        PieceType.BISHOP -> "♝"
        PieceType.KNIGHT -> "♞"
        PieceType.PAWN -> "♟"
    }
}
