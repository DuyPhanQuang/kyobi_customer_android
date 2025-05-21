package com.kyobi.feature.collection.ui.collection.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu

@Composable
fun CollectionSectionMenu(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    collectionMenus: List<CollectionMenu>,
    selectedCollectionId: String?,
    expanded: Boolean = false,
    onAllClick: () -> Unit,
    onCollapseClick: () -> Unit,
    onMenuItemClick: (CollectionMenu) -> Unit
) {
    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
        },
        label = "CollectionSectionMenuExpandedTransition"
    ) { targetExpanded ->
        if (!targetExpanded) {
            CompactMenuRow(
                modifier = modifier,
                imageLoader = imageLoader,
                collectionMenus = collectionMenus,
                onAllClick = onAllClick,
                selectedCollectionId = selectedCollectionId,
                onItemClick = onMenuItemClick
            )
        } else {
            ExpandedMenuGrid(
                modifier = modifier,
                imageLoader = imageLoader,
                collectionMenus = collectionMenus,
                onCollapseClick = onCollapseClick,
                selectedCollectionId = selectedCollectionId,
                onItemClick = onMenuItemClick
            )
        }
    }
}