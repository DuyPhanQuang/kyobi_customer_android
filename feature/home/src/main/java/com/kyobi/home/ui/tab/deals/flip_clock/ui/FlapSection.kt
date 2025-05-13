package com.kyobi.home.ui.tab.deals.flip_clock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphRegularXs

@Composable
fun FlapSection(
    currentValue: Int,
    nextValue: Int,
    factor: Float,
    label: String
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Flaps(
            currentText = currentValue.toString().padStart(2, '0'),
            nextText = nextValue.toString().padStart(2, '0'),
            factor = factor
        )
        Text(
            text = label,
            style = MaterialTheme.kyobiTheme.typography.paragraphRegularXs,
            color = MaterialTheme.kyobiTheme.colors.onPrimary,
            textAlign = TextAlign.Center
        )
    }
}