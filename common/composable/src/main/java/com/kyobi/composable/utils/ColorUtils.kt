package com.kyobi.composable.utils

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorUtils {
    fun getColorValue(colorName: String): Color {
        val colorHex = when (colorName.lowercase()) {
            "black" -> "#000000"
            "gold" -> "#E0BE51"
            "sliver" -> "#DDE1E3"
            "orange" -> "#E69B52"
            "purple", "violet" -> "#905CB4"
            "grey" -> "#A0A0A0"
            "white" -> "#FFFFFF"
            "pink" -> "#E688B8"
            "blue" -> "#4982B6"
            "nude" -> "#F4DDCD"
            "yellow" -> "#F2F180"
            "ivory" -> "#F0F0DF"
            "red" -> "#DE4D42"
            "green" -> "#529E3B"
            "brown", "chocolate" -> "#694A34"
            else -> "#000000"
        }
        return Color(colorHex.toColorInt())
    }
}