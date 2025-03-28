package com.zeppelin.app.screens._common.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize

@Composable
fun LoadingText(
    length: Int,
    textStyle: TextStyle,
    modifier: Modifier = Modifier, // Allow passing external modifiers
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
    animationDurationMillis: Int = 1000
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val textWidth = size.width.toFloat() + 100f

    val gradientWidth = if (textWidth > 0) textWidth / 3f else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "LoadingTextTransition")

    val translateX by infiniteTransition.animateFloat(
        initialValue = -gradientWidth,
        targetValue = textWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoadingTextTranslateX"
    )


    val shimmerBrush = if (gradientWidth > 0) {
        Brush.linearGradient(
            0f to backgroundColor,
            0.5f to shimmerColor,
            1f to backgroundColor,
            start = Offset(translateX - 100f, 0f),
            end = Offset(translateX + gradientWidth, 0f),

            )
    } else {
        Brush.linearGradient(
            colors = listOf(backgroundColor, backgroundColor)
        )
    }

    val backgroundBrush = Brush.linearGradient(listOf(backgroundColor, backgroundColor))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 15))
            .background(backgroundBrush)
            .onSizeChanged { size = it }
    ) {
        Text(
            modifier = Modifier.background(shimmerBrush),
            text = " ".repeat(length),
            style = textStyle,
            color = Color.Transparent
        )
    }
}