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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonSaleProductGridView(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val height = MaterialTheme.kyobiTheme.height
    val colorTheme = MaterialTheme.kyobiTheme.colors

    val horizontalPadding = spacing.dp12
    val imageAspectRatio = 0.8324f

    Column(
        modifier = modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.dp8)
    ) {
        val rows = (itemCount + 1) / 2
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp8)
            ) {
                repeat(2) { columnIndex ->
                    val index = rowIndex * 2 + columnIndex
                    if (index < itemCount) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(spacing.dp8)
                                .clip(shapeTheme.small)
                                .background(colorTheme.bg.stone100)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SkeletonContainer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(height.dp16)
                                )
                                XsSpaceX()
                            }
                            XsSpaceY()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing.dp4)
                            ) {
                                repeat(2) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        SkeletonContainer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(imageAspectRatio)
                                                .clip(shapeTheme.extraSmall)
                                        )
                                        Spacer(modifier = Modifier.height(height.dp4))
                                        SkeletonContainer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(height.dp10)
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
