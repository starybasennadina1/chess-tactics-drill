package com.chesstactics.drill.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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

@Composable
fun MenuRoute(generation: Int, onPlay: (String) -> Unit, onRating: () -> Unit, onSettings: () -> Unit) {
    val model: MenuViewModel = viewModel(key = "menu:" + generation, factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onPlay) }
    MenuScreen(state, model::filters, model::play, model::resume, model::retry, onRating, onSettings)
}

@Composable
fun MenuScreen(state: MenuUiState, onFilters: (Filters) -> Unit, onPlay: () -> Unit, onResume: () -> Unit, onRetry: () -> Unit, onRating: () -> Unit, onSettings: () -> Unit) {
    val compact = LocalConfiguration.current.screenHeightDp < 640 || LocalDensity.current.fontScale > 1.3f
    val pageScroll = rememberScrollState()
    val bodyScroll = rememberScrollState()
    ScreenBackground {
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 480.dp).fillMaxSize().safeDrawingPadding().then(if (compact) Modifier.verticalScroll(pageScroll) else Modifier)) {
            Column(Modifier.then(if (compact) Modifier else Modifier.weight(1f).verticalScroll(bodyScroll)).padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.menu_heading), Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = Purple)
                    Text(stringResource(R.string.menu_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
                StudyCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.titleLarge, color = Purple)
                            Text(stringResource(R.string.hero_subtitle), style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(painterResource(R.drawable.sprite_4), null, Modifier.size(54.dp), contentScale = ContentScale.Fit)
                            Image(painterResource(R.drawable.sprite_5), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
                        }
                    }
                }
                if (state.puzzles.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stringResource(R.string.training_rating), stringResource(R.string.number, state.rating), Modifier.weight(1f))
                    val streak = state.bestStreak > 0
                    if (streak || state.puzzles.isNotEmpty()) StatCard(stringResource(if (streak) R.string.best_streak else R.string.practice_puzzles), stringResource(R.string.number, if (streak) state.bestStreak else state.puzzles.size), Modifier.weight(1f))
                }
                if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state.error) ErrorPanel(R.string.storage_error, onRetry)
                if (state.pendingCount > 0) ErrorPanel(R.string.pending_results, onRetry, R.string.retry_save)
                if (state.corruptSession) StatusMessage(stringResource(R.string.session_error))
                if (state.contentError) StatusMessage(stringResource(R.string.library_error))
                Text(stringResource(R.string.choose_theme), style = MaterialTheme.typography.titleLarge, color = Purple)
                PuzzleTheme.entries.forEachIndexed { index, theme ->
                    val selected = state.filters.theme == theme
                    val selectedText = stringResource(if (selected) R.string.selected else R.string.not_selected)
                    val art = when (index) { 0 -> painterResource(R.drawable.sprite_1); 1 -> painterResource(R.drawable.sprite_2); else -> painterResource(R.drawable.sprite_3) }
                    StudyCard(Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = { onFilters(state.filters.copy(theme = theme)) }).semantics { stateDescription = selectedText }, selected) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Image(art, null, Modifier.size(42.dp), contentScale = ContentScale.Fit)
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(theme.titleRes()), style = MaterialTheme.typography.labelLarge, color = Purple)
                                Text(stringResource(theme.descriptionRes()), style = MaterialTheme.typography.bodyMedium)
                                Text(stringResource(R.string.theme_progress, state.puzzles.count { it.theme == theme && it.id in state.solved }, state.puzzles.count { it.theme == theme }), style = MaterialTheme.typography.labelSmall)
                            }
                            RadioButton(selected, onClick = null)
                        }
                    }
                }
                Text(stringResource(R.string.difficulty), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Difficulty.entries.forEach { difficulty ->
                        val chosen = state.filters.difficulty == difficulty
                        val choiceText = stringResource(if (chosen) R.string.selected else R.string.not_selected)
                        OutlinedButton(onClick = { onFilters(state.filters.copy(difficulty = difficulty)) }, modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = choiceText }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if (chosen) Purple else Cream, contentColor = if (chosen) Cream else Purple)) { Text(stringResource(difficulty.labelRes())) }
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.unsolved_only), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(state.filters.unsolvedOnly, { onFilters(state.filters.copy(unsolvedOnly = it)) })
                }
                if (!state.loading && state.eligible.isEmpty()) {
                    StatusMessage(stringResource(R.string.empty_filter))
                    SecondaryButton(R.string.practice_all, { onFilters(state.filters.copy(unsolvedOnly = false)) })
                    SecondaryButton(R.string.reset_filters, { onFilters(state.filters.copy(difficulty = Difficulty.ALL, unsolvedOnly = false)) })
                }
                state.resume?.let { session ->
                    val puzzle = state.puzzles.find { it.id == session.puzzleId }
                    if (puzzle != null) {
                        StatusMessage(stringResource(R.string.resume_summary, stringResource(puzzle.titleRes()), session.plies.size / 2 + 1))
                        SecondaryButton(R.string.resume, onResume, Modifier.fillMaxWidth())
                    }
                }
            }
            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.menu_hint), style = MaterialTheme.typography.bodyMedium)
                StatusMessage(stringResource(R.string.available, state.eligible.size))
                PrimaryButton(R.string.play, onPlay, enabled = state.ready && !state.loading && !state.busy && state.eligible.isNotEmpty() && state.pendingCount == 0)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onRating, Modifier.weight(1f).heightIn(min = 48.dp)) { Text(stringResource(R.string.rating)) }
                    TextButton(onSettings, Modifier.weight(1f).heightIn(min = 48.dp)) { Text(stringResource(R.string.settings)) }
                }
            }
        }
    }
}
