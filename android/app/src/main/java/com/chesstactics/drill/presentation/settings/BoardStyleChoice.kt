package com.chesstactics.drill.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.core.ui.StudyCard
import com.chesstactics.drill.domain.model.BoardStyle
import com.chesstactics.drill.presentation.mapper.labelRes

@Composable
fun BoardStyleChoice(style: BoardStyle, selected: Boolean, enabled: Boolean, onSelect: () -> Unit) {
    val dark = when (style) { BoardStyle.CLASSIC -> Sage; BoardStyle.LAVENDER -> Plum; BoardStyle.HIGH_CONTRAST -> Purple }
    val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
    StudyCard(Modifier.fillMaxWidth().selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect).semantics { stateDescription = description }, selected) {
        Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.size(48.dp).clearAndSetSemantics { }) {
                repeat(4) { row -> Row(Modifier.weight(1f)) { repeat(4) { column -> Box(Modifier.weight(1f).fillMaxHeight().background(if ((row + column) % 2 == 0) dark else Cream)) } } }
            }
            Text(stringResource(style.labelRes()), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = if (enabled) Purple else Ink.copy(alpha = 0.45f))
            RadioButton(selected, null, enabled = enabled)
        }
    }
}
