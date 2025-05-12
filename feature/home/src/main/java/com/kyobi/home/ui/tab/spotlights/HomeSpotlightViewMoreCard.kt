package com.kyobi.home.ui.tab.spotlights

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs

@Composable
fun HomeSpotlightViewMoreCard(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.kyobiTheme.shapes.medium)
            .clickable(
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.kyobiTheme.width.dp48)
                .clip(CircleShape)
                .background(
                    MaterialTheme.kyobiTheme.colors.bg.white,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "View More Icon",
                modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.xl),
                tint = MaterialTheme.kyobiTheme.colors.bg.stone950
            )
        }
        Text(
            modifier = Modifier
                .padding(top = MaterialTheme.kyobiTheme.spacing.dp4),
            text = "View More",
            color = MaterialTheme.kyobiTheme.colors.text.neutral950,
            style = MaterialTheme.kyobiTheme.typography.labelSmallXs,
        )
    }
}