package com.kyobi.home.ui.tab.sale_products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
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
    val itemsPerRow = 2
    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    Column(
        modifier = modifier
            .padding(
                horizontal = spacing.dp12,
                vertical = spacing.dp8),
        verticalArrangement = Arrangement.spacedBy(spacing.dp8)
    ) {
        saleProducts.chunked(itemsPerRow).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp8)
            ) {
                rowItems.forEach { saleGroup ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(spacing.dp8)
                            .clip(shapeTheme.small)
                            .background(colorTheme.bg.stone100)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = saleGroup.catalog.toName,
                                style = typographyTheme.smallHeader,
                                color = colorTheme.bg.logo,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                modifier = Modifier.size(iconTheme.sm),
                                painter = painterResource(id = R.drawable.ic_arrow_right),
                                contentDescription = "Arrow Right Icon",
                                tint = colorTheme.bg.stone400
                            )
                        }
                        XsSpaceY()
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.dp4)
                        ) {
                            val productItems = saleGroup.products.take(itemsPerRow)
                            productItems.forEach { product ->
                                HomeSaleProductCard(
                                    modifier = Modifier.weight(1f),
                                    product = product,
                                    imageLoader = imageLoader,
                                )
                            }
                            // Fill empty space if this row has only 1 item
                            if (productItems.size < itemsPerRow) {
                                repeat(itemsPerRow - productItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                // Fill empty space if this row has only 1 item
                if (rowItems.size < itemsPerRow) {
                    repeat(itemsPerRow - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}