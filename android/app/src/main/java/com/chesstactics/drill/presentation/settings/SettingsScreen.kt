package com.chesstactics.drill.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
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
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.dialog.ResetProgressDialog

@Composable
fun SettingsRoute(generation: Int, onBack: () -> Unit, onReset: () -> Unit) {
    val model: SettingsViewModel = viewModel(key = "settings:" + generation, factory = AppViewModelFactory())
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect { onReset() } }
    SettingsScreen(state, model::preferences, onBack, model::openReset, model::retry)
    if (state.resetDialog) ResetProgressDialog(model::closeReset, model::reset)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onPreferences: (Preferences) -> Unit, onBack: () -> Unit, onReset: () -> Unit, onRetry: () -> Unit) {
    StudyPage(listOf(Cream.copy(alpha = 0.97f), Lavender.copy(alpha = 0.96f))) {
        ScreenHeader(stringResource(R.string.settings), onBack)
        Text(stringResource(R.string.make_board_yours), Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = Purple)
        Text(stringResource(R.string.board_style), style = MaterialTheme.typography.titleLarge)
        BoardStyle.entries.forEach { style -> BoardStyleChoice(style, state.preferences.boardStyle == style, !state.saving) { onPreferences(state.preferences.copy(boardStyle = style)) } }
        PreferenceRow(R.string.sound, R.string.sound_description, state.preferences.sound, !state.saving) { onPreferences(state.preferences.copy(sound = it)) }
        PreferenceRow(R.string.reduced_motion, R.string.motion_description, state.preferences.reducedMotion, !state.saving) { onPreferences(state.preferences.copy(reducedMotion = it)) }
        if (state.saving) { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(stringResource(R.string.saving)) }
        if (state.error) ErrorPanel(R.string.settings_error, onRetry)
        SecondaryButton(R.string.reset_progress, onReset, Modifier.fillMaxWidth(), enabled = !state.saving)
        Text(stringResource(R.string.local_only), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PreferenceRow(title: Int, description: Int, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val state = stringResource(if (checked) R.string.on_state else R.string.off_state)
    StudyCard(Modifier.fillMaxWidth().toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange).semantics { stateDescription = state }) {
        Row(Modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(description), style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked, null, enabled = enabled)
        }
    }
}
