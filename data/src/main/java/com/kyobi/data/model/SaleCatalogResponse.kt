package com.kyobi.data.model

import com.kyobi.domain.model.SaleCatalog
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SaleCatalogResponse(
    @Json(name = "id") val id: String,
    @Json(name = "handle") val handle: String,
    @Json(name = "display") val display: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
) {
    fun toSaleCatalog(): SaleCatalog {
        return SaleCatalog(
            id = id,
            handle = handle,
            display = display,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}