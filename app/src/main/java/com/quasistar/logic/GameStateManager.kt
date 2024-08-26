package com.quasistar.logic

import android.content.Context
import com.google.gson.Gson
import com.quasistar.model.CellState
import com.quasistar.model.Player

object GameStateManager {

    private const val PREFS_NAME = "quasistar_prefs"
    private const val GAME_STATE_KEY = "game_state"

    fun saveGameState(context: Context, board: Array<Array<CellState>>, currentPlayer: Player, gameOfLifeSteps: List<Boolean>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()
        val boardJson = gson.toJson(board)
        val currentPlayerJson = gson.toJson(currentPlayer)
        val gameOfLifeJson = gson.toJson(gameOfLifeSteps)

        editor.putString("board", boardJson)
        editor.putString("currentPlayer", currentPlayerJson)
        editor.putString("gameOfLifeSteps", gameOfLifeJson)
        editor.apply()
    }

    fun loadGameState(context: Context): Triple<Array<Array<CellState>>, Player, List<Boolean>>? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val boardJson = sharedPreferences.getString("board", null)
        val currentPlayerJson = sharedPreferences.getString("currentPlayer", null)
        val gameOfLifeJson = sharedPreferences.getString("gameOfLifeSteps", null)

        if (boardJson == null || currentPlayerJson == null || gameOfLifeJson == null) {
            return null
        }

        val boardType = object : com.google.gson.reflect.TypeToken<Array<Array<CellState>>>() {}.type
        val board: Array<Array<CellState>> = gson.fromJson(boardJson, boardType)
        val currentPlayer: Player = gson.fromJson(currentPlayerJson, Player::class.java)
        val gameOfLifeSteps: List<Boolean> = gson.fromJson(gameOfLifeJson, object : com.google.gson.reflect.TypeToken<List<Boolean>>() {}.type)

        return Triple(board, currentPlayer, gameOfLifeSteps)
    }

    fun clearGameState(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
