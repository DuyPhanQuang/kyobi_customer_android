package com.kyobi.feature.collection.ui.collection.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    collectionMenu: CollectionMenu,
    isSelected: Boolean = false,
    onItemClick: () -> Unit,
    ) {

    val width = MaterialTheme.kyobiTheme.width
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    Column(
        modifier = modifier.clickable { onItemClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        val imageData = collectionMenu.thumbnailInfo?.image
        AppImage(
            imageUrl = imageData?.url,
            modifier = Modifier
                .size(width.dp48)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) width.dp2 else width.dp1,
                    color = if (isSelected) colorTheme.bg.stone950 else colorTheme.bg.stone100,
                    shape = CircleShape)
                .aspectRatio(1f)
                .background(
                    color = colorTheme.background,
                    shape = CircleShape),
            contentScale = ContentScale.Fit,
            contentDescription = imageData?.altText,
            imageLoader = imageLoader
        )
        Text(
            text = collectionMenu.title,
            style = typographyTheme.paragraphXs,
            color = if (collectionMenu.title.lowercase(Locale.getDefault()) == "sale")
                colorTheme.text.red700 else
                colorTheme.onBackground,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}