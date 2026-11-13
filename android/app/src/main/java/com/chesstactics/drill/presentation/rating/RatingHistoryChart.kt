package com.chesstactics.drill.presentation.rating

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.domain.model.AttemptResult

@Composable
fun RatingHistoryChart(records: List<AttemptResult>) {
    val data = records.takeLast(20)
    val values = data.map { it.ratingAfter }
    if (values.size < 2) return
    val low = values.min()
    val high = values.max()
    val minY = (low - 25).coerceAtLeast(100)
    val maxY = (high + 25).coerceAtMost(2500)
    val summary = stringResource(R.string.chart_summary, data.first().ratingBefore, values.last(), low, high)
    Canvas(Modifier.fillMaxWidth().height(160.dp).semantics { contentDescription = summary }) {
        val inset = 12.dp.toPx()
        val width = size.width - inset * 2
        val height = size.height - inset * 2
        repeat(3) { index ->
            val y = inset + height * index / 2
            drawLine(Plum.copy(alpha = 0.4f), Offset(inset, y), Offset(size.width - inset, y), 1.dp.toPx())
        }
        val points = values.mapIndexed { index, value -> Offset(inset + width * index / values.lastIndex, inset + height * (1 - (value - minY).toFloat() / (maxY - minY).coerceAtLeast(1))) }
        val path = Path().apply { points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) } }
        drawPath(path, Purple, style = Stroke(3.dp.toPx()))
        points.forEach { point -> drawCircle(Lime, 4.dp.toPx(), point); drawCircle(Purple, 4.dp.toPx(), point, style = Stroke(1.5.dp.toPx())) }
    }
}
