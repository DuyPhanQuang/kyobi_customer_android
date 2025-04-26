package com.kyobi.domain.model

data class AssetSource(
    val id: String,
    val name: Map<String, String>,
    val canGetGroups: Boolean,
    val credits: Credits,
    val license: License,
    val canAddAsset: Boolean,
    val canRemoveAsset: Boolean,
    val supportedMimeTypes: List<String>
)

data class Credits(
    val name: String,
    val url: String
)

data class License(
    val name: String,
    val url: String
)