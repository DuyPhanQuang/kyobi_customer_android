package com.kyobi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopifyReferences(
    val nodes: List<ShopifyMetaobject>? = emptyList()
): Parcelable