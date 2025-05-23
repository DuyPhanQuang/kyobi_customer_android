package com.kyobi.composable.product_option

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kyobi.theme.Colors
import com.kyobi.theme.kyobiTheme

@Composable
fun ColorOption(
    modifier: Modifier = Modifier,
    color: Color,
    size: Dp,
    isSelected: Boolean = false,
) {
    val width = MaterialTheme.kyobiTheme.width
    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors

    val basedRoundedColor = if (color == Colors().white) colorTheme.bg.stone950 else colorTheme.bg.white
    val basedSelectedColor = if (color == Colors().black) colorTheme.bg.white else colorTheme.bg.stone950
    val roundedColor = if (isSelected) basedSelectedColor else basedRoundedColor

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorTheme.bg.stone300)
            .padding(spacing.dp1)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color = roundedColor)
                .padding(spacing.dp2),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(color = color),
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Selected Icon",
                        tint = basedSelectedColor,
                        modifier = Modifier
                            .size(size / 2)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}