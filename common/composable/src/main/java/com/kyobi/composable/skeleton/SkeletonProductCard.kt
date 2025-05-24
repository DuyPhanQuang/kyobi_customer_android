package com.kyobi.composable.skeleton

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
import com.kyobi.theme.kyobiTheme

@Composable
fun SkeletonProductCard(
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val shape = MaterialTheme.kyobiTheme.shapes
    val aspectRatio = 0.668f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.dp16)
    ) {
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape.small)
                .aspectRatio(aspectRatio)
        )
        Spacer(modifier = Modifier.height(spacing.dp8))
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(spacing.dp12)
                .clip(shape.small)
        )
        Spacer(modifier = Modifier.height(spacing.dp4))
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(spacing.dp10)
                .clip(shape.small)
        )
    }
}