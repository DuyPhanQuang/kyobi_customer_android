package com.kyobi.composable.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonProductGridView(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    val spacing = MaterialTheme.kyobiTheme.spacing.dp8
    val aspectRatio = 0.668f

    Column(modifier = modifier) {
        val rows = (itemCount + 1) / 2
        repeat(rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                repeat(2) { index ->
                    if (it * 2 + index < itemCount) {
                        SkeletonContainer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(aspectRatio)
                                .clip(MaterialTheme.kyobiTheme.shapes.small)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (it != rows - 1) {
                Spacer(modifier = Modifier.height(spacing))
            }
        }
    }
}