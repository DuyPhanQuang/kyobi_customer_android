package com.kyobi.feature.collection.ui.tab.category

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
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.model.CategoryMenu
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs
import java.util.Locale

@Composable
fun CollectionCategoryTile(
    modifier: Modifier = Modifier,
    category: CategoryMenu,
    imageLoader: ImageLoader,
    onItemClick: () -> Unit,
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val width = MaterialTheme.kyobiTheme.width

    Column(
        modifier = modifier
            .clickable { onItemClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        AppImage(
            imageUrl = null,
            modifier = Modifier
                .size(width.dp48)
                .clip(CircleShape)
                .border(
                    width = width.dp1,
                    color = MaterialTheme.kyobiTheme.colors.bg.stone100,
                    shape = CircleShape
                )
                .aspectRatio(1f)
                .background(
                    color = MaterialTheme.kyobiTheme.colors.background,
                    shape = CircleShape
                ),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            imageLoader = imageLoader
        )
        Text(
            modifier = Modifier
                .padding(top = spacing.dp4),
            text = category.title,
            style = MaterialTheme.kyobiTheme.typography.paragraphXs,
            color = if (category.title.lowercase(Locale.getDefault()) == "sale")
                MaterialTheme.kyobiTheme.colors.text.red700 else
                MaterialTheme.kyobiTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}