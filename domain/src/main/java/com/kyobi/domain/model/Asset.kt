package com.kyobi.domain.model

data class Assets(
    val assets: List<Asset>,
    val currentPage: Int?,
    val nextPage: Int?,
    val total: Int
)

data class Asset(
    val id: String,
    val label: String,
    val locale: String,
    val tags: List<String>,
    val groups: List<String>,
    val meta: Map<String, String>,
    val payload: Payload,
    val credits: Credits,
    val license: License,
    val utm: Utm
)

data class Payload(
    val sourceSet: List<Source>
)

data class Source(
    val uri: String,
    val width: Int,
    val height: Int
)

data class Utm(
    val source: String,
    val medium: String
)