package com.kyobi.home.ui.tab.banners

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.kyobi.composable.R
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelXs

@Composable
fun HomeSectionVoucher() {
    val appOnlyWidth = MaterialTheme.kyobiTheme.width.dp100
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp36)
            .background(
                colorTheme.bg.red700,
                RoundedCornerShape(topStart = width.dp8, topEnd = width.dp8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .width(appOnlyWidth)
                    .fillMaxHeight()
                    .background(
                        Color.Transparent,
                        RoundedCornerShape(topStart = width.dp8, topEnd = width.dp8)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crown),
                    contentDescription = "Crown",
                    modifier = Modifier.size(iconTheme.md),
                    tint = Color.Unspecified
                )
                XxsSpaceX()
                Text(
                    text = "APP ONLY",
                    color = colorTheme.text.white,
                    style = typographyTheme.labelXs.copy(
                        lineHeight = 20.sp
                    )
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_draw_voucher),
                contentDescription = "Draw voucher",
                tint = Color.Unspecified
            )
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "FREE SHIPPING OVER $50",
                    color = colorTheme.text.white,
                    style = typographyTheme.labelXs.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_3_arrow_right),
                    contentDescription = "Arrow right",
                    modifier = Modifier.size(iconTheme.md),
                    tint = Color.Unspecified
                )
            }
        }
    }
}