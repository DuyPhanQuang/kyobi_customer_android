package com.kyobi.composable.space

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Dp.SpaceX() = Spacer(
    modifier = Modifier
        .width(this)
)

@Composable
fun XxsSpaceX() = Spacer(modifier = Modifier.width(4.dp))

@Composable
fun XsSpaceX() = Spacer(modifier = Modifier.width(8.dp))

@Composable
fun SmSpaceX() = Spacer(modifier = Modifier.width(12.dp))

@Composable
fun MdSpaceX() = Spacer(modifier = Modifier.width(16.dp))

@Composable
fun LgSpaceX() = Spacer(modifier = Modifier.width(20.dp))

@Composable
fun XlSpaceX() = Spacer(modifier = Modifier.width(24.dp))

@Composable
fun XxlSpaceX() = Spacer(modifier = Modifier.width(28.dp))

@Composable
fun XxxlSpaceX() = Spacer(modifier = Modifier.width(32.dp))