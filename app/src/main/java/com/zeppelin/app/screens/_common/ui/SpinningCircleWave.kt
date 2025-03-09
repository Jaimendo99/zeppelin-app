package com.zeppelin.app.screens._common.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R

@Composable
fun SpinningCircleWave(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primaryContainer,
    direction: Int = 1
) {
    val infiniteTransition = rememberInfiniteTransition()
    val initialValue = if (direction == 1) 0f else 360f
    val targetValue = if (direction == 1) 360f else 0f
    val rotation by infiniteTransition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.wavy_cirlce),
            contentDescription = "Wavy Circle",
            modifier = modifier
                .size(200.dp)
                .rotate(rotation),
            tint = tint
        )
    }
}