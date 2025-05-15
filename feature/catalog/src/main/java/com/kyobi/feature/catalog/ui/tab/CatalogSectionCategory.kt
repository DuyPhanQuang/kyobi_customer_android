package com.kyobi.feature.catalog.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.kyobi.composable.R
import com.kyobi.domain.model.TopCatalog
import com.kyobi.feature.catalog.ui.tab.category.CategoryTile
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs

@Composable
fun CatalogSectionCategory(
    modifier: Modifier = Modifier,
    categories: List<TopCatalog>,
    imageLoader: ImageLoader,
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
        val allContainerWidth = MaterialTheme.kyobiTheme.spacing.dp48
        LazyRow(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp16),
            contentPadding = PaddingValues(
                start = MaterialTheme.kyobiTheme.spacing.dp12,
                end = MaterialTheme.kyobiTheme.spacing.dp12 + allContainerWidth)
        ) {
            items(categories) { category ->
                CategoryTile(
                    catalog = category,
                    imageLoader = imageLoader,
                    onItemClick = {  }
                )
            }
        }
        Box(
            modifier = Modifier
                .width(allContainerWidth)
                .fillMaxHeight()
                .padding(vertical = MaterialTheme.kyobiTheme.spacing.dp1)
                .align(Alignment.TopEnd)
                .drawBehind {
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        setShadowLayer(4f, -2f, 0f, android.graphics.Color.argb(25, 0, 0, 0)) // blur 4px, offset -2px
                    }
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        drawRect(0f, 0f, size.width, size.height, paint)
                        restore()
                    }
                }
                .graphicsLayer {
                    shadowElevation = 0f
                    shape = RectangleShape
                    clip = false
                }
                .background(MaterialTheme.kyobiTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = MaterialTheme.kyobiTheme.spacing.dp12)
                    .clickable {  },
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.lg),
                    painter = painterResource(id = R.drawable.ic_category),
                    contentDescription = "All Category Icon",
                    tint = Color.Unspecified
                )
                Text(
                    text = "All",
                    style = MaterialTheme.kyobiTheme.typography.labelSmallXs,
                    color = MaterialTheme.kyobiTheme.colors.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}