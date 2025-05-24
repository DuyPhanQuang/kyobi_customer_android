package com.kyobi.composable.button

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme

@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    onClick: () -> Unit,
    borderWidth: Dp = Dimension.dp0,
    borderColor: Color? = null,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    size: Dp = Dimension.dp40,
    iconSize: Dp? = null,
    iconColor: Color? = null,
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val finalBorderColor = borderColor ?: Color.Transparent
    val finalIconSize = iconSize ?: iconTheme.lg
    val finalIconColor = iconColor ?: colorTheme.icon.white

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .border(
                width = borderWidth,
                color = finalBorderColor,
                shape = shape),
        enabled = enabled,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(finalIconSize),
            tint = finalIconColor
        )
    }
}