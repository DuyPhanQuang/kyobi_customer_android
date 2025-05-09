package com.kyobi.home.ui.tab.deals.flip_clock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Flaps(currentText: String, nextText: String, factor: Float) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box {
                Flap(
                    text = nextText,
                    position = FlapPosition.TOP
                )
                if (factor < 0.5F) {
                    val f = factor * 2F
                    Flap(
                        modifier = Modifier.graphicsLayer(
                            rotationX = -90F * f,
                            transformOrigin = TransformOrigin(0.5F, 1F)
                        ),
                        text = currentText,
                        position = FlapPosition.TOP,
                    )
                }
            }
            Box {
                Flap(
                    text = currentText,
                    position = FlapPosition.BOTTOM
                )
                if (factor >= 0.5F) {
                    val f = (1F - factor) * 2F
                    Flap(
                        modifier = Modifier.graphicsLayer(
                            rotationX = 90F * f,
                            transformOrigin = TransformOrigin(0.5F, 0F)
                        ),
                        text = nextText,
                        position = FlapPosition.BOTTOM,
                    )
                }
            }
        }
    }
}