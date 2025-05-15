package com.kyobi.composable.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.kyobi.theme.Colors
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonProductCard(
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val aspectRatio = 0.668f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.dp16)
    ) {
        // Placeholder cho hình ảnh
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.kyobiTheme.shapes.small)
                .aspectRatio(aspectRatio)
        )
        // Placeholder cho tiêu đề
        Spacer(modifier = Modifier.height(spacing.dp8))
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(spacing.dp12)
                .clip(MaterialTheme.kyobiTheme.shapes.small)
        )
        // Placeholder cho giá
        Spacer(modifier = Modifier.height(spacing.dp4))
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(spacing.dp10)
                .clip(MaterialTheme.kyobiTheme.shapes.small)
        )
    }
}

@Composable
private fun SkeletonContainer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .background(Colors().stone200)
    )
}