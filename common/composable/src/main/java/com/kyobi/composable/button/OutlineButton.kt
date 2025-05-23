package com.kyobi.composable.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.launch

/* How to use
* OutlineButton(
        text = "ADD TO WISHLIST",
        leadingIcon = R.drawable.l,
        trailingIcon = R.drawable.t,
        onClick = {
            isLoading = true
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                isLoading = false
            }
        },
        isLoading = isLoading
    )
* */
@Composable
fun OutlineButton(
    modifier: Modifier = Modifier,
    buttonHeight: Dp? = null,
    text: String? = null,
    textStyle: TextStyle,
    borderColor: Color? = null,
    buttonColor: ButtonColors? = null,
    contentPadding: PaddingValues? = null,
    leadingIcon: Painter? = null,
    leadingIconColor: Color? = null,
    trailingIcon: Painter? = null,
    trailingIconColor: Color? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isShowLoadingOnly: Boolean = false,
    debounceTime: Long = 500L,
    enableScaleEffect: Boolean = true,
    roundedType: ButtonRoundedType = ButtonRoundedType.LARGE
) {
    val scope = rememberCoroutineScope()
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }

    // Bounce animation: Scale down to 0.95, then bounce back to 1.0
    val scale by animateFloatAsState(
        targetValue = if (enableScaleEffect && isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "Button Scale"
    )

    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconTheme = MaterialTheme.kyobiTheme.icon

    val buttonShape = if (roundedType == ButtonRoundedType.LARGE) CircleShape else shapeTheme.extraSmall
    val finalButtonHeight = buttonHeight ?: height.dp48
    val finalContentPadding = contentPadding ?: ButtonDefaults.ContentPadding
    val finalBorderColor = borderColor ?: colorTheme.border.stone950
    val finalButtonColors = buttonColor ?: ButtonDefaults.outlinedButtonColors(
        containerColor = colorTheme.outline,
        contentColor = colorTheme.onSecondary,
        disabledContainerColor = colorTheme.outline,
        disabledContentColor = colorTheme.onSecondary
    )

    OutlinedButton(
        modifier = modifier
            .height(finalButtonHeight)
            .clip(buttonShape)
            .then(if (enableScaleEffect) Modifier.scale(scale) else Modifier)
            .border(
                width.dp1,
                finalBorderColor,
                buttonShape
            )
            .padding(spacing.dp0)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .then(
                if (isPressed)
                    Modifier.background(
                        color = colorTheme.bg.stone950.copy(alpha = 0.5f),
                        shape = buttonShape)
                else Modifier),
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= debounceTime) {
                lastClickTime = currentTime
                scope.launch { onClick() }
            }
        },
        enabled = enabled && !isLoading,
        shape = buttonShape,
        colors = finalButtonColors,
        contentPadding =  if (text == null && !isLoading) PaddingValues(spacing.dp0) else finalContentPadding
    ) {
        if (isLoading && isShowLoadingOnly) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconTheme.lg),
                color = colorTheme.onSecondary,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (text == null) Arrangement.Center else Arrangement.SpaceBetween,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        modifier = Modifier
                            .size(iconTheme.sm)
                            .padding(
                                end = if (text != null) spacing.dp8 else spacing.dp0
                            ),
                        painter = leadingIcon,
                        contentDescription = "Leading Icon",
                        tint = leadingIconColor ?: LocalContentColor.current
                    )
                }
                if (text != null) {
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = text,
                        style = textStyle,
                    )
                }
                if (trailingIcon != null) {
                    Icon(
                        modifier = Modifier
                            .size(iconTheme.sm)
                            .padding(
                                start = if (text != null) spacing.dp8 else spacing.dp0
                            ),
                        painter = trailingIcon,
                        contentDescription = "Trailing Icon",
                        tint = trailingIconColor ?: LocalContentColor.current
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconTheme.lg),
                    color = colorTheme.onSecondary,
                )
            }
        }
    }
}