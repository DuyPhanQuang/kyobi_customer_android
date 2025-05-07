package com.kyobi.composable.button

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.kyobi.theme.kyobiTheme

@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    onClick: () -> Unit,
    borderWidth: Dp = MaterialTheme.kyobiTheme.width.dp0,
    borderColor: Color = Color.Transparent,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    size: Dp = MaterialTheme.kyobiTheme.height.dp40,
    iconSize: Dp = MaterialTheme.kyobiTheme.icon.lg,
    iconColor: Color = MaterialTheme.kyobiTheme.colors.icon.white,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        enabled = enabled,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconColor
        )
    }
}