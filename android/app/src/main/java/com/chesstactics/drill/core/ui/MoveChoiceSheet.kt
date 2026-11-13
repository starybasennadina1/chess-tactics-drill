package com.chesstactics.drill.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.game.*
import com.chesstactics.drill.presentation.mapper.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveChoiceSheet(state: GameUiState, onSource: (Int) -> Unit, onDestination: (Int) -> Unit, onPromotion: (PieceType) -> Unit, onClose: () -> Unit) {
    val board = state.board ?: return
    val title = when (state.chooser) { Chooser.SOURCE -> R.string.choose_piece; Chooser.PROMOTION -> R.string.choose_promotion; else -> R.string.choose_square }
    val choices = when (state.chooser) {
        Chooser.SOURCE -> state.moves.map { it.from }.distinct().sorted()
        Chooser.DESTINATION -> state.moves.filter { it.from == state.session?.source }.map { it.to }.distinct().sorted()
        else -> emptyList()
    }
    ModalBottomSheet(onDismissRequest = onClose, containerColor = Cream, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge, color = Purple)
            if (state.chooser == Chooser.PROMOTION) {
                listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { type ->
                    OutlinedButton(onClick = { onPromotion(type) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(stringResource(type.labelRes())) }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(if (state.chooser == Chooser.SOURCE) 144.dp else 96.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(choices) { index ->
                        val piece = board.squares[index]
                        val label = if (state.chooser == Chooser.SOURCE && piece != null) stringResource(R.string.piece_square, stringResource(piece.color.labelRes()), stringResource(piece.type.labelRes()), coordinate(index))
                        else if (piece != null) stringResource(R.string.capture_choice, coordinate(index), stringResource(piece.color.labelRes()), stringResource(piece.type.labelRes()))
                        else stringResource(R.string.square_choice, coordinate(index))
                        OutlinedButton(onClick = { if (state.chooser == Chooser.SOURCE) onSource(index) else onDestination(index) }, modifier = Modifier.heightIn(min = 56.dp)) { Text(label, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            SecondaryButton(R.string.close, onClose, Modifier.fillMaxWidth())
        }
    }
}
