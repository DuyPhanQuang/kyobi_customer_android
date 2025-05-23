package com.kyobi.feature.collection.ui.collection.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs
import java.util.Locale

@Composable
fun CollectionMenuTile(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    showOnlyLabel: Boolean = false,
    collectionMenu: CollectionMenu,
    isSelected: Boolean = false,
    onItemClick: () -> Unit
) {
    val width = MaterialTheme.kyobiTheme.width
    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    val textFontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

    if (!showOnlyLabel) {
        val background = if (isSelected) colorTheme.bg.red100 else Color.Transparent
        Box(
            modifier = modifier
                .clip(MaterialTheme.kyobiTheme.shapes.extraSmall)
                .background(background)
                .clickable { onItemClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                val imageData = collectionMenu.thumbnailInfo?.image
                AppImage(
                    modifier = Modifier
                        .size(width.dp48)
                        .border(
                            width = width.dp1,
                            color = colorTheme.bg.stone100,
                            shape = CircleShape)
                        .clip(CircleShape)
                        .aspectRatio(1f)
                        .background(
                            color = colorTheme.background,
                            shape = CircleShape),
                    imageUrl = imageData?.url,
                    contentScale = ContentScale.Fit,
                    contentDescription = imageData?.altText,
                    imageLoader = imageLoader
                )
                Text(
                    modifier = Modifier.padding(top = spacing.dp2),
                    text = collectionMenu.title,
                    style = typographyTheme.paragraphXs,
                    color = if (collectionMenu.title.lowercase(Locale.getDefault()) == "sale")
                        colorTheme.text.red700 else
                        colorTheme.onBackground,
                    fontWeight = textFontWeight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    } else {
        val background = if (isSelected) colorTheme.bg.red100 else colorTheme.bg.stone100
        Box(
            modifier = modifier
                .clip(MaterialTheme.kyobiTheme.shapes.extraSmall)
                .background(background)
                .clickable { onItemClick() }
        ) {
            Row(
                modifier = modifier
                    .padding(
                        vertical = spacing.dp8,
                        horizontal = spacing.dp8),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = collectionMenu.title,
                    style = typographyTheme.paragraphXs,
                    color = if (collectionMenu.title.lowercase(Locale.getDefault()) == "sale")
                        colorTheme.text.red700 else
                        colorTheme.onBackground,
                    fontWeight = textFontWeight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}