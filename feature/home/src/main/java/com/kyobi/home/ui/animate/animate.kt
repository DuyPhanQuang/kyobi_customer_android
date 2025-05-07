package com.kyobi.home.ui.animate

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@SuppressLint("UnrememberedMutableState")
@Composable
fun animateShapeAsState(
    targetValue: Shape,
    animationSpec: AnimationSpec<Float> = tween()
): State<Shape> {
    val shape = remember { Animatable(0f) }
    val shapeState = remember { mutableStateOf(targetValue) }

    LaunchedEffect(targetValue) {
        shape.animateTo(
            targetValue = if (targetValue == CircleShape) 0f else 1f,
            animationSpec = animationSpec
        )
    }

    return derivedStateOf {
        if (shape.value == 0f) CircleShape
        else RoundedCornerShape((shape.value * 5.dp.value).dp)
    }
}