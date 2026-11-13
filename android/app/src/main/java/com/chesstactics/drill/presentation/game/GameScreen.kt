package com.chesstactics.drill.presentation.game

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
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.mapper.*

@Composable
fun GameRoute(id: String, reducedMotion: Boolean, onResult: (String) -> Unit, onBack: () -> Unit, onMenu: () -> Unit) {
    val model: GameViewModel = viewModel(key = "game:" + id, factory = AppViewModelFactory(id))
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onResult) }
    PlaybackLifecycle(id, model::resume, model::pause)
    val back = { model.pause(); onBack() }
    BackHandler { if (state.pauseDialog) model.closeBreak() else if (state.chooser != Chooser.NONE || state.hintVisible) model.closeOverlay() else back() }
    GameScreen(state, reducedMotion, back, { model.pause(); onMenu() }, model::openChooser, model::commitMove, model::hint, model::endAttempt, model::viewResult, model::takeBreak, model::retrySave)
    if (state.hintVisible) HintSheet(state, model::closeOverlay)
    if (state.chooser != Chooser.NONE) MoveChoiceSheet(state, model::chooseSource, model::chooseDestination, model::choosePromotion, model::closeOverlay)
    if (state.pauseDialog) AlertDialog(onDismissRequest = model::closeBreak, title = { Text(stringResource(R.string.pause_title)) }, text = { Text(stringResource(R.string.pause_message)) }, confirmButton = { TextButton(model::closeBreak) { Text(stringResource(R.string.resume)) } }, dismissButton = { TextButton(onClick = { model.pause(); onMenu() }) { Text(stringResource(R.string.menu)) } })
}

@Composable
fun GameScreen(state: GameUiState, reducedMotion: Boolean, onBack: () -> Unit, onMenu: () -> Unit, onChooser: (Chooser) -> Unit, onCommit: () -> Unit, onHint: () -> Unit, onEnd: () -> Unit, onResult: () -> Unit, onPause: () -> Unit, onRetry: () -> Unit) {
    StudyPage(listOf(Lavender.copy(alpha = 0.96f), SurfaceDeep.copy(alpha = 0.96f))) {
        val puzzle = state.puzzle
        val session = state.session
        val board = state.board
        ScreenHeader(if (puzzle != null) stringResource(puzzle.theme.titleRes()) else stringResource(R.string.app_name), onBack, puzzle?.let { stringResource(R.string.puzzle_difficulty, it.rating) })
        if (puzzle == null || session == null || board == null) {
            StatusMessage(stringResource(R.string.content_error))
            PrimaryButton(R.string.menu, onMenu)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(if (state.orientation == PieceColor.WHITE) R.string.white_to_move else R.string.black_to_move), style = MaterialTheme.typography.titleLarge, color = Purple)
                Text(stringResource(puzzle.theme.goalRes()), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.your_move, minOf(session.correctMoves + 1, puzzle.playerMoves), puzzle.playerMoves), style = MaterialTheme.typography.labelSmall)
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ChessBoard(board, state.orientation, state.preferences.boardStyle, source = session.source, destination = session.destination, hint = state.hintSquare, lastMove = session.plies.lastOrNull()?.let(Move::parse), reducedMotion = reducedMotion)
            }
            if (session.phase == Phase.COMPLETED) {
                StatusMessage(stringResource(R.string.complete_status))
                PrimaryButton(R.string.view_result, onResult)
                SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), icon = ButtonGlyph.HOME)
            } else {
                StudyCard(Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(R.string.choose_piece, { onChooser(Chooser.SOURCE) }, Modifier.weight(1f), state.canInput)
                        SecondaryButton(R.string.choose_square, { onChooser(Chooser.DESTINATION) }, Modifier.weight(1f), state.canInput && session.source != null)
                    }
                    val source = session.source
                    val destination = session.destination
                    val piece = source?.let { board.squares[it] }
                    if (source != null && destination != null && piece != null) StatusMessage(stringResource(R.string.move_summary, stringResource(piece.type.labelRes()), coordinate(source), coordinate(destination)))
                    else StatusMessage(stringResource(state.status.labelRes()))
                    state.hintSquare?.let { square ->
                        val hintPiece = board.squares[square]
                        if (hintPiece != null) Text(stringResource(R.string.hint_sentence, stringResource(hintPiece.color.labelRes()), stringResource(hintPiece.type.labelRes()), coordinate(square)), style = MaterialTheme.typography.bodyMedium)
                    }
                    PrimaryButton(R.string.make_move, onCommit, enabled = state.canCommit)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(if (session.hintUsed) R.string.hint_used else R.string.hint, onHint, enabled = state.canInput && !session.hintUsed)
                        TextButton(onEnd, enabled = state.canInput, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.end_attempt)) }
                    }
                }
                if (state.busy) StatusMessage(stringResource(state.status.labelRes()))
                Text(stringResource(R.string.end_helper), style = MaterialTheme.typography.bodyMedium)
                SecondaryButton(R.string.pause, onPause, Modifier.fillMaxWidth(), icon = ButtonGlyph.PAUSE)
            }
            if (state.storageError) ErrorPanel(R.string.settings_error, onRetry, R.string.retry_save)
        }
    }
}
