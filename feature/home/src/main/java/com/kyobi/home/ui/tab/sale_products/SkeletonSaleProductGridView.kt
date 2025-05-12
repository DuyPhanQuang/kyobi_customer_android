package com.kyobi.home.ui.tab.sale_products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonSaleProductGridView(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    val spacing = MaterialTheme.kyobiTheme.spacing.dp8
    val horizontalPadding = MaterialTheme.kyobiTheme.spacing.dp12
    val imageAspectRatio = 0.8324f

    Column(
        modifier = modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        val rows = (itemCount + 1) / 2
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                repeat(2) { columnIndex ->
                    val index = rowIndex * 2 + columnIndex
                    if (index < itemCount) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.kyobiTheme.shapes.small)
                                .background(MaterialTheme.kyobiTheme.colors.bg.stone100)
                                .padding(spacing)
                        ) {
                            // Header Skeleton
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SkeletonContainer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(MaterialTheme.kyobiTheme.height.dp16)
                                )
                                Spacer(modifier = Modifier.width(spacing))
                            }
                            Spacer(modifier = Modifier.height(spacing))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp4)
                            ) {
                                repeat(2) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        SkeletonContainer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(imageAspectRatio)
                                                .clip(MaterialTheme.kyobiTheme.shapes.extraSmall)
                                        )
                                        Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp4))
                                        SkeletonContainer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(MaterialTheme.kyobiTheme.height.dp10)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
