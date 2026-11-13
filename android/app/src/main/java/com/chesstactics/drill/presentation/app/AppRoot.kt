package com.chesstactics.drill.presentation.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chesstactics.drill.core.di.AppViewModelFactory
import com.chesstactics.drill.core.navigation.Screen
import com.chesstactics.drill.presentation.game.GameRoute
import com.chesstactics.drill.presentation.gameover.GameOverRoute
import com.chesstactics.drill.presentation.menu.MenuRoute
import com.chesstactics.drill.presentation.rating.RatingRoute
import com.chesstactics.drill.presentation.settings.SettingsRoute
import com.chesstactics.drill.presentation.solution.SolutionRoute
import com.chesstactics.drill.presentation.splash.SplashRoute

@Composable
fun AppRoot() {
    val model: AppViewModel = viewModel(factory = AppViewModelFactory())
    val screen by model.screen.collectAsStateWithLifecycle()
    val state by model.state.collectAsStateWithLifecycle()
    BackHandler(enabled = screen != Screen.Menu && screen != Screen.Splash) { model.back() }
    AnimatedContent(targetState = screen, modifier = Modifier.fillMaxSize(), transitionSpec = { fadeIn(tween(if (state.reducedMotion) 120 else 320)) togetherWith fadeOut(tween(if (state.reducedMotion) 120 else 320)) }, label = "screen") { current ->
        when (current) {
            Screen.Splash -> SplashRoute(state.reducedMotion, model::splashFinished)
            Screen.Menu -> MenuRoute(state.generation, { model.navigate(Screen.Game(it)) }, { model.navigate(Screen.Rating) }, { model.navigate(Screen.Settings) })
            is Screen.Game -> GameRoute(current.sessionId, state.reducedMotion, { model.navigate(Screen.GameOver(it)) }, model::back, model::menu)
            is Screen.GameOver -> GameOverRoute(current.resultId, model::replay, { model.navigate(Screen.Solution(current.resultId)) }, model::menu)
            is Screen.Solution -> SolutionRoute(current.resultId, state.reducedMotion, model::back, model::menu)
            Screen.Rating -> RatingRoute(model::back, { if (it.isEmpty()) model.menu() else model.navigate(Screen.Game(it)) })
            Screen.Settings -> SettingsRoute(state.generation, model::back, model::progressReset)
        }
    }
}
