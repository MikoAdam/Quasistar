package com.quasistar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quasistar.ui.theme.QuasistarTheme
import kotlin.random.Random

enum class Player {
    NONE, BLUE, RED
}

data class CellState(
    val player: Player,
    val isProtected: Boolean
)

sealed class Screen {
    object MainMenu : Screen()
    object Rules : Screen()
    object Game : Screen()
    data class Victory(val winner: Player) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuasistarTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }
    when (currentScreen) {
        is Screen.MainMenu -> MainMenuScreen(onStartGame = { currentScreen = Screen.Game }, onViewRules = { currentScreen = Screen.Rules })
        is Screen.Rules -> RulesScreen(onBack = { currentScreen = Screen.MainMenu })
        is Screen.Game -> GameScreen(onWin = { winner -> currentScreen = Screen.Victory(winner) }, onBack = { currentScreen = Screen.MainMenu })
        is Screen.Victory -> VictoryScreen(winner = (currentScreen as Screen.Victory).winner, onRestart = { currentScreen = Screen.Game }, onBack = { currentScreen = Screen.MainMenu })
    }
}

@Composable
fun MainMenuScreen(onStartGame: () -> Unit, onViewRules: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quasistar", fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStartGame, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Start Game", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onViewRules, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Rules", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("© 2024 Quasistar. All rights reserved.", textAlign = TextAlign.Center, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Follow us:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Facebook | Instagram | YouTube", fontSize = 14.sp, color = Color.Blue, modifier = Modifier.clickable {
            // Add actual social media link actions here
        })
    }
}

@Composable
fun RulesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rules of Quasistar", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("1. The goal is to get 3 of your pieces into the enemy's protected zone.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("2. Pieces can move one block in any direction or jump over pieces if at the edge.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("3. After each move, there is a chance for a 'Game of Life' step to occur, where pieces can spawn or disappear based on their neighbors.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("4. The probability of a Game of Life step starts at 20% after the first 3 rounds and increases by 10% each turn thereafter. The probability resets to 20% after a Game of Life step occurs.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Back", fontSize = 18.sp)
        }
    }
}

@Composable
fun GameScreen(onWin: (Player) -> Unit, onBack: () -> Unit) {
    var board by remember { mutableStateOf(initialBoard()) }
    var currentPlayer by remember { mutableStateOf(Player.BLUE) }
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var possibleMoves by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var moveCount by remember { mutableStateOf(0) }
    var gameOfLifeProbability by remember { mutableStateOf(0.2f) }
    var showExitDialog by remember { mutableStateOf(false) }

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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Current Player: ${if (currentPlayer == Player.BLUE) "Blue" else "Red"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Moves: $moveCount", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Game of Life Step Probability: ${gameOfLifeProbability * 100}%", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        for (row in 0 until 12) {
            Row {
                for (col in 0 until 8) {
                    Cell(
                        cellState = board[row][col],
                        isHighlighted = (row to col) in possibleMoves,
                        onClick = {
                            if (selectedPiece == row to col) {
                                selectedPiece = null
                                possibleMoves = listOf()
                            } else if (selectedPiece == null && board[row][col].player == currentPlayer) {
                                selectedPiece = row to col
                                possibleMoves = calculatePossibleMoves(board, row, col)
                            } else if (selectedPiece != null && (row to col) in possibleMoves) {
                                board = movePiece(board, selectedPiece!!, row to col)
                                currentPlayer = if (currentPlayer == Player.BLUE) Player.RED else Player.BLUE
                                selectedPiece = null
                                possibleMoves = listOf()
                                moveCount++
                                if (moveCount >= 6 && Random.nextFloat() < gameOfLifeProbability) {
                                    board = applyGameOfLifeStep(board)
                                    gameOfLifeProbability = 0.2f
                                } else if (moveCount >= 6) {
                                    gameOfLifeProbability += 0.1f
                                }
                                checkWinCondition(board, onWin)
                            } else if (selectedPiece != null && board[row][col].player == currentPlayer) {
                                selectedPiece = row to col
                                possibleMoves = calculatePossibleMoves(board, row, col)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun Cell(cellState: CellState, isHighlighted: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (isHighlighted) 1.05f else 1f)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .scale(scale)
            .background(
                color = when {
                    isHighlighted -> Color(0x8033CC33) // Slightly transparent green
                    cellState.isProtected -> Color.LightGray
                    else -> Color.White
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cellState.player != Player.NONE) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = when (cellState.player) {
                            Player.BLUE -> Color.Blue
                            Player.RED -> Color.Red
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

fun initialBoard(): Array<Array<CellState>> {
    val board = Array(12) { row ->
        Array(8) { col ->
            CellState(
                player = when {
                    row in 0..1 && (row + col) % 2 == 1 -> Player.RED
                    row in 10..11 && (row + col) % 2 == 1 -> Player.BLUE
                    else -> Player.NONE
                },
                isProtected = row in 0..1 || row in 10..11 || (row in 5..6 && col in 3..4)
            )
        }
    }
    return board
}

fun calculatePossibleMoves(board: Array<Array<CellState>>, row: Int, col: Int): List<Pair<Int, Int>> {
    val moves = mutableListOf<Pair<Int, Int>>()
    val directions = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    for ((dr, dc) in directions) {
        var newRow = row + dr
        var newCol = col + dc
        while (newRow in 0 until 12 && newCol in 0 until 8 && board[newRow][newCol].player != Player.NONE) {
            newRow += dr
            newCol += dc
        }
        if (newRow in 0 until 12 && newCol in 0 until 8 && board[newRow][newCol].player == Player.NONE) {
            moves.add(newRow to newCol)
        }
    }
    return moves
}

fun movePiece(board: Array<Array<CellState>>, from: Pair<Int, Int>, to: Pair<Int, Int>): Array<Array<CellState>> {
    val newBoard = board.map { it.copyOf() }.toTypedArray()
    newBoard[to.first][to.second] = newBoard[to.first][to.second].copy(player = newBoard[from.first][from.second].player)
    newBoard[from.first][from.second] = newBoard[from.first][from.second].copy(player = Player.NONE)
    return newBoard
}

fun applyGameOfLifeStep(board: Array<Array<CellState>>): Array<Array<CellState>> {
    val newBoard = Array(12) { Array(8) { CellState(Player.NONE, false) } }

    for (row in 0 until 12) {
        for (col in 0 until 8) {
            if (board[row][col].isProtected) {
                newBoard[row][col] = board[row][col]
                continue
            }

            val neighbors = listOf(
                row - 1 to col - 1, row - 1 to col, row - 1 to col + 1,
                row to col - 1, row to col + 1,
                row + 1 to col - 1, row + 1 to col, row + 1 to col + 1
            )

            val (blue, red) = neighbors.fold(0 to 0) { acc, (r, c) ->
                if (r in 0 until 12 && c in 0 until 8 && !board[r][c].isProtected) {
                    when (board[r][c].player) {
                        Player.BLUE -> acc.copy(first = acc.first + 1)
                        Player.RED -> acc.copy(second = acc.second + 1)
                        else -> acc
                    }
                } else acc
            }

            newBoard[row][col] = when {
                board[row][col].player == Player.NONE && blue + red == 3 -> if (blue > red) CellState(Player.BLUE, false) else CellState(Player.RED, false)
                board[row][col].player != Player.NONE && (blue + red < 2 || blue + red > 3) -> CellState(Player.NONE, false)
                else -> board[row][col]
            }
        }
    }

    return newBoard
}

fun checkWinCondition(board: Array<Array<CellState>>, onWin: (Player) -> Unit) {
    val blueInRedZone = board.flatMapIndexed { rowIndex, row ->
        row.mapIndexed { colIndex, cell ->
            if (cell.player == Player.BLUE && cell.isProtected && rowIndex in 0..1) 1 else 0
        }
    }.sum()

    val redInBlueZone = board.flatMapIndexed { rowIndex, row ->
        row.mapIndexed { colIndex, cell ->
            if (cell.player == Player.RED && cell.isProtected && rowIndex in 10..11) 1 else 0
        }
    }.sum()

    val blueCount = board.flatten().count { it.player == Player.BLUE }
    val redCount = board.flatten().count { it.player == Player.RED }

    when {
        blueInRedZone >= 3 -> onWin(Player.BLUE)
        redInBlueZone >= 3 -> onWin(Player.RED)
        blueCount == 0 -> onWin(Player.RED)
        redCount == 0 -> onWin(Player.BLUE)
    }
}

@Composable
fun VictoryScreen(winner: Player, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Player ${if (winner == Player.BLUE) "Blue" else "Red"} Wins!", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Play Again", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Main Menu", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    QuasistarTheme {
        MainScreen()
    }
}
