package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme
import kotlin.math.roundToInt

@Composable
fun CustomDropdown(
    modifier: Modifier = Modifier,
    height: Dp,
    totalSpacing: Dp = Dimension.dp0,
    shapeForButton: Shape? = null,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    contentForButton: @Composable () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    var dropdownOffsetX by remember { mutableFloatStateOf(0f) }
    var dropdownOffsetY by remember { mutableFloatStateOf(0f) }
    var dropdownWidth by remember { mutableStateOf(Dimension.dp0) }
    var dropdownHeight by remember { mutableStateOf(Dimension.dp0) }

    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    val navigationBarHeightDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    val calculateOffsetY = dropdownOffsetY.dp + dropdownHeight + totalSpacing
    val topHeight = calculateOffsetY + statusBarHeightDp + navigationBarHeightDp

    val colorTheme = MaterialTheme.kyobiTheme.colors
    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    dropdownOffsetX = coordinates.positionInParent().x
                    dropdownOffsetY = coordinates.positionInParent().y
                    dropdownWidth = coordinates.size.width.dp
                    dropdownHeight = coordinates.size.height.dp
                }
                .clip(shapeForButton ?: shapeTheme.extraSmall)
                .clickable {
                    onClick()
                    showDropdown = !showDropdown
                }
        ) {
            contentForButton()
        }

        if (showDropdown) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = dropdownOffsetX.roundToInt(),
                    y = calculateOffsetY.value.roundToInt()
                ),
                properties = PopupProperties(focusable = true),
                onDismissRequest = { showDropdown = false }
            ) {
                BoxWithConstraints {
                    val remainingHeight = (maxHeight - topHeight).coerceAtLeast(Dimension.dp0)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(remainingHeight)
                            .background(colorTheme.bg.stone950.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }
}