package com.kyobi.home.ui.tab.deals.flip_clock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import com.kyobi.theme.Dimension
import com.kyobi.theme.headingLg
import com.kyobi.theme.kyobiTheme

@Composable
fun Flap(
    modifier: Modifier = Modifier,
    text: String,
    position: FlapPosition,
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height

    val flapWidth = width.dp36
    val flapHeight = height.dp18

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            colorTheme.primary,
            Color.DarkGray
        )
    )
    val shape = when (position) {
        FlapPosition.TOP -> RoundedCornerShape(
            topStart = Dimension.dp4,
            topEnd = Dimension.dp4,
            bottomStart = Dimension.dp0,
            bottomEnd = Dimension.dp0
        )
        FlapPosition.BOTTOM -> RoundedCornerShape(
            topStart = Dimension.dp0,
            topEnd = Dimension.dp0,
            bottomStart = Dimension.dp4,
            bottomEnd = Dimension.dp4,
        )
    }

    Card(
        modifier = modifier
            .size(width = flapWidth, height = flapHeight),
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .background(backgroundGradient)
                .shadow(
                    elevation = spacing.dp4,
                    shape = shape,
                    ambientColor = colorTheme.primary.copy(alpha = 0.5f),
                    spotColor = colorTheme.primary.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(
                        y = if (position == FlapPosition.TOP) flapHeight / 2 else -flapHeight / 2
                    ),
                text = text,
                style = typographyTheme.headingLg,
                color = colorTheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class FlapPosition {
    TOP, BOTTOM
}