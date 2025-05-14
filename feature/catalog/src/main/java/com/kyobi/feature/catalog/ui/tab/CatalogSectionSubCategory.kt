package com.kyobi.feature.catalog.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
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
fun CatalogSectionSubCategory(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    subCategories: List<String> = List(30) { "Sub $it" }
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .background(MaterialTheme.kyobiTheme.colors.bg.stone100),
        contentPadding = PaddingValues(
            bottom = MaterialTheme.kyobiTheme.spacing.dp84
        )
    ) {
        items(subCategories.size) { index ->
            Text(
                text = subCategories[index],
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}