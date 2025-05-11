package com.kyobi.home.ui.tab.top_catalogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonTopCatalogGridView(
    modifier: Modifier = Modifier
) {
    val itemsPerRow = 5

    Column(
        modifier = modifier
            .padding(
                vertical = MaterialTheme.kyobiTheme.spacing.dp16
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.kyobiTheme.spacing.dp8),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp12)
        ) {
            repeat(itemsPerRow) {
                SkeletonContainer(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                )
            }
        }
    }
}