package com.quasistar.model

sealed class Screen {
    data object MainMenu : Screen()
    data object Rules : Screen()
    data object Game : Screen()
    data class Victory(val winner: Player) : Screen()
    data object Settings : Screen()
}
