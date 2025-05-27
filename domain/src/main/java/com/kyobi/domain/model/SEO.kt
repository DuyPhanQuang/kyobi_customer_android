package com.kyobi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SEO(
    val title: String? = null,
    val description: String? = null,
): Parcelable