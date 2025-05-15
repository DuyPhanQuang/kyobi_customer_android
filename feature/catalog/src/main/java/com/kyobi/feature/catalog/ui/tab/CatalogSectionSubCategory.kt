package com.kyobi.feature.catalog.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.kyobi.theme.Colors
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

@Composable
fun CatalogSectionSubCategory(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    subCategories: List<String> = List(30) { "Sub $it" },
    selectedIndex: Int = 0,
    onItemClick: (Int) -> Unit = {},
    bottomPadding: Dp
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .background(MaterialTheme.kyobiTheme.colors.bg.stone100),
        contentPadding = PaddingValues(
            bottom = bottomPadding
        )
    ) {
        items(subCategories.size) { index ->
            val isSelected = index == selectedIndex
            val backgroundColor = if (isSelected) MaterialTheme.kyobiTheme.colors.background else
                MaterialTheme.kyobiTheme.colors.bg.stone100
            val stripeWidth = MaterialTheme.kyobiTheme.width.dp3

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .clickable { onItemClick(index) }
            ) {
                if (isSelected) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        drawRect(
                            color = Colors().stone950,
                            topLeft = Offset.Zero,
                            size = Size(stripeWidth.toPx(), size.height)
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = MaterialTheme.kyobiTheme.spacing.dp20,
                            horizontal = MaterialTheme.kyobiTheme.spacing.dp12),
                    text = subCategories[index],
                    style = MaterialTheme.kyobiTheme.typography.paragraphXs,
                    color = MaterialTheme.kyobiTheme.colors.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}