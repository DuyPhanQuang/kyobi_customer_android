package com.kyobi.composable.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

@Composable
fun getImageHeightByAspectRatio(aspectRatio: Float): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthPx = windowInfo.containerSize.width
    val screenWidthDp = with(density) { screenWidthPx.toDp() }
    return screenWidthDp / aspectRatio
}