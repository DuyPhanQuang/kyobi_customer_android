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
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallSm
import com.kyobi.theme.paragraphRegularXs

data class FilterOption(
    val label: String,
    val value: String,
    val code: String? = null
)

val mockSizeFilters = listOf(
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "One size",
        value = "oversize"
    ),
    FilterOption(
        label = "XXS",
        value = "xxs"
    ),
    FilterOption(
        label = "XS",
        value = "xs"
    ),
    FilterOption(
        label = "S",
        value = "s"
    ),
    FilterOption(
        label = "M",
        value = "m"
    ),
    FilterOption(
        label = "L",
        value = "l"
    ),
)

@Composable
fun CollectionSizeFilterContent(
    sizeFilters: List<FilterOption>
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height

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
                    sizeFilters.forEach { option ->
                        OutlineButton(
                            modifier = Modifier
                                .wrapContentWidth(),
                            buttonHeight = height.dp32,
                            text = option.label,
                            textStyle = typographyTheme.paragraphRegularXs,
                            borderColor = colorTheme.bg.stone200,
                            buttonColor = ButtonDefaults.outlinedButtonColors(
                                containerColor = colorTheme.outline,
                                contentColor = colorTheme.onSecondary,
                                disabledContainerColor = colorTheme.outline,
                                disabledContentColor = colorTheme.onSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = spacing.dp16),
                            enableScaleEffect = false,
                            roundedType = ButtonRoundedType.SMALL,
                            onClick = {}
                        )
                    }
                }
            }
        }
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
                onClick = {}
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
                onClick = {}
            )
        }
    }
}