package com.kyobi.home.ui.tab.recommended_products

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
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.domain.model.Product
import com.kyobi.home.ui.common.HomeProductCard
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphMd

@Composable
fun HomeSectionRecommendedProducts(
    modifier: Modifier = Modifier,
    items: List<Product>,
    imageLoader: ImageLoader,
) {
    val itemsPerRow = 2
    val rows = items.chunked(itemsPerRow)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.kyobiTheme.spacing.dp12,
                end = MaterialTheme.kyobiTheme.spacing.dp12,
                top = MaterialTheme.kyobiTheme.spacing.dp16)
    ) {
        XsSpaceY()
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "For You",
            style = MaterialTheme.kyobiTheme.typography.paragraphMd,
            color = MaterialTheme.kyobiTheme.colors.onBackground
        )
        XsSpaceY()

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
                        onClick = {  }
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