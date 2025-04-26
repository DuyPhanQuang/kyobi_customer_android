package com.kyobi.data.model

import com.kyobi.domain.model.Asset
import com.kyobi.domain.model.Assets
import com.kyobi.domain.model.Credits
import com.kyobi.domain.model.License
import com.kyobi.domain.model.Payload
import com.kyobi.domain.model.Source
import com.kyobi.domain.model.Utm
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AssetsResponse(
    @Json(name = "data") val data: AssetsData
) {
    fun toAssets(): Assets {
        return Assets(
            assets = data.assets.map { asset ->
                Asset(
                    id = asset.id,
                    label = asset.label,
                    locale = asset.locale,
                    tags = asset.tags,
                    groups = asset.groups,
                    meta = asset.meta,
                    payload = Payload(
                        sourceSet = asset.payload.sourceSet.map { source ->
                            Source(source.uri, source.width, source.height)
                        }
                    ),
                    credits = Credits(asset.credits.name, asset.credits.url),
                    license = License(asset.license.name, asset.license.url),
                    utm = Utm(asset.utm.source, asset.utm.medium)
                )
            },
            currentPage = data.currentPage,
            nextPage = data.nextPage,
            total = data.total
        )
    }
}

@JsonClass(generateAdapter = true)
data class AssetsData(
    @Json(name = "assets") val assets: List<AssetResponse>,
    @Json(name = "currentPage") val currentPage: Int?,
    @Json(name = "nextPage") val nextPage: Int?,
    @Json(name = "total") val total: Int
)

@JsonClass(generateAdapter = true)
data class AssetResponse(
    @Json(name = "id") val id: String,
    @Json(name = "label") val label: String,
    @Json(name = "locale") val locale: String,
    @Json(name = "tags") val tags: List<String>,
    @Json(name = "groups") val groups: List<String>,
    @Json(name = "meta") val meta: Map<String, String>,
    @Json(name = "payload") val payload: PayloadResponse,
    @Json(name = "credits") val credits: CreditsResponse,
    @Json(name = "license") val license: LicenseResponse,
    @Json(name = "utm") val utm: UtmResponse
)

@JsonClass(generateAdapter = true)
data class PayloadResponse(
    @Json(name = "sourceSet") val sourceSet: List<SourceResponse>
)

@JsonClass(generateAdapter = true)
data class SourceResponse(
    @Json(name = "uri") val uri: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int
)

@JsonClass(generateAdapter = true)
data class UtmResponse(
    @Json(name = "source") val source: String,
    @Json(name = "medium") val medium: String
)