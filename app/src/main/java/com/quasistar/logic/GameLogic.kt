package com.quasistar.logic

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.quasistar.model.CellState
import com.quasistar.model.Player
import kotlin.random.Random

object GameLogic {

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

        val movingPlayer = board[row][col].player

        for ((dr, dc) in directions) {
            var newRow = row + dr
            var newCol = col + dc
            var jumpedOverPiece = false

            while (newRow in 0 until 12 && newCol in 0 until 8) {
                if (board[newRow][newCol].player == Player.NONE || isEnemyHomeZone(movingPlayer, newRow)) {
                    if (jumpedOverPiece) {
                        // If we jumped over at least one piece diagonally, this is a valid move
                        moves.add(newRow to newCol)
                    } else {
                        // Normal move to an adjacent empty cell or into enemy home zone
                        moves.add(newRow to newCol)
                    }
                    break
                } else if (dr != 0 && dc != 0) {
                    // If moving diagonally and we've encountered a piece, attempt to jump
                    jumpedOverPiece = true
                    newRow += dr
                    newCol += dc
                } else {
                    // If moving horizontally or vertically and encounter a piece, stop
                    break
                }
            }
        }
        return moves
    }

    private fun isEnemyHomeZone(player: Player, row: Int): Boolean {
        return (player == Player.ONE && row in 0..1) || (player == Player.TWO && row in 10..11)
    }

    fun movePiece(board: Array<Array<CellState>>, from: Pair<Int, Int>, to: Pair<Int, Int>): Array<Array<CellState>> {
        val newBoard = board.map { it.copyOf() }.toTypedArray()
        val movingPlayer = newBoard[from.first][from.second].player

        if (isEnemyHomeZone(movingPlayer, to.first)) {
            // Always allow the move into the enemy's home zone, and remove any enemy piece there
            newBoard[to.first][to.second] = newBoard[to.first][to.second].copy(player = movingPlayer)
            newBoard[from.first][from.second] = newBoard[from.first][from.second].copy(player = Player.NONE)
        } else {
            // Normal move or into the middle protected zone
            newBoard[to.first][to.second] = newBoard[to.first][to.second].copy(player = movingPlayer)
            newBoard[from.first][from.second] = newBoard[from.first][from.second].copy(player = Player.NONE)
        }

        return newBoard
    }

    fun updateGameOfLifeProbability(
        context: Context,
        board: Array<Array<CellState>>,
        moveCount: Int,
        gameOfLifeProbability: Float
    ): Float {
        var updatedProbability = gameOfLifeProbability
        if (moveCount >= 6 && Random.nextFloat() < updatedProbability) {
            applyGameOfLifeStep(board)
            updatedProbability = 0.1f
            triggerVibration(context)
        } else if (moveCount >= 6) {
            updatedProbability += 0.02f
        }
        return updatedProbability
    }

    private fun applyGameOfLifeStep(board: Array<Array<CellState>>) {
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

    private fun triggerVibration(context: Context) {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }

        vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun checkWinCondition(
        board: Array<Array<CellState>>,
        winningCondition: Int,
        onWin: (Player) -> Unit
    ) {
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
            oneInTwoZone >= winningCondition -> onWin(Player.ONE)
            twoInOneZone >= winningCondition -> onWin(Player.TWO)
            oneCount == 0 -> onWin(Player.TWO)
            twoCount == 0 -> onWin(Player.ONE)
        }
    }
}
