package com.kyobi.feature.collection.ui.collection.sort_filter.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.kyobi.composable.button.ButtonRoundedType
import com.kyobi.composable.button.OutlineButton
import com.kyobi.composable.product_option.ColorOption
import com.kyobi.composable.space.SpaceY
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.domain.model.CateFilter
import com.kyobi.feature.collection.extension.isMatchedColorKey
import com.kyobi.feature.collection.extension.toColorFilterOptions
import com.kyobi.feature.collection.model.FilterOption
import com.kyobi.theme.Colors
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallSm
import com.kyobi.theme.paragraphRegularXs
import com.kyobi.theme.paragraphXs
import androidx.core.graphics.toColorInt

@Composable
fun CollectionColorFilterContent(
    cateFilter: CateFilter? = null,
    selectedFilterOptions: List<FilterOption>,
    toggleColorFilterOption: (FilterOption) -> Unit,
    onClearClick: () -> Unit,
    onApplyClick: () -> Unit,
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height

    val colorFilters = cateFilter?.fields
        ?.filter { it.isMatchedColorKey() }
        ?.toColorFilterOptions() ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorTheme.background)
            .padding(horizontal = spacing.dp12)
    ) {
        spacing.dp12.SpaceY()
        Text(
            text = "Color",
            style = typographyTheme.labelSmallSm,
            color = colorTheme.onBackground,
        )
        spacing.dp12.SpaceY()
        if (colorFilters.isNotEmpty()) {
            val itemsPerRow = 4
            val rows = colorFilters.chunked(itemsPerRow)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp144)
                    .verticalScroll(rememberScrollState())
            ) {
                rows.forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { colorFilter ->
                            val isSelected = selectedFilterOptions.any { it.label == colorFilter.label && it.key == colorFilter.key }
                            ColorFilterTile(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = spacing.dp8),
                                data = colorFilter,
                                isSelected = isSelected,
                                onTileClick = { toggleColorFilterOption(colorFilter) }
                            )
                        }
                        repeat(itemsPerRow - rowItems.size) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = spacing.dp8)
                            ) {}
                        }
                    }
                }
            }
        }
        if (colorFilters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlineButton(
                    modifier = Modifier
                        .wrapContentWidth(),
                    buttonHeight = height.dp36,
                    text = "Clear",
                    textStyle = typographyTheme.paragraphRegularXs,
                    borderColor = colorTheme.bg.stone200,
                    contentPadding = PaddingValues(horizontal = spacing.dp24),
                    roundedType = ButtonRoundedType.LARGE,
                    onClick = { onClearClick() }
                )
                XsSpaceX()
                OutlineButton(
                    modifier = Modifier
                        .wrapContentWidth(),
                    buttonHeight = height.dp36,
                    text = "Apply",
                    textStyle = typographyTheme.paragraphRegularXs,
                    borderColor = colorTheme.bg.stone200,
                    contentPadding = PaddingValues(horizontal = spacing.dp24),
                    roundedType = ButtonRoundedType.LARGE,
                    onClick = { onApplyClick() }
                )
            }
        }
    }
}

@Composable
private fun ColorFilterTile(
    modifier: Modifier = Modifier,
    data: FilterOption,
    isSelected: Boolean = false,
    onTileClick: () -> Unit
) {
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val width = MaterialTheme.kyobiTheme.width
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val spacing = MaterialTheme.kyobiTheme.spacing

    val colorValue = data.code?.let { hexColor -> Color(hexColor.toColorInt()) } ?: Colors().black
    val textStyle = if (isSelected) typographyTheme.paragraphXs else typographyTheme.paragraphRegularXs
    val textColor = if (isSelected) colorTheme.text.neutral950 else colorTheme.text.neutral700

    Row(
        modifier = modifier
            .clip(shapeTheme.extraSmall)
            .clickable { onTileClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.padding(spacing.dp4)
        ) {
            ColorOption(
                color = colorValue,
                isSelected = isSelected,
                size = width.dp24,
            )
        }
        Text(
            text = data.label,
            style = textStyle,
            color = textColor
        )
    }
}