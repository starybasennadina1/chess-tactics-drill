package com.chesstactics.drill.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.domain.model.*
import com.chesstactics.drill.presentation.mapper.*

@Composable
fun ChessBoard(board: Board, orientation: PieceColor, style: BoardStyle, modifier: Modifier = Modifier, source: Int? = null, destination: Int? = null, hint: Int? = null, lastMove: Move? = null, reducedMotion: Boolean = false) {
    val dark = when (style) { BoardStyle.CLASSIC -> Sage; BoardStyle.LAVENDER -> Plum; BoardStyle.HIGH_CONTRAST -> Purple }
    val description = stringResource(R.string.board_description, stringResource(board.turn.labelRes()))
    val selectedSource = stringResource(R.string.source_selected)
    val selectedDestination = stringResource(R.string.destination_selected)
    val hinted = stringResource(R.string.hint_piece)
    val last = stringResource(R.string.last_move_square)
    Column(modifier.widthIn(max = 384.dp).fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Cream).padding(8.dp).semantics { contentDescription = description }) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(16.dp))
            repeat(8) { column ->
                val file = if (orientation == PieceColor.WHITE) column else 7 - column
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(stringResource(R.string.data_text, ('a'.code + file).toChar().toString()), style = MaterialTheme.typography.labelSmall, color = Purple) }
            }
        }
        repeat(8) { row ->
            val rank = if (orientation == PieceColor.WHITE) 7 - row else row
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.number, rank + 1), Modifier.width(16.dp), style = MaterialTheme.typography.labelSmall, color = Purple)
                repeat(8) { column ->
                    val file = if (orientation == PieceColor.WHITE) column else 7 - column
                    val index = rank * 8 + file
                    val piece = board.squares[index]
                    val cellDescription = if (piece == null) coordinate(index) else stringResource(R.string.piece_square, stringResource(piece.color.labelRes()), stringResource(piece.type.labelRes()), coordinate(index))
                    val state = listOfNotNull(selectedSource.takeIf { source == index }, selectedDestination.takeIf { destination == index }, hinted.takeIf { hint == index }, last.takeIf { lastMove?.from == index || lastMove?.to == index }).joinToString(", ")
                    Box(Modifier.weight(1f).aspectRatio(1f).background(if ((rank + file) % 2 == 0) dark else Cream).semantics { contentDescription = cellDescription; stateDescription = state }, contentAlignment = Alignment.Center) {
                        if (lastMove?.from == index || lastMove?.to == index) Box(Modifier.fillMaxSize().background(Purple.copy(alpha = 0.16f)))
                        if (source == index) Box(Modifier.fillMaxSize().padding(2.dp).border(2.dp, Purple))
                        if (destination == index) Canvas(Modifier.fillMaxSize().padding(3.dp)) { drawCircle(Purple, style = Stroke(2.dp.toPx())) }
                        if (hint == index) Canvas(Modifier.fillMaxSize().padding(3.dp)) { drawCircle(Purple, style = Stroke(5.dp.toPx())); drawCircle(Lime, style = Stroke(3.dp.toPx())) }
                        Crossfade(piece, animationSpec = tween(if (reducedMotion) 100 else 200), label = "piece") { current ->
                            if (current != null) PieceGlyph(current, Modifier.fillMaxSize().padding(3.dp))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PieceGlyph(piece: Piece, modifier: Modifier) {
    val fill = if (piece.color == PieceColor.WHITE) Cream else Ink
    val edge = if (piece.color == PieceColor.WHITE) Ink else Cream
    Canvas(modifier.clearAndSetSemantics { }) {
        scale(size.width / 100f, size.height / 100f, pivot = Offset.Zero) {
            val body = Path().apply {
                when (piece.type) {
                    PieceType.PAWN -> { moveTo(39f, 43f); lineTo(61f, 43f); cubicTo(57f, 57f, 60f, 70f, 69f, 79f); lineTo(31f, 79f); cubicTo(40f, 68f, 43f, 57f, 39f, 43f) }
                    PieceType.ROOK -> { moveTo(23f, 18f); lineTo(36f, 18f); lineTo(36f, 30f); lineTo(44f, 30f); lineTo(44f, 18f); lineTo(56f, 18f); lineTo(56f, 30f); lineTo(64f, 30f); lineTo(64f, 18f); lineTo(77f, 18f); lineTo(74f, 43f); lineTo(63f, 47f); lineTo(65f, 76f); lineTo(35f, 76f); lineTo(37f, 47f); lineTo(26f, 43f) }
                    PieceType.KNIGHT -> { moveTo(27f, 77f); cubicTo(32f, 59f, 57f, 53f, 44f, 42f); lineTo(27f, 53f); lineTo(17f, 43f); lineTo(31f, 26f); lineTo(46f, 23f); lineTo(50f, 12f); lineTo(59f, 23f); cubicTo(82f, 35f, 76f, 61f, 72f, 77f) }
                    PieceType.BISHOP -> { moveTo(32f, 76f); lineTo(41f, 53f); cubicTo(10f, 40f, 44f, 24f, 50f, 13f); cubicTo(60f, 28f, 87f, 43f, 59f, 53f); lineTo(68f, 76f) }
                    PieceType.QUEEN -> { moveTo(31f, 76f); lineTo(35f, 55f); lineTo(20f, 26f); lineTo(40f, 39f); lineTo(50f, 17f); lineTo(60f, 39f); lineTo(80f, 26f); lineTo(65f, 55f); lineTo(69f, 76f) }
                    PieceType.KING -> { moveTo(32f, 76f); lineTo(39f, 51f); cubicTo(19f, 40f, 29f, 29f, 45f, 34f); lineTo(45f, 25f); lineTo(37f, 25f); lineTo(37f, 17f); lineTo(45f, 17f); lineTo(45f, 9f); lineTo(55f, 9f); lineTo(55f, 17f); lineTo(63f, 17f); lineTo(63f, 25f); lineTo(55f, 25f); lineTo(55f, 34f); cubicTo(71f, 29f, 81f, 40f, 61f, 51f); lineTo(68f, 76f) }
                }
                close()
            }
            drawPath(body, fill)
            drawPath(body, edge, style = Stroke(3.2f))
            if (piece.type == PieceType.PAWN) { drawCircle(fill, 13f, Offset(50f, 31f)); drawCircle(edge, 13f, Offset(50f, 31f), style = Stroke(3.2f)) }
            if (piece.type == PieceType.KNIGHT) drawCircle(edge, 2.7f, Offset(43f, 34f))
            if (piece.type == PieceType.BISHOP) drawLine(edge, Offset(52f, 29f), Offset(42f, 42f), 3.2f)
            if (piece.type == PieceType.QUEEN) listOf(Offset(20f, 25f), Offset(50f, 17f), Offset(80f, 25f)).forEach { drawCircle(fill, 4f, it); drawCircle(edge, 4f, it, style = Stroke(2f)) }
            val base = Path().apply { moveTo(29f, 77f); lineTo(71f, 77f); lineTo(77f, 87f); lineTo(23f, 87f); close() }
            drawPath(base, fill)
            drawPath(base, edge, style = Stroke(3.2f))
        }
    }
}
