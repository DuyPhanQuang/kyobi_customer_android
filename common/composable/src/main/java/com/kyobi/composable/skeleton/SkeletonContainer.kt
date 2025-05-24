package com.kyobi.composable.skeleton

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonContainer(
    modifier: Modifier = Modifier,
    width: Dp = Dimension.dp0,
    height: Dp = Dimension.dp0,
    border: Shape? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SkeletonContainerAnimation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SkeletonContainerAlpha"
    )

    val colorTheme = MaterialTheme.kyobiTheme.colors

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.2f),
            Color.Gray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.2f)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 0f)
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .then(border?.let { Modifier.clip(it) } ?: Modifier)
            .background(gradientBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(border?.let { Modifier.clip(it) } ?: Modifier)
                .background(colorTheme.bg.white.copy(alpha = alpha))
        )
    }
}