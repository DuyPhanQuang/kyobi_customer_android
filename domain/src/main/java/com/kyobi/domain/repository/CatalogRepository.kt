package com.kyobi.domain.repository

import com.kyobi.domain.model.SaleCatalog

interface CatalogRepository {
    suspend fun getSaleCatalogs(): List<SaleCatalog>
}