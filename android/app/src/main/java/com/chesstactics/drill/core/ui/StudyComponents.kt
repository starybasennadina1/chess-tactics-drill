package com.chesstactics.drill.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*

@Composable
fun ScreenBackground(colors: List<Color> = listOf(Lavender.copy(alpha = 0.96f), Cream.copy(alpha = 0.96f)), content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        content()
    }
}

@Composable
fun StudyPage(colors: List<Color> = listOf(Lavender.copy(alpha = 0.96f), Cream.copy(alpha = 0.96f)), content: @Composable ColumnScope.() -> Unit) {
    ScreenBackground(colors) {
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 480.dp).fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
fun StudyCard(modifier: Modifier = Modifier, selected: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = Cream.copy(alpha = 0.96f), border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Purple else Plum.copy(alpha = 0.6f)), shadowElevation = 4.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    StudyCard(modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontFeatureSettings = "tnum"), color = Purple)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, trailing: String? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.back)) }
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = Purple)
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StatusMessage(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium, color = Ink.copy(alpha = 0.78f))
}

@Composable
fun ErrorPanel(textRes: Int, onRetry: () -> Unit, retryRes: Int = R.string.retry) {
    StudyCard(Modifier.fillMaxWidth()) { StatusMessage(stringResource(textRes)); SecondaryButton(retryRes, onRetry) }
}
