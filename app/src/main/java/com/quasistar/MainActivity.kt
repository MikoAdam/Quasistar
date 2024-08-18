package com.quasistar

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.quasistar.ui.theme.QuasistarTheme
import kotlin.random.Random

enum class Player {
    NONE, ONE, TWO
}

data class CellState(
    val player: Player,
    val isProtected: Boolean
)

sealed class Screen {
    data object MainMenu : Screen()
    data object Rules : Screen()
    data object Game : Screen()
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
        is Screen.MainMenu -> MenuUI.MainMenuScreen(
            onStartGame = { currentScreen = Screen.Game },
            onViewRules = { currentScreen = Screen.Rules },
            onAboutUs = { /* Navigate to About Us screen */ },
            onSettings = { /* Navigate to Settings screen */ }
        )
        is Screen.Rules -> RulesUI.RulesScreen(onBack = { currentScreen = Screen.MainMenu })
        is Screen.Game -> GameScreen(
            onWin = { winner -> currentScreen = Screen.Victory(winner) },
            onBack = { currentScreen = Screen.MainMenu }
        )
        is Screen.Victory -> VictoryScreen(
            winner = (currentScreen as Screen.Victory).winner,
            onRestart = { currentScreen = Screen.Game },
            onBack = { currentScreen = Screen.MainMenu }
        )
    }
}

@Composable
fun GameScreen(onWin: (Player) -> Unit, onBack: () -> Unit) {
    var board by remember { mutableStateOf(initialBoard()) }
    var currentPlayer by remember { mutableStateOf(Player.ONE) }
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var possibleMoves by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var moveCount by remember { mutableIntStateOf(0) }
    val gameOfLifeProbability = remember { mutableFloatStateOf(0.1f) }
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
        LinearProgressIndicator(
            progress = { gameOfLifeProbability.floatValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(horizontal = 32.dp),
            color = Color(0xFFFF6F61),
            trackColor = Color(0xFF003C46),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF003C46), RoundedCornerShape(16.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                for (row in 0 until 12) {
                    Row {
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
                                        possibleMoves = calculatePossibleMoves(board, row, col)
                                    } else if (selectedPiece != null && (row to col) in possibleMoves) {
                                        board = movePiece(board, selectedPiece!!, row to col)
                                        currentPlayer =
                                            if (currentPlayer == Player.ONE) Player.TWO else Player.ONE
                                        selectedPiece = null
                                        possibleMoves = listOf()
                                        moveCount++
                                        updateGameOfLifeProbability(
                                            context,
                                            board,
                                            moveCount,
                                            gameOfLifeProbability
                                        )
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
    }
}

fun updateGameOfLifeProbability(
    context: android.content.Context,
    board: Array<Array<CellState>>,
    moveCount: Int,
    gameOfLifeProbability: MutableState<Float>
) {
    if (moveCount >= 6 && Random.nextFloat() < gameOfLifeProbability.value) {
        applyGameOfLifeStep(board)
        gameOfLifeProbability.value = 0.1f
        triggerVibration(context)
    } else if (moveCount >= 6) {
        gameOfLifeProbability.value += 0.02f
    }
}

fun applyGameOfLifeStep(board: Array<Array<CellState>>) {
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

            val (one, two) = neighbors.fold(0 to 0) { acc, (r, c) ->
                if (r in 0 until 12 && c in 0 until 8 && !board[r][c].isProtected) {
                    when (board[r][c].player) {
                        Player.ONE -> acc.copy(first = acc.first + 1)
                        Player.TWO -> acc.copy(second = acc.second + 1)
                        else -> acc
                    }
                } else acc
            }

            newBoard[row][col] = when {
                board[row][col].player == Player.NONE && one + two == 3 -> if (one > two) CellState(Player.ONE, false) else CellState(Player.TWO, false)
                board[row][col].player != Player.NONE && (one + two < 2 || one + two > 3) -> CellState(Player.NONE, false)
                else -> board[row][col]
            }
        }
    }

    for (row in 0 until 12) {
        for (col in 0 until 8) {
            board[row][col] = newBoard[row][col]
        }
    }
}

fun triggerVibration(context: android.content.Context) {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        context.getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        context.getSystemService<Vibrator>()
    }

    vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
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

fun initialBoard(): Array<Array<CellState>> {
    val board = Array(12) { row ->
        Array(8) { col ->
            CellState(
                player = when {
                    row in 0..1 && (row + col) % 2 == 1 -> Player.TWO
                    row in 10..11 && (row + col) % 2 == 1 -> Player.ONE
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
    val directions = listOf(
        -1 to -1, -1 to 0, -1 to 1,  // Upward directions
        0 to -1, 0 to 1,             // Horizontal directions
        1 to -1, 1 to 0, 1 to 1      // Downward directions
    )

    for ((dr, dc) in directions) {
        val newRow = row + dr
        val newCol = col + dc
        val jumpRow = row + 2 * dr
        val jumpCol = col + 2 * dc

        // Normal move
        if (newRow in 0 until 12 && newCol in 0 until 8 && board[newRow][newCol].player == Player.NONE) {
            moves.add(newRow to newCol)
        }

        // Diagonal jump over an opponent
        if (dr != 0 && dc != 0 && newRow in 0 until 12 && newCol in 0 until 8 &&
            jumpRow in 0 until 12 && jumpCol in 0 until 8 &&
            board[newRow][newCol].player != Player.NONE && board[newRow][newCol].player != board[row][col].player &&
            board[jumpRow][jumpCol].player == Player.NONE) {
            moves.add(jumpRow to jumpCol)
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

fun checkWinCondition(board: Array<Array<CellState>>, onWin: (Player) -> Unit) {
    val oneInTwoZone = board.flatMapIndexed { rowIndex, row ->
        row.mapIndexed { _, cell ->
            if (cell.player == Player.ONE && cell.isProtected && rowIndex in 0..1) 1 else 0
        }
    }.sum()

    val twoInOneZone = board.flatMapIndexed { rowIndex, row ->
        row.mapIndexed { _, cell ->
            if (cell.player == Player.TWO && cell.isProtected && rowIndex in 10..11) 1 else 0
        }
    }.sum()

    val oneCount = board.flatten().count { it.player == Player.ONE }
    val twoCount = board.flatten().count { it.player == Player.TWO }

    when {
        oneInTwoZone >= 3 -> onWin(Player.ONE)
        twoInOneZone >= 3 -> onWin(Player.TWO)
        oneCount == 0 -> onWin(Player.TWO)
        twoCount == 0 -> onWin(Player.ONE)
    }
}

@Composable
fun VictoryScreen(winner: Player, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003C46))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Player ${if (winner == Player.ONE) "One" else "Two"} Wins!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61))
        ) {
            Text("Play Again", fontSize = 18.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF017374))
        ) {
            Text("Main Menu", fontSize = 18.sp, color = Color.White)
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
