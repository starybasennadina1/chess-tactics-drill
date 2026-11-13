package com.chesstactics.drill.presentation.solution

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chesstactics.drill.R
import com.chesstactics.drill.core.di.AppViewModelFactory
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.core.ui.*
import com.chesstactics.drill.domain.model.Move
import com.chesstactics.drill.presentation.mapper.*

@Composable
fun SolutionRoute(id: String, reducedMotion: Boolean, onBack: () -> Unit, onMenu: () -> Unit) {
    val model: SolutionViewModel = viewModel(key = "solution:" + id, factory = AppViewModelFactory(id))
    val state by model.state.collectAsStateWithLifecycle()
    PlaybackLifecycle(id, model::resume, { model.pause(true) })
    BackHandler { model.pause(); onBack() }
    SolutionScreen(state, reducedMotion, { model.pause(); onBack() }, { model.pause(); onMenu() }, { model.step(-1) }, { model.step(1) }, model::autoplay, model::restart)
}

@Composable
fun SolutionScreen(state: SolutionUiState, reducedMotion: Boolean, onBack: () -> Unit, onMenu: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit, onAutoplay: () -> Unit, onRestart: () -> Unit) {
    StudyPage(listOf(SurfaceDeep.copy(alpha = 0.96f), Lavender.copy(alpha = 0.96f))) {
        ScreenHeader(stringResource(R.string.solution), onBack)
        val puzzle = state.puzzle
        val board = state.board
        if (puzzle == null || board == null || state.error) {
            StatusMessage(stringResource(R.string.content_error))
        } else {
            Text(stringResource(puzzle.titleRes()), style = MaterialTheme.typography.titleLarge, color = Purple)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ChessBoard(board, state.orientation, state.preferences.boardStyle, lastMove = puzzle.line.getOrNull(state.index - 1)?.let(Move::parse), reducedMotion = reducedMotion)
            }
            val playing = stringResource(if (state.playing) R.string.playing else R.string.stopped)
            val spoken = stringResource(R.string.replay_state, state.index, puzzle.line.size, playing)
            StudyCard(Modifier.fillMaxWidth().semantics { stateDescription = spoken }) {
                Text(stringResource(R.string.solution_line, puzzle.san.joinToString(" ")), style = MaterialTheme.typography.titleMedium, color = Purple)
                StatusMessage(stringResource(R.string.replay_index, state.index, puzzle.line.size))
                Text(stringResource(if (state.index == 0) R.string.solution_start else if (state.index == 2) R.string.opponent_annotation else puzzle.explanationRes()), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.previous, onPrevious, Modifier.weight(1f), state.index > 0)
                    SecondaryButton(R.string.next, onNext, Modifier.weight(1f), state.index < puzzle.line.size)
                }
                PrimaryButton(if (state.playing) R.string.pause else R.string.auto_play, onAutoplay, enabled = state.index < puzzle.line.size)
                SecondaryButton(R.string.restart, onRestart, Modifier.fillMaxWidth())
            }
        }
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), icon = ButtonGlyph.HOME)
    }
}
