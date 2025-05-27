package com.kyobi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopifyMetaobject(
    val id: String,
    val handle: String,
    val type: String? = null,
    val fields: List<ShopifyMetaobjectField>? = emptyList()
): Parcelable

@Parcelize
data class ShopifyMetaobjectField(
    val key: String,
    val value: String? = null
): Parcelable