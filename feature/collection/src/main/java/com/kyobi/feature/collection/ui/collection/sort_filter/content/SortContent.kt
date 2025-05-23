package com.kyobi.feature.collection.ui.collection.sort_filter.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyobi.composable.button.ButtonRoundedType
import com.kyobi.composable.button.OutlineButton
import com.kyobi.composable.space.SpaceY
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallSm
import com.kyobi.theme.paragraphRegularXs


data class SortOption(
    val label: String,
    val value: String
)

val mockSorts = listOf(
    SortOption(
        label = "Featured",
        value = "featured"
    ),
    SortOption(
        label = "Price: High to Low",
        value = "high_to_low"
    ),
    SortOption(
        label = "Price: Low to High",
        value = "low_to_high"
    ),
)

@Composable
fun CollectionSortContent(
    sortOptions: List<SortOption>,
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorTheme.background)
            .padding(horizontal = spacing.dp12)
    ) {
        spacing.dp12.SpaceY()
        Text(
            text = "Sort by",
            style = typographyTheme.labelSmallSm,
            color = colorTheme.onBackground,
        )
        spacing.dp12.SpaceY()
        if (sortOptions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp12),
                verticalArrangement = Arrangement.spacedBy(spacing.dp12),
            ) {
                sortOptions.forEach { sortOption ->
                    SortTile(
                        data = sortOption,
                        isSelected = true,
                        onTileClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun SortTile(
    data: SortOption,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onTileClick: () -> Unit
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height

    val basedSelectedColor = colorTheme.bg.stone950
    val finalButtonColor = if (isSelected)
        ButtonDefaults.outlinedButtonColors(
            containerColor = basedSelectedColor,
            contentColor = colorTheme.text.white,
            disabledContainerColor = colorTheme.bg.stone200,
            disabledContentColor = colorTheme.text.neutral950)
        else ButtonDefaults.outlinedButtonColors(
        containerColor = colorTheme.outline,
        contentColor = colorTheme.onSecondary,
        disabledContainerColor = colorTheme.outline,
        disabledContentColor = colorTheme.onSecondary)
    val finalBorderColor = if (isSelected) basedSelectedColor else colorTheme.bg.stone200

    OutlineButton(
        modifier = Modifier
            .wrapContentWidth(),
        enabled = enabled,
        buttonHeight = height.dp32,
        text = data.label,
        textStyle = typographyTheme.paragraphRegularXs,
        borderColor = finalBorderColor,
        buttonColor = finalButtonColor,
        contentPadding = PaddingValues(horizontal = spacing.dp16),
        enableScaleEffect = false,
        roundedType = ButtonRoundedType.SMALL,
        onClick = { onTileClick() }
    )
}