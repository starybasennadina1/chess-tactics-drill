package com.chesstactics.drill.presentation.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.core.ui.*
import com.chesstactics.drill.domain.model.coordinate
import com.chesstactics.drill.presentation.mapper.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HintSheet(state: GameUiState, onClose: () -> Unit) {
    val square = state.hintSquare ?: return
    val piece = state.board?.squares?.get(square) ?: return
    ModalBottomSheet(onDismissRequest = onClose, containerColor = Cream, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(painterResource(R.drawable.sprite_1), null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
            Text(stringResource(R.string.hint_title), Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = Purple)
            StatusMessage(stringResource(R.string.hint_sentence, stringResource(piece.color.labelRes()), stringResource(piece.type.labelRes()), coordinate(square)))
            Text(stringResource(R.string.hint_cost), style = MaterialTheme.typography.bodyLarge)
            PrimaryButton(R.string.close, onClose)
            Spacer(Modifier.height(12.dp))
        }
    }
}
