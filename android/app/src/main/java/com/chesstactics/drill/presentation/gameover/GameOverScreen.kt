package com.chesstactics.drill.presentation.gameover

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chesstactics.drill.R
import com.chesstactics.drill.core.di.AppViewModelFactory
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.core.ui.*
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.mapper.*

@Composable
fun GameOverRoute(id: String, onAgain: (String) -> Unit, onSolution: () -> Unit, onMenu: () -> Unit) {
    val model: GameOverViewModel = viewModel(key = "result:" + id, factory = AppViewModelFactory(id))
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onAgain) }
    GameOverScreen(state, model::playAgain, onSolution, onMenu, model::retrySave)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onAgain: () -> Unit, onSolution: () -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    StudyPage(listOf(Cream.copy(alpha = 0.97f), Plum.copy(alpha = 0.94f))) {
        Image(painterResource(R.drawable.logo), null, Modifier.size(96.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
        val session = state.session
        val puzzle = state.puzzle
        Text(stringResource(session?.outcome?.labelRes() ?: R.string.mistake_title), Modifier.fillMaxWidth().semantics { heading(); liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.headlineMedium, color = Purple, textAlign = TextAlign.Center)
        if (session != null && puzzle != null) {
            StudyCard(Modifier.fillMaxWidth()) {
                Text(stringResource(puzzle.titleRes()), style = MaterialTheme.typography.titleLarge, color = Purple)
                Text(stringResource(puzzle.theme.titleRes()), style = MaterialTheme.typography.labelSmall)
                StatusMessage(stringResource(R.string.found_moves, session.correctMoves, puzzle.playerMoves))
                val moves = session.plies.filterIndexed { index, _ -> index % 2 == 0 }.joinToString(", ")
                Text(if (moves.isEmpty()) stringResource(R.string.no_moves_played) else stringResource(R.string.played_moves, moves), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(puzzle.explanationRes()), style = MaterialTheme.typography.bodyLarge)
                if (session.hintUsed) Text(stringResource(R.string.hint_result), style = MaterialTheme.typography.bodyMedium)
            }
            state.result?.let { result ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stringResource(R.string.training_rating), stringResource(R.string.number, result.ratingAfter), Modifier.weight(1f))
                    StatCard(stringResource(R.string.difficulty), stringResource(R.string.number, puzzle.rating), Modifier.weight(1f))
                }
                val delta = if (result.delta > 0) stringResource(R.string.signed_positive, result.delta) else stringResource(R.string.number, result.delta)
                StatusMessage(if (result.rated) stringResource(R.string.rating_change, result.ratingAfter, delta) else stringResource(R.string.practice_repeat))
                Text(stringResource(R.string.saved), style = MaterialTheme.typography.labelSmall)
            }
        }
        if (state.saving) { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(stringResource(R.string.saving)) }
        if (state.error) ErrorPanel(R.string.result_save_error, onRetry, R.string.retry_save)
        Text(stringResource(R.string.retry_helper), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(R.string.play_again, onAgain, enabled = !state.saving && state.result != null && !state.starting)
        SecondaryButton(R.string.view_solution, onSolution, Modifier.fillMaxWidth(), enabled = session != null)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), icon = ButtonGlyph.HOME)
    }
}
