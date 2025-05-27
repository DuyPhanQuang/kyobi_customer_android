package com.kyobi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopifyImage(
    val altText: String? = null,
    val url: String,
    val width: Float? = null,
    val height: Float? = null,
): Parcelable