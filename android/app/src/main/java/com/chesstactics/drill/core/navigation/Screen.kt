package com.chesstactics.drill.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val sessionId: String) : Screen
    data class GameOver(val resultId: String) : Screen
    data class Solution(val resultId: String) : Screen
    data object Rating : Screen
    data object Settings : Screen
}
