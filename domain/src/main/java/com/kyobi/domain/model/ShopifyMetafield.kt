package com.kyobi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopifyMetafield(
    val id: String,
    val type: String,
    val key: String,
    val value: String,
    val references: ShopifyReferences? = null,
): Parcelable