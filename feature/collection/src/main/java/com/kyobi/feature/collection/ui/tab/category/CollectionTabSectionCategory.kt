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
fun CollectionTabSectionCategory(
    modifier: Modifier = Modifier,
    categories: List<CategoryMenu>,
    imageLoader: ImageLoader,
    expanded: Boolean = false,
    onAllClick: () -> Unit,
    selectedCategoryId: String?,
    onCollapseClick: () -> Unit,
    onCategoryClick: (CategoryMenu) -> Unit
) {
    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
        },
        label = "CollectionSectionCategoryExpandedTransition"
    ) { targetExpanded ->
        if (!targetExpanded) {
            CompactCategoryRow(
                modifier = modifier,
                categories = categories,
                imageLoader = imageLoader,
                onAllClick = onAllClick,
                selectedCategoryId = selectedCategoryId,
                onItemClick = onCategoryClick
            )
        } else {
            ExpandedCategoryGrid(
                modifier = modifier,
                categories = categories,
                imageLoader = imageLoader,
                onCollapseClick = onCollapseClick,
                selectedCategoryId = selectedCategoryId,
                onItemClick = onCategoryClick
            )
        }
    }
}



