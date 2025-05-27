package com.kyobi.featurecommon.product.ui.product.header.menu

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.kyobi.core.extensions.toUppercaseFirstChar
import com.kyobi.featurecommon.product.ui.product.header.PinnedHeaderMenuBarType
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs

@Composable
fun ProductSectionPinnedHeaderMenuBarItem(
    menuBarType: PinnedHeaderMenuBarType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val spacing = MaterialTheme.kyobiTheme.spacing
    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    Tab(
        modifier = Modifier.clip(shapeTheme.extraSmall),
        selected = isSelected,
        onClick = onClick,
        selectedContentColor = colorTheme.primary,
        unselectedContentColor = colorTheme.text.stone400
    ) {
        Text(
            modifier = Modifier
                .padding(
                    vertical = spacing.dp8,
                    horizontal = spacing.dp12),
            text = menuBarType.name.toUppercaseFirstChar(),
            style = typographyTheme.labelSmallXs,
            textAlign = TextAlign.Center
        )
    }
}