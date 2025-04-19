package com.kyobi.composable.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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
    text: String? = null,
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

    OutlinedButton(
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= debounceTime) {
                lastClickTime = currentTime
                scope.launch { onClick() }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (enableScaleEffect) Modifier.scale(scale) else Modifier)
            .border(
                1.dp,
                MaterialTheme.kyobiTheme.colors.border.stone950,
                MaterialTheme.kyobiTheme.shapes.extraSmall
            ).pointerInput(Unit) {
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
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = if (roundedType == ButtonRoundedType.LARGE)
                        MaterialTheme.kyobiTheme.shapes.extraLarge
                    else MaterialTheme.kyobiTheme.shapes.extraSmall
                ) else Modifier),
        enabled = enabled && !isLoading,
        shape = if (roundedType == ButtonRoundedType.LARGE)
            MaterialTheme.kyobiTheme.shapes.extraLarge
        else MaterialTheme.kyobiTheme.shapes.extraSmall,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.kyobiTheme.colors.outline,
            contentColor = MaterialTheme.kyobiTheme.colors.onSecondary,
            disabledContainerColor = MaterialTheme.kyobiTheme.colors.outline,
            disabledContentColor = MaterialTheme.kyobiTheme.colors.onSecondary
        ),
        contentPadding = if (text == null && !isLoading) PaddingValues(0.dp) else ButtonDefaults.ContentPadding
    ) {
        if (isLoading && isShowLoadingOnly) {
            CircularProgressIndicator(
                color = MaterialTheme.kyobiTheme.colors.onSecondary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (text == null) Arrangement.Center else Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        painter = leadingIcon,
                        contentDescription = "Leading Icon",
                        modifier = Modifier
                            .size(MaterialTheme.kyobiTheme.icon.sm)
                            .padding(end = if (text != null) MaterialTheme.kyobiTheme.spacing.space8 else 0.dp),
                        tint = leadingIconColor ?: MaterialTheme.kyobiTheme.colors.onSecondary
                    )
                }
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.kyobiTheme.typography.labelLarge,
                        color = MaterialTheme.kyobiTheme.colors.onSecondary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                if (trailingIcon != null) {
                    Icon(
                        painter = trailingIcon,
                        contentDescription = "Trailing Icon",
                        modifier = Modifier
                            .size(MaterialTheme.kyobiTheme.icon.sm)
                            .padding(start = if (text != null) MaterialTheme.kyobiTheme.spacing.space8 else 0.dp),
                        tint = trailingIconColor ?: MaterialTheme.kyobiTheme.colors.onSecondary
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.kyobiTheme.colors.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}