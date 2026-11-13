package com.chesstactics.drill.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chesstactics.drill.R
import com.chesstactics.drill.core.theme.*

enum class ButtonGlyph { HOME, PAUSE }

@Composable
fun PrimaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(16.dp)
    Button(onClick = onClick, enabled = enabled, shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Ink, disabledContainerColor = Color.Transparent, disabledContentColor = Ink.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp).shadow(if (enabled) 5.dp else 0.dp, shape).clip(shape)
            .background(if (enabled) Brush.horizontalGradient(listOf(Lime, Sage)) else Brush.horizontalGradient(listOf(SurfaceDeep, SurfaceWarm))),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ButtonGlyph? = null) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Purple.copy(alpha = if (enabled) 0.65f else 0.2f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Cream.copy(alpha = 0.92f), contentColor = Purple, disabledContainerColor = SurfaceDeep, disabledContentColor = Ink.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
        if (icon != null) {
            val painter = when (icon) { ButtonGlyph.HOME -> painterResource(R.drawable.icon_home); ButtonGlyph.PAUSE -> painterResource(R.drawable.icon_pause) }
            Image(painter, null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(8.dp))
        }
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}
