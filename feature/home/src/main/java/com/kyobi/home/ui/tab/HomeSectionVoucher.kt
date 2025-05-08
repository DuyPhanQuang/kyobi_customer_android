package com.kyobi.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyobi.composable.R
import com.kyobi.composable.space.LgSpaceX
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.theme.Colors
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelXs

@Composable
fun HomeSectionVoucher() {
    val appOnlyWidth = MaterialTheme.kyobiTheme.width.dp100

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp36)
            .background(
                MaterialTheme.kyobiTheme.colors.bg.red700,
                RoundedCornerShape(
                    topStart = MaterialTheme.kyobiTheme.width.dp8,
                    topEnd = MaterialTheme.kyobiTheme.width.dp8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.kyobiTheme.spacing.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .width(appOnlyWidth)
                    .fillMaxHeight()
                    .background(
                        Color.Transparent,
                        RoundedCornerShape(
                            topStart = MaterialTheme.kyobiTheme.width.dp8,
                            topEnd = MaterialTheme.kyobiTheme.width.dp8)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crown),
                    contentDescription = "Crown",
                    modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.md),
                    tint = Color.Unspecified
                )
                XxsSpaceX()
                Text(
                    text = "APP ONLY",
                    color = MaterialTheme.kyobiTheme.colors.text.white,
                    style = MaterialTheme.kyobiTheme.typography.labelXs.copy(
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
                    color = MaterialTheme.kyobiTheme.colors.text.white,
                    style = MaterialTheme.kyobiTheme.typography.labelXs.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_3_arrow_right),
                    contentDescription = "Arrow right",
                    modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.md),
                    tint = Color.Unspecified
                )
            }
        }
    }
}