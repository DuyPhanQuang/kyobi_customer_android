package com.kyobi.home.ui.tab.spotlights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.domain.model.TrendingResearch
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphMd

@Composable
fun HomeSectionSpotlights(
    items: List<TrendingResearch>,
    imageLoader: ImageLoader
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.kyobiTheme.spacing.dp12)
    ) {
        XsSpaceY()
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Spotlight",
            style = MaterialTheme.kyobiTheme.typography.paragraphMd,
            color = MaterialTheme.kyobiTheme.colors.onBackground
        )
        XsSpaceY()

        // Giới hạn tối đa 9 item
        val displayedItems = if (items.size > 9) items.take(9) else items
        val showViewMore = items.size > 9
        // Chia items thành 2 cột
        val columnCount = 2
        val columns = List(columnCount) { mutableListOf<TrendingResearch>() }
        displayedItems.forEachIndexed { index, item ->
            columns[index % columnCount].add(item)
        }
        if (showViewMore) {
            columns[1].add(TrendingResearch.empty())
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp16)
        ) {
            columns.forEach { columnItems ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp16)
                ) {
                    columnItems.forEach { item ->
                        if (item.title.isEmpty() && showViewMore && columnItems.indexOf(item) == columnItems.lastIndex) {
                            // ở vị trí cuối cùng của cột 2
                            HomeSpotlightViewMoreCard(
                                onClick = {}
                            )
                        } else {
                            HomeSpotlightCard(
                                item = item,
                                imageLoader = imageLoader,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}