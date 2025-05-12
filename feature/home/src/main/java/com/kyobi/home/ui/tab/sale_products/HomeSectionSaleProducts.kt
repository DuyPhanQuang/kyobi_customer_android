package com.kyobi.home.ui.tab.sale_products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import coil.ImageLoader
import com.kyobi.composable.R
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.domain.extension.toName
import com.kyobi.domain.model.SaleGroupProduct
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.smallHeader

@Composable
fun HomeSectionSaleProducts(
    modifier: Modifier = Modifier,
    saleProducts: List<SaleGroupProduct>,
    imageLoader: ImageLoader,
) {
    val rowItems = 2
    val spacing = MaterialTheme.kyobiTheme.spacing.dp8
    val horizontalContentPadding = MaterialTheme.kyobiTheme.spacing.dp12
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val containerSizeDp: DpSize = with(density) {
        Size(
            width = windowInfo.containerSize.width.toFloat(),
            height = windowInfo.containerSize.height.toFloat()
        ).toDpSize()
    }
    val itemWidth = (containerSizeDp.width - (spacing * (rowItems - 1)) - (horizontalContentPadding * 2)) / 2

    LazyRow(
        modifier = modifier
            .padding(
                vertical = MaterialTheme.kyobiTheme.spacing.dp8
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp8),
        contentPadding = PaddingValues(horizontal = horizontalContentPadding)
    ) {
        items(saleProducts, key = { it.catalog.id }) { saleGroup ->
            Column(
                modifier = Modifier
                    .width(itemWidth)
                    .clip(MaterialTheme.kyobiTheme.shapes.small)
                    .background(MaterialTheme.kyobiTheme.colors.bg.stone100)
                    .padding(MaterialTheme.kyobiTheme.spacing.dp8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = saleGroup.catalog.toName,
                        style = MaterialTheme.kyobiTheme.typography.smallHeader,
                        color = MaterialTheme.kyobiTheme.colors.bg.logo,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm),
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Arrow Right",
                        tint = MaterialTheme.kyobiTheme.colors.bg.stone400,
                    )
                }
                XsSpaceY()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp4)
                ) {
                    saleGroup.products.take(rowItems).forEach { product ->
                        HomeSaleProductCard(
                            modifier = Modifier
                                .weight(1f),
                            product = product,
                            imageLoader = imageLoader,
                        )
                    }
                }
            }
        }
    }
}