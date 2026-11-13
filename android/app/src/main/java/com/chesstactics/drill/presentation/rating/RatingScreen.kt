package com.chesstactics.drill.presentation.rating

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.mapper.*
import java.text.DateFormat
import java.util.Date

@Composable
fun RatingRoute(onBack: () -> Unit, onPlay: (String) -> Unit) {
    val model: RatingViewModel = viewModel(factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onPlay) }
    RatingScreen(state, onBack, model::play, model::retry)
}

@Composable
fun RatingScreen(state: RatingUiState, onBack: () -> Unit, onPlay: () -> Unit, onRetry: () -> Unit) {
    StudyPage(listOf(Lavender.copy(alpha = 0.96f), SurfaceWarm.copy(alpha = 0.96f))) {
        ScreenHeader(stringResource(R.string.your_progress), onBack)
        Text(stringResource(R.string.progress_heading), Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = Purple)
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.error) ErrorPanel(R.string.storage_error, onRetry)
        val progress = state.progress
        if (state.puzzles.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.training_rating), stringResource(R.string.number, progress.rating), Modifier.weight(1f))
            StatCard(stringResource(if (progress.bestStreak > 0) R.string.best_streak else R.string.practice_puzzles), stringResource(R.string.number, if (progress.bestStreak > 0) progress.bestStreak else state.puzzles.size), Modifier.weight(1f))
        }
        if (progress.results.isEmpty()) StatusMessage(stringResource(R.string.starting_rating))
        val rated = progress.results.filter { it.rated }
        StudyCard(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rating_history), style = MaterialTheme.typography.titleLarge, color = Purple)
            if (rated.size < 2) Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge) else RatingHistoryChart(rated)
        }
        PuzzleTheme.entries.forEach { theme ->
            val puzzles = state.puzzles.filter { it.theme == theme }
            val solved = puzzles.count { progress.puzzles[it.id]?.solved == true }
            val clean = puzzles.count { progress.puzzles[it.id]?.clean == true }
            val assisted = puzzles.count { progress.puzzles[it.id]?.assisted == true }
            val description = stringResource(R.string.theme_progress, solved, puzzles.size)
            StudyCard(Modifier.fillMaxWidth()) {
                Text(stringResource(theme.titleRes()), style = MaterialTheme.typography.labelLarge, color = Purple)
                Text(description, style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(progress = { if (puzzles.isEmpty()) 0f else solved.toFloat() / puzzles.size }, modifier = Modifier.fillMaxWidth().height(8.dp).semantics { stateDescription = description }, color = Purple, trackColor = SurfaceDeep)
                Text(stringResource(R.string.clean_assisted, clean, assisted), style = MaterialTheme.typography.bodyMedium)
            }
        }
        StatusMessage(stringResource(R.string.attempts_count, progress.attemptCount))
        rated.takeLast(20).reversed().forEach { result ->
            val puzzle = state.puzzles.find { it.id == result.session.puzzleId }
            if (puzzle != null) StudyCard(Modifier.fillMaxWidth()) {
                val date = remember(result.timestamp) { DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(result.timestamp)) }
                Text(stringResource(R.string.history_entry, stringResource(puzzle.titleRes()), date), style = MaterialTheme.typography.titleMedium)
                val delta = if (result.delta > 0) stringResource(R.string.signed_positive, result.delta) else stringResource(R.string.number, result.delta)
                Text(stringResource(R.string.history_detail, stringResource(result.session.outcome?.labelRes() ?: R.string.mistake_title), delta), style = MaterialTheme.typography.bodyMedium)
                if (result.session.hintUsed) Text(stringResource(R.string.hint_result), style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(stringResource(R.string.menu_hint), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(R.string.play, onPlay, enabled = !state.loading && !state.busy)
    }
}
