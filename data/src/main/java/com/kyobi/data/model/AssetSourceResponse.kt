package com.kyobi.data.model

import com.kyobi.domain.model.AssetSource
import com.kyobi.domain.model.Credits
import com.kyobi.domain.model.License
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AssetSourceResponse(
    @Json(name = "data") val data: AssetSourceData
) {
    fun toAssetSource(): AssetSource {
        return AssetSource(
            id = data.id,
            name = data.name,
            canGetGroups = data.canGetGroups,
            credits = Credits(data.credits.name, data.credits.url),
            license = License(data.license.name, data.license.url),
            canAddAsset = data.canAddAsset,
            canRemoveAsset = data.canRemoveAsset,
            supportedMimeTypes = data.supportedMimeTypes
        )
    }
}

@JsonClass(generateAdapter = true)
data class AssetSourceData(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: Map<String, String>,
    @Json(name = "canGetGroups") val canGetGroups: Boolean,
    @Json(name = "credits") val credits: CreditsResponse,
    @Json(name = "license") val license: LicenseResponse,
    @Json(name = "canAddAsset") val canAddAsset: Boolean,
    @Json(name = "canRemoveAsset") val canRemoveAsset: Boolean,
    @Json(name = "supportedMimeTypes") val supportedMimeTypes: List<String>
)

@JsonClass(generateAdapter = true)
data class CreditsResponse(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class LicenseResponse(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)