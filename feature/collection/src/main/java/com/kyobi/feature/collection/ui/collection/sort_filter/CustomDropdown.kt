package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
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
fun <T : Enum<T>> CustomDropdown(
    modifier: Modifier = Modifier,
    height: Dp,
    totalSpacing: Dp = Dimension.dp0,
    shapeForButton: Shape? = null,
    focusable: Boolean = true,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit,
    onSwitch: (T) -> Unit,
    type: T,
    currentActive: T?,
    onDismissRequest: (() -> Unit)? = {},
    popupContent: @Composable () -> Unit,
    child: @Composable () -> Unit
) {
    var dropdownOffsetX by remember { mutableStateOf(Dimension.dp0) }
    var dropdownOffsetY by remember { mutableStateOf(Dimension.dp0) }
    var dropdownWidth by remember { mutableStateOf(Dimension.dp0) }
    var dropdownHeight by remember { mutableStateOf(Dimension.dp0) }
    var animationState by remember { mutableStateOf(isActive) }

    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    val navigationBarHeightDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val calculateOffsetY = dropdownOffsetY + dropdownHeight + totalSpacing
    val topHeight = calculateOffsetY + statusBarHeightDp + navigationBarHeightDp

    val colorTheme = MaterialTheme.kyobiTheme.colors
    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    LaunchedEffect(currentActive, isActive) {
        if (isActive) {
            animationState = true
        } else if (currentActive != type) {
            animationState = false
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    dropdownOffsetX = coordinates.positionInParent().x.dp
                    dropdownOffsetY = coordinates.positionInParent().y.dp
                    dropdownWidth = coordinates.size.width.dp
                    dropdownHeight = coordinates.size.height.dp
                }
                .clip(shapeForButton ?: shapeTheme.extraSmall)
                .clickable {
                    if (currentActive != null && currentActive != type) {
                        onToggle(false) // Đóng dropdown hiện tại
                        onSwitch(type) // Chuyển sang dropdown mới
                    } else {
                        onToggle(!isActive) // Toggle nếu không có dropdown khác
                    }
                }
        ) {
            child()
        }

        if (animationState) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = dropdownOffsetX.value.roundToInt(),
                    y = calculateOffsetY.value.roundToInt()
                ),
                properties = PopupProperties(focusable = focusable),
                onDismissRequest = onDismissRequest,
            ) {
                BoxWithConstraints {
                    val remainingHeight = (maxHeight - topHeight).coerceAtLeast(Dimension.dp0)
                    AnimatedVisibility(
                        visible = isActive,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
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
                                popupContent()
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            if (offset.y > height.value) {
                                                onToggle(false)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}