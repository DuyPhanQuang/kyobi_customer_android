package com.kyobi.core.models

import com.kyobi.core.enums.TagType

data class Tag(
    val name: TagType,
    val message: String?,
)
