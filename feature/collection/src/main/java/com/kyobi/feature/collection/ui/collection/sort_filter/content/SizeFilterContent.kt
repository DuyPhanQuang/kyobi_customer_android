package com.kyobi.feature.collection.ui.collection.sort_filter.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kyobi.composable.button.ButtonRoundedType
import com.kyobi.composable.button.OutlineButton
import com.kyobi.composable.space.SpaceY
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.domain.model.CateFilter
import com.kyobi.feature.collection.extension.isMatchedSizeKey
import com.kyobi.feature.collection.extension.toFilterOptions
import com.kyobi.feature.collection.model.FilterOption
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallSm
import com.kyobi.theme.paragraphRegularXs
import com.kyobi.theme.paragraphXs

@Composable
fun CollectionSizeFilterContent(
    cateFilter: CateFilter? = null,
    selectedFilterOptions: List<FilterOption>,
    toggleSizeFilterOption: (FilterOption) -> Unit,
    onClearClick: () -> Unit,
    onApplyClick: () -> Unit,
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height

    val sizeFilters = cateFilter?.fields
        ?.filter { it.isMatchedSizeKey() }
        ?.toFilterOptions() ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorTheme.background)
            .padding(horizontal = spacing.dp12)
    ) {
        spacing.dp12.SpaceY()
        Text(
            text = "Size",
            style = typographyTheme.labelSmallSm,
            color = colorTheme.onBackground,
        )
        spacing.dp12.SpaceY()
        if (sizeFilters.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp144)
                    .verticalScroll(rememberScrollState())
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = spacing.dp24),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp12),
                    verticalArrangement = Arrangement.spacedBy(spacing.dp12),
                ) {
                    sizeFilters.forEach { sizeFilter ->
                        val isSelected = selectedFilterOptions.any { it.label == sizeFilter.label && it.key == sizeFilter.key }
                        SizeTile(
                            data = sizeFilter,
                            isSelected = isSelected,
                            onTileClick = { toggleSizeFilterOption(sizeFilter) }
                        )
                    }
                }
            }
        }
        if (sizeFilters.isNotEmpty()) {
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
private fun SizeTile(
    data: FilterOption,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onTileClick: () -> Unit
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height

    val finalBorderColor = if (isSelected) colorTheme.bg.stone950 else colorTheme.bg.stone200
    val textColor = if (isSelected) colorTheme.text.neutral950 else colorTheme.text.neutral700
    val textStyle = if (isSelected) typographyTheme.paragraphXs else typographyTheme.paragraphRegularXs

    OutlineButton(
        modifier = Modifier
            .wrapContentWidth(),
        enabled = enabled,
        buttonHeight = height.dp32,
        text = data.label,
        textStyle = textStyle.copy(
            color = textColor
        ),
        borderColor = finalBorderColor,
        buttonColor = ButtonDefaults.outlinedButtonColors(
            containerColor = colorTheme.outline,
            contentColor = colorTheme.onSecondary,
            disabledContainerColor = colorTheme.outline,
            disabledContentColor = colorTheme.onSecondary
        ),
        contentPadding = PaddingValues(horizontal = spacing.dp16),
        enableScaleEffect = false,
        roundedType = ButtonRoundedType.SMALL,
        onClick = { onTileClick() }
    )
}