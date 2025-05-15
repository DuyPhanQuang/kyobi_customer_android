package com.kyobi.feature.collection.ui.tab.category

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.kyobi.domain.model.CategoryMenu

@Composable
fun CollectionSectionCategory(
    modifier: Modifier = Modifier,
    categories: List<CategoryMenu>,
    imageLoader: ImageLoader,
    expanded: Boolean = false,
    onAllClick: () -> Unit = {},
    selectedCategoryId: String?,
    onCollapseClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
        },
        label = "CategoryExpandedTransition"
    ) { targetExpanded ->
        if (!targetExpanded) {
            CompactCategoryRow(
                modifier = modifier,
                categories = categories,
                imageLoader = imageLoader,
                onAllClick = onAllClick,
                selectedCategoryId = selectedCategoryId,
                onCategoryClick = onCategoryClick
            )
        } else {
            ExpandedCategoryGrid(
                modifier = modifier,
                categories = categories,
                imageLoader = imageLoader,
                onCollapseClick = onCollapseClick,
                selectedCategoryId = selectedCategoryId,
                onCategoryClick = onCategoryClick
            )
        }
    }
}



