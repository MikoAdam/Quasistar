package com.quasistar.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.quasistar.logic.GameStateManager
import com.quasistar.model.Screen
import com.quasistar.settings.SettingsScreen
import com.quasistar.ui.rules.RulesUI

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    val context = LocalContext.current
    var showContinueDialog by remember { mutableStateOf(false) }

    // Start with the SplashScreen
    if (currentScreen == null) {
        SplashScreen(onFinishLoading = { currentScreen = Screen.MainMenu })
    } else {
        when (currentScreen) {
            is Screen.MainMenu -> {
                if (showContinueDialog) {
                    val savedGameState = GameStateManager.loadGameState(context)
                    if (savedGameState != null) {
                        // Show a dialog to continue or start a new game
                        AlertDialog(
                            onDismissRequest = { showContinueDialog = false },
                            title = { Text("Continue previous game?") },
                            text = { Text("A previous game was found. Do you want to continue or start a new game?") },
                            confirmButton = {
                                Button(onClick = {
                                    showContinueDialog = false
                                    currentScreen = Screen.Game // Continue the previous game
                                }) {
                                    Text("Continue")
                                }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showContinueDialog = false
                                    GameStateManager.clearGameState(context)
                                    currentScreen = Screen.Game // Start a new game
                                }) {
                                    Text("Start New Game")
                                }
                            }
                        )
                    } else {
                        currentScreen = Screen.Game // No saved game, start a new game directly
                    }
                } else {
                    MenuUI.MainMenuScreen(
                        onStartGame = {
                            // Check for saved game only when Play is clicked
                            val savedGameState = GameStateManager.loadGameState(context)
                            if (savedGameState != null) {
                                showContinueDialog = true
                            } else {
                                currentScreen = Screen.Game // Start a new game directly
                            }
                        },
                        onViewRules = { currentScreen = Screen.Rules },
                        onAboutUs = { /* Navigate to About Us screen */ },
                        onSettings = { currentScreen = Screen.Settings }
                    )
                }
            }
            is Screen.Rules -> RulesUI.RulesScreen(onBack = { currentScreen = Screen.MainMenu })
            is Screen.Game -> {
                BackHandler {
                    currentScreen = Screen.MainMenu // Go back to the main menu when back is pressed
                }
                GameScreen(
                    onWin = { winner -> currentScreen = Screen.Victory(winner) },
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }
            is Screen.Victory -> VictoryScreen(
                winner = (currentScreen as Screen.Victory).winner,
                onRestart = { currentScreen = Screen.Game },
                onBack = { currentScreen = Screen.MainMenu }
            )
            is Screen.Settings -> SettingsScreen(
                context = context, // Pass the context here
                onBack = { currentScreen = Screen.MainMenu }
            )
            else -> {
                // Handle unexpected states or do nothing
            }
        }
    }
}
