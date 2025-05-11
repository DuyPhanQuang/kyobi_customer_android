package com.kyobi.home.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.kyobi.composable.space.MdSpaceX
import com.kyobi.domain.model.Product
import com.kyobi.home.ui.common.HomeProductCard
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphMd

@Composable
fun HomeSectionRecommendedProducts(
    modifier: Modifier = Modifier,
    items: List<Product>,
    imageLoader: ImageLoader,
    onProductClick: (Product) -> Unit
) {
    val itemsPerRow = 2
    val rows = items.chunked(itemsPerRow)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.kyobiTheme.spacing.dp12)
    ) {
        Text(
            modifier = Modifier
                .padding(
                    vertical = MaterialTheme.kyobiTheme.spacing.dp8
                ),
            text = "For You",
            style = MaterialTheme.kyobiTheme.typography.paragraphMd,
            color = MaterialTheme.kyobiTheme.colors.onBackground
        )
        MdSpaceX()
        rows.forEach { rowItems ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp8),
            ) {
                rowItems.forEach { product ->
                    HomeProductCard(
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.kyobiTheme.spacing.dp16)
                            .weight(1f),  // Chia đều chiều rộng cho mỗi item
                        product = product,
                        imageLoader = imageLoader,
                        onClick = { onProductClick(product) }
                    )
                }
                // Nếu hàng không đủ 2 item, thêm khoảng trống để giữ layout đồng đều
                repeat(itemsPerRow - rowItems.size) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.kyobiTheme.spacing.dp16)
                            .weight(1f)
                    ) {}
                }
            }
        }
    }
}