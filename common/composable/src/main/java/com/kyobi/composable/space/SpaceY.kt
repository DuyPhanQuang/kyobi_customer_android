package com.kyobi.composable.space

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Dp.SpaceY() = Spacer(
    modifier = Modifier
        .height(this)
)

@Composable
fun XxsSpaceY() = Spacer(modifier = Modifier.height(4.dp))

@Composable
fun XsSpaceY() = Spacer(modifier = Modifier.height(8.dp))

@Composable
fun SmSpaceY() = Spacer(modifier = Modifier.height(16.dp))

@Composable
fun MdSpaceY() = Spacer(modifier = Modifier.height(24.dp))

@Composable
fun LgSpaceY() = Spacer(modifier = Modifier.height(32.dp))

@Composable
fun XlSpaceY() = Spacer(modifier = Modifier.height(40.dp))