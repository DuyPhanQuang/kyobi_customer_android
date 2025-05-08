package com.kyobi.home.ui.tab.deals

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallMd
import com.kyobi.theme.paragraphRegularXs
import kotlinx.coroutines.delay

@Composable
fun CountdownFlipTimer(totalSeconds: Int) {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val digits = listOf(
        hours / 10, hours % 10,
        minutes / 10, minutes % 10,
        seconds / 10, seconds % 10
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "End in:",
            color = MaterialTheme.kyobiTheme.colors.onPrimary,
            style = MaterialTheme.kyobiTheme.typography.paragraphRegularXs,
            textAlign = TextAlign.End
        )
        digits.forEachIndexed { index, newDigit ->
            var previousDigit by remember { mutableIntStateOf(newDigit) }
            var isFlipping by remember { mutableStateOf(false) }

            LaunchedEffect(newDigit) {
                if (newDigit != previousDigit) {
                    isFlipping = true
                    delay(700) // đợi animation flip xong
                    previousDigit = newDigit
                    isFlipping = false
                }
            }
            FlipDigit(
                currentDigit = previousDigit,
                targetDigit = newDigit,
                isFlipping = isFlipping,
                modifier = Modifier.padding(MaterialTheme.kyobiTheme.spacing.dp1)
            )
            // Thêm dấu ":" sau giờ và phút
            if (index == 1 || index == 3) {
                Text(
                    text = ":",
                    style = MaterialTheme.kyobiTheme.typography.labelSmallMd.copy(
                        lineHeight = 12.sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.kyobiTheme.colors.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FlipDigit(
    currentDigit: Int,
    targetDigit: Int,
    isFlipping: Boolean,
    modifier: Modifier = Modifier
) {
    val topFlipAngle = remember { Animatable(0f) }
    val bottomFlipAngle = remember { Animatable(90f) }

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            topFlipAngle.snapTo(0f)
            bottomFlipAngle.snapTo(90f)
            topFlipAngle.animateTo(
                -90f,
                tween(durationMillis = 700, easing = LinearOutSlowInEasing)
            )
            bottomFlipAngle.animateTo(
                0f,
                tween(durationMillis = 700, easing = FastOutLinearInEasing)
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Static top face (luôn luôn hiển thị digit hiện tại)
        DigitFace(digit = currentDigit, isTop = true)
        // Top flip
        if (topFlipAngle.value != 0f) {
            DigitFace(digit = currentDigit, isTop = true, angle = topFlipAngle.value)
        }
        // Static bottom face (digit target sau lật)
        DigitFace(digit = targetDigit, isTop = false)
        // Bottom flip
        if (bottomFlipAngle.value != 90f) {
            DigitFace(digit = targetDigit, isTop = false, angle = bottomFlipAngle.value)
        }
    }
}

@Composable
fun DigitFace(
    digit: Int,
    isTop: Boolean,
    angle: Float = 0f
) {
    val rotationX = if (isTop) angle else angle - 90f

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.rotationX = rotationX
                cameraDistance = 8f * density
                transformOrigin = if (isTop) {
                    TransformOrigin(0.5f, 1f)
                } else {
                    TransformOrigin(0.5f, 0f)
                }
            }
            .clipToBounds()
            .background(if (isTop) MaterialTheme.kyobiTheme.colors.primary
            else MaterialTheme.kyobiTheme.colors.onPrimary)
            .height(MaterialTheme.kyobiTheme.height.dp20)
            .width(MaterialTheme.kyobiTheme.width.dp20),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.kyobiTheme.typography.labelSmallMd.copy(
                lineHeight = 12.sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.kyobiTheme.colors.onPrimary
        )
    }
}

@Composable
fun CountdownTimerController() {
    val initialSeconds = 12 * 3600 + 43 * 60 + 12 // ví dụ: 12:43:12
    val remainingTime = remember { mutableIntStateOf(initialSeconds) }

    LaunchedEffect(Unit) {
        while (remainingTime.intValue > 0) {
            delay(1000L)
            remainingTime.intValue -= 1
        }
    }

    CountdownFlipTimer(totalSeconds = remainingTime.intValue)
}