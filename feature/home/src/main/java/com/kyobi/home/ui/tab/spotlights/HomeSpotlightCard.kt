package com.kyobi.home.ui.tab.spotlights

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.model.TrendingResearch
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.launch

@Composable
fun HomeSpotlightCard(
    item: TrendingResearch,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    val imageData = item.thumbnail?.image
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = remember { Animatable(1f) }
    val glowAlpha = remember { Animatable(0f) }
    val elevation = remember { Animatable(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            launch {
                scale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f)
                )
            }
            launch {
                glowAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(stiffness = 800f, dampingRatio = 0.7f)
                )
            }
            launch {
                elevation.animateTo(
                    targetValue = 12f,
                    animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f)
                )
            }
        } else {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f)
                )
            }
            launch {
                glowAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = 800f, dampingRatio = 0.7f)
                )
            }
            launch {
                elevation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(MaterialTheme.kyobiTheme.shapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
    ) {
        AppImage(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.kyobiTheme.shapes.medium),
            imageUrl = imageData?.url,
            contentDescription = imageData?.altText,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
        )
    }
}