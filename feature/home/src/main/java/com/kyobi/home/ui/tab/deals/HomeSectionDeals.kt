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
import com.kyobi.theme.paragraphMd

@Composable
fun HomeSectionDeals(
    imageLoader: ImageLoader,
    flashSaleData: FlashSale,
) {
    val backgroundImage = flashSaleData.flashSaleInfo.background?.image
    val endTime = flashSaleData.flashSaleInfo.endTime

    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val height = MaterialTheme.kyobiTheme.height

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapeTheme.small)
            .background(Color.Transparent)
            .padding(horizontal = spacing.dp12)
    ) {
        AppImage(
            imageUrl = backgroundImage?.url,
            modifier = Modifier
                .zIndex(0f)
                .fillMaxWidth()
                .height(height.dp284)
                .clip(shapeTheme.small),
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
                        start = spacing.dp12,
                        end = spacing.dp12,
                        top = spacing.dp8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        modifier = Modifier.size(iconTheme.xxl),
                        painter = painterResource(id = R.drawable.ic_lightning),
                        contentDescription = "Lightning Icon",
                        tint = Color.Unspecified
                    )
                    XxsSpaceX()
                    Text(
                        text = "Super Deals",
                        color = colorTheme.onPrimary,
                        style = typographyTheme.paragraphMd,
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
                val paddingTop = spacing.dp8 + spacing.dp28 + spacing.dp16 + spacing.dp40
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = paddingTop,
                            bottom = spacing.dp12),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp8),
                    contentPadding = PaddingValues(
                        start = spacing.dp12)
                ) {
                    val displayProductDeals = flashSaleData.products.take(5)
                    items(
                        displayProductDeals,
                        key = { "deal_product_${it.id}" }
                    ) { product ->
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
    val spacing = MaterialTheme.kyobiTheme.spacing
    val width = MaterialTheme.kyobiTheme.width
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val iconTheme = MaterialTheme.kyobiTheme.icon

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(width.dp120)
            .height(width.dp156)
            .padding(top = spacing.dp16)
    ) {
        Box(
            modifier = Modifier
                .size(width.dp48)
                .clip(CircleShape)
                .background(
                    colorTheme.bg.stone300,
                    shape = CircleShape)
                .clickable { onViewMoreClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "View More Icon",
                modifier = Modifier.size(iconTheme.lg),
                tint = colorTheme.bg.stone950
            )
        }
        Text(
            modifier = Modifier
                .padding(top = spacing.dp4),
            text = "View more",
            color = colorTheme.text.neutral500,
            style = typographyTheme.labelSmallXs,
            textAlign = TextAlign.Center
        )
    }
}