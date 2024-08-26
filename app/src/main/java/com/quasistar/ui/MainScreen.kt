package com.quasistar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.quasistar.model.Screen
import com.quasistar.settings.SettingsScreen
import com.quasistar.ui.rules.RulesUI

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }
    val context = LocalContext.current // Retrieve the current context

    when (currentScreen) {
        is Screen.MainMenu -> MenuUI.MainMenuScreen(
            onStartGame = { currentScreen = Screen.Game },
            onViewRules = { currentScreen = Screen.Rules },
            onAboutUs = { /* Navigate to About Us screen */ },
            onSettings = { currentScreen = Screen.Settings }
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
        is Screen.Settings -> SettingsScreen(
            context = context, // Pass the context here
            onBack = { currentScreen = Screen.MainMenu }
        )
    }
}
