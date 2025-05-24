package com.kyobi.home.ui.tab.spotlights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonSpotlightGridView(
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val height = MaterialTheme.kyobiTheme.height

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = spacing.dp12,
                end = spacing.dp12,
                top = spacing.dp16)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.dp16)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(spacing.dp16)
            ) {
                SkeletonContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp220)
                        .clip(shapeTheme.medium)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(spacing.dp16)
            ) {
                SkeletonContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp330)
                        .clip(shapeTheme.medium)
                )
            }
        }
    }
}