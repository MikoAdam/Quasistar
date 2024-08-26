package com.quasistar.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quasistar.logic.GameLogic
import com.quasistar.model.CellState
import com.quasistar.model.Player
import com.quasistar.settings.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun GameScreen(onWin: (Player) -> Unit, onBack: () -> Unit) {
    var board by remember { mutableStateOf(GameLogic.initialBoard()) }
    var currentPlayer by remember { mutableStateOf(Player.ONE) }
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var possibleMoves by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var moveCount by remember { mutableIntStateOf(0) }
    val gameOfLifeProbability = remember { mutableFloatStateOf(0.1f) }
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showLabels by remember { mutableStateOf(false) } // Default to false to avoid flashing
    var winningCondition by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        scope.launch {
            showLabels = SettingsManager.getShowLabels(context)
            winningCondition = SettingsManager.getWinningCondition(context)
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = "Confirm Exit") },
            text = { Text(text = "Are you sure you want to quit and go back to the main menu?") },
            confirmButton = {
                Button(onClick = {
                    showExitDialog = false
                    onBack()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    BackHandler {
        showExitDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF003C46), Color(0xFFEABF6A))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF003C46), RoundedCornerShape(16.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                if (showLabels) {
                    // Row for column labels (top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, end = 40.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(8) { col ->
                            Text(
                                text = ('A' + col).toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                for (row in 0 until 12) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (showLabels) {
                            Text(
                                text = (row + 1).toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .width(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }

                        for (col in 0 until 8) {
                            Cell(
                                cellState = board[row][col],
                                isHighlighted = (row to col) in possibleMoves,
                                isActivePlayer = board[row][col].player == currentPlayer,
                                onClick = {
                                    if (selectedPiece == row to col) {
                                        selectedPiece = null
                                        possibleMoves = listOf()
                                    } else if (selectedPiece == null && board[row][col].player == currentPlayer) {
                                        selectedPiece = row to col
                                        possibleMoves = GameLogic.calculatePossibleMoves(board, row, col)
                                    } else if (selectedPiece != null && (row to col) in possibleMoves) {
                                        board = GameLogic.movePiece(board, selectedPiece!!, row to col)
                                        currentPlayer =
                                            if (currentPlayer == Player.ONE) Player.TWO else Player.ONE
                                        selectedPiece = null
                                        possibleMoves = listOf()
                                        moveCount++
                                        GameLogic.updateGameOfLifeProbability(
                                            context,
                                            board,
                                            moveCount,
                                            gameOfLifeProbability.floatValue
                                        )
                                        GameLogic.checkWinCondition(board, winningCondition, onWin)
                                    } else if (selectedPiece != null && board[row][col].player == currentPlayer) {
                                        selectedPiece = row to col
                                        possibleMoves = GameLogic.calculatePossibleMoves(board, row, col)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (showLabels) {
                            Text(
                                text = (row + 1).toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .width(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }

                if (showLabels) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Row for column labels (bottom)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, end = 40.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(8) { col ->
                            Text(
                                text = ('A' + col).toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(
    cellState: CellState,
    isHighlighted: Boolean,
    isActivePlayer: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isHighlighted || isActivePlayer) 1.15f else 1f, label = "")
    val alpha by animateFloatAsState(if (cellState.player == Player.NONE) 0f else 1f, label = "")

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .scale(scale)
            .background(
                color = when {
                    isHighlighted -> Color(0xFFF6D743) // Light yellow-green for highlight
                    cellState.isProtected -> Color(0xFFE4D5A6) // Warm beige for protected zones
                    else -> Color(0xFFF6F2ED) // Soft background for regular cells
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = alpha > 0f,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut(targetScale = 0.5f) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(if (isActivePlayer) 40.dp else 32.dp)
                    .background(
                        color = when (cellState.player) {
                            Player.ONE -> Color(0xFFFF6F61) // Bright coral for Player One
                            Player.TWO -> Color(0xFF017374) // Deep turquoise for Player Two
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}
