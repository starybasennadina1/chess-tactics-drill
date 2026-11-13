package com.chesstactics.drill.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chesstactics.drill.R
import com.chesstactics.drill.core.di.AppViewModelFactory
import com.chesstactics.drill.core.theme.*
import com.chesstactics.drill.core.ui.ScreenBackground

@Composable
fun SplashRoute(reducedMotion: Boolean, onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = AppViewModelFactory())
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished && model.acknowledge()) onFinished() }
    SplashScreen(reducedMotion)
}

@Composable
fun SplashScreen(reducedMotion: Boolean) {
    val loop = rememberInfiniteTransition(label = "studyHalo")
    val float by loop.animateFloat(-6f, 6f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    val halo by loop.animateFloat(0.10f, if (reducedMotion) 0.12f else 0.18f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "halo")
    val preparing = stringResource(R.string.preparing)
    ScreenBackground(listOf(Purple.copy(alpha = 0.93f), Ink.copy(alpha = 0.90f), Purple.copy(alpha = 0.97f))) {
        Column(Modifier.align(Alignment.Center).widthIn(max = 400.dp).fillMaxWidth().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.offline_study), color = Cream.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(40.dp))
            Box(Modifier.size(192.dp).graphicsLayer { translationY = if (reducedMotion) 0f else float }, contentAlignment = Alignment.Center) {
                Box(Modifier.size(176.dp).background(Cream.copy(alpha = halo), CircleShape).border(1.dp, Cream.copy(alpha = 0.16f), CircleShape))
                Image(painterResource(R.drawable.logo), null, Modifier.size(154.dp), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.app_name), Modifier.semantics { heading() }, color = Cream, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.splash_tagline), color = Cream.copy(alpha = 0.86f), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))
            CircularProgressIndicator(Modifier.size(32.dp).semantics { contentDescription = preparing }, color = Lime, strokeWidth = 3.dp, trackColor = Cream.copy(alpha = 0.12f))
        }
    }
}
