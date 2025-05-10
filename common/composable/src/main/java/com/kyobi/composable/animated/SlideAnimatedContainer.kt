package com.kyobi.composable.animated

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SlideAnimatedContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isVisible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        isVisible.value = true
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible.value,
        enter = slideInVertically(
            initialOffsetY = { -it } // Bắt đầu từ trên (ngoài màn hình), trượt xuống
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it } // Trượt lên trên khi biến mất
        ),
    ) {
        content()
    }
}