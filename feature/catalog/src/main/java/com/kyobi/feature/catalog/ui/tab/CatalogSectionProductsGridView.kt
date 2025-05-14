package com.kyobi.feature.catalog.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyobi.theme.kyobiTheme

@Composable
fun CatalogSectionProductsGridView(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState
) {

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .background(MaterialTheme.kyobiTheme.colors.background),
        contentPadding = PaddingValues(
            bottom = MaterialTheme.kyobiTheme.spacing.dp84
        )
    ) {
        items(50) {
            Text(text = "Product Item $it", modifier = Modifier.padding(8.dp))
        }
    }
}