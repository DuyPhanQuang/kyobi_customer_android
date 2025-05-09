package com.kyobi.home.ui.tab.deals.flip_clock.ui

import androidx.compose.runtime.Composable

@Composable
fun FlapSection(
    currentValue: Int,
    nextValue: Int,
    factor: Float
) {
    Flaps(
        currentText = currentValue.toString().padStart(2, '0'),
        nextText = nextValue.toString().padStart(2, '0'),
        factor = factor
    )
}