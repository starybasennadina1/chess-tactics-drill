package com.chesstactics.drill.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chesstactics.drill.presentation.app.AppViewModel
import com.chesstactics.drill.presentation.splash.SplashViewModel
import com.chesstactics.drill.presentation.menu.MenuViewModel
import com.chesstactics.drill.presentation.game.GameViewModel
import com.chesstactics.drill.presentation.gameover.GameOverViewModel
import com.chesstactics.drill.presentation.solution.SolutionViewModel
import com.chesstactics.drill.presentation.rating.RatingViewModel
import com.chesstactics.drill.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val routeId: String = "") : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val services = ServiceLocator.services
        return when (modelClass) {
            AppViewModel::class.java -> AppViewModel(services.progress, services.systemReducedMotion)
            SplashViewModel::class.java -> SplashViewModel()
            MenuViewModel::class.java -> MenuViewModel(services.puzzles, services.progress)
            GameViewModel::class.java -> GameViewModel(routeId, services.puzzles, services.progress, services.sound())
            GameOverViewModel::class.java -> GameOverViewModel(routeId, services.puzzles, services.progress)
            SolutionViewModel::class.java -> SolutionViewModel(routeId, services.puzzles, services.progress)
            RatingViewModel::class.java -> RatingViewModel(services.puzzles, services.progress)
            SettingsViewModel::class.java -> SettingsViewModel(services.progress)
            else -> error(modelClass.name)
        } as T
    }
}
