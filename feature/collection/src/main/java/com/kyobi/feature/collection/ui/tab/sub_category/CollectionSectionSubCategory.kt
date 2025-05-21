package com.kyobi.feature.collection.ui.tab.sub_category

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.kyobi.domain.model.SubcategoryMenu
import com.kyobi.theme.Colors
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.smallTitle

@Composable
fun CollectionSectionSubCategory(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    selectedSubCategoryId: String?,
    bottomPadding: Dp,
    subCategories: List<SubcategoryMenu>,
    onItemClick: (SubcategoryMenu) -> Unit
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .background(colorTheme.bg.stone100),
        contentPadding = PaddingValues(bottom = bottomPadding)
    ) {
        items(
            subCategories,
            key = { "subcategory_${it.id}_${it.filterHandle}" }
        ) { subCategory ->
            val isSelected = subCategory.id == selectedSubCategoryId
            val backgroundColor = if (isSelected) colorTheme.background else colorTheme.bg.stone100
            val stripeWidth = MaterialTheme.kyobiTheme.width.dp3

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .clickable { onItemClick(subCategory) }
            ) {
                if (isSelected) {
                    Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        drawRect(
                            color = Colors().stone950,
                            topLeft = Offset.Zero,
                            size = Size(stripeWidth.toPx(), size.height)
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = spacing.dp20,
                            horizontal = spacing.dp8),
                    text = subCategory.title,
                    style = typographyTheme.smallTitle,
                    color = colorTheme.onBackground,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}