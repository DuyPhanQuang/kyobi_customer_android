package com.kyobi.feature.catalog.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyobi.theme.kyobiTheme

@Composable
fun CatalogSectionCategory(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp84)
            .background(MaterialTheme.kyobiTheme.colors.background)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val topY = 0f
                val bottomY = size.height - strokeWidth
                val borderColor = Color(0xFFF5F5F4)
                // Top border
                drawLine(
                    color = borderColor,
                    start = Offset(0f, topY),
                    end = Offset(size.width, topY),
                    strokeWidth = strokeWidth
                )
                // Bottom border
                drawLine(
                    color = borderColor,
                    start = Offset(0f, bottomY),
                    end = Offset(size.width, bottomY),
                    strokeWidth = strokeWidth
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Category Section")
    }
}