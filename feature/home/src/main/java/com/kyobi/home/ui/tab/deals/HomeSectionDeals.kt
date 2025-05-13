package com.kyobi.home.ui.tab.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.composable.R
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.domain.model.FlashSale
import com.kyobi.home.ui.tab.deals.flip_clock.Flipper
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs
import com.kyobi.theme.paragraph2Xl
import com.kyobi.theme.paragraphMd

@Composable
fun HomeSectionDeals(
    imageLoader: ImageLoader,
    flashSaleData: FlashSale,
) {
    val backgroundImage = flashSaleData.flashSaleInfo.background?.image
    val endTime = flashSaleData.flashSaleInfo.endTime
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.kyobiTheme.shapes.small)
            .background(Color.Transparent)
            .padding(
                horizontal = MaterialTheme.kyobiTheme.spacing.dp12
            )
    ) {
        AppImage(
            imageUrl = backgroundImage?.url,
            modifier = Modifier
                .zIndex(0f)
                .fillMaxWidth()
                .height(MaterialTheme.kyobiTheme.height.dp284)
                .clip(MaterialTheme.kyobiTheme.shapes.small),
            contentScale = ContentScale.Crop,
            contentDescription = backgroundImage?.altText,
            imageLoader = imageLoader
        )
        Box(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.kyobiTheme.spacing.dp12,
                        end = MaterialTheme.kyobiTheme.spacing.dp12,
                        top = MaterialTheme.kyobiTheme.spacing.dp8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.xxl),
                        painter = painterResource(id = R.drawable.ic_lightning),
                        contentDescription = "Lightning Icon",
                        tint = Color.Unspecified
                    )
                    XxsSpaceX()
                    Text(
                        text = "Super Deals",
                        color = MaterialTheme.kyobiTheme.colors.onPrimary,
                        style = MaterialTheme.kyobiTheme.typography.paragraphMd,
                    )
                    XxsSpaceX()
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Arrow Right Icon",
                        tint = Color.Unspecified
                    )
                }
                if (endTime != null) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Flipper(
                            endTime = endTime
                        )
                    }
                }
            }
            if (flashSaleData.products.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = MaterialTheme.kyobiTheme.spacing.dp8 +
                                    MaterialTheme.kyobiTheme.spacing.dp28 +
                                    MaterialTheme.kyobiTheme.spacing.dp16 +
                                    MaterialTheme.kyobiTheme.spacing.dp40,
                            bottom = MaterialTheme.kyobiTheme.spacing.dp12),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp8),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.kyobiTheme.spacing.dp12
                    )
                ) {
                    items(flashSaleData.products.take(5)) { product ->
                        HomeProductDealCard(
                            product = product,
                            imageLoader = imageLoader
                        )
                    }
                    item {
                        ViewMoreButton(
                            onViewMoreClick = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewMoreButton(
    onViewMoreClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(MaterialTheme.kyobiTheme.width.dp120)
            .height(MaterialTheme.kyobiTheme.width.dp156)
            .padding(
                top = MaterialTheme.kyobiTheme.spacing.dp16
            )
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.kyobiTheme.width.dp48)
                .clip(CircleShape)
                .background(
                    MaterialTheme.kyobiTheme.colors.bg.stone300,
                    shape = CircleShape)
                .clickable { onViewMoreClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "View More Icon",
                modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.lg),
                tint = MaterialTheme.kyobiTheme.colors.bg.stone950
            )
        }
        Text(
            modifier = Modifier
                .padding(top = MaterialTheme.kyobiTheme.spacing.dp4),
            text = "View more",
            color = MaterialTheme.kyobiTheme.colors.text.neutral500,
            style = MaterialTheme.kyobiTheme.typography.labelSmallXs,
            textAlign = TextAlign.Center
        )
    }
}