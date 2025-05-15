package com.kyobi.feature.catalog.ui.tab.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import com.kyobi.composable.space.SmSpaceY
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.domain.model.TopCatalog
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs

@Composable
fun ExpandedCategoryGrid(
    modifier: Modifier,
    categories: List<TopCatalog>,
    imageLoader: ImageLoader,
    onCollapseClick: () -> Unit
) {
    val spacing = MaterialTheme.kyobiTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.kyobiTheme.colors.bg.stone300)
    ) {
        val columns = 5
        val rows = (categories.size + columns - 1) / columns
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.dp12),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp16)
            ) {
                for (colIndex in 0 until columns) {
                    val index = rowIndex * columns + colIndex
                    if (index < categories.size) {
                        CategoryTile(
                            modifier = Modifier.weight(1f),
                            catalog = categories[index],
                            imageLoader = imageLoader,
                            onItemClick = { }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        SmSpaceY()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .clickable { onCollapseClick() },
            text = "Collapse",
            style = MaterialTheme.kyobiTheme.typography.labelSmallXs,
            color = MaterialTheme.kyobiTheme.colors.primary,
            textAlign = TextAlign.Center
        )
        SmSpaceY()
    }
}
