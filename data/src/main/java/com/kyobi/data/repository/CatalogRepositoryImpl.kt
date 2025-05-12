package com.kyobi.data.repository

import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.SaleCatalog
import com.kyobi.domain.repository.CatalogRepository
import javax.inject.Inject

class CatalogRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
): CatalogRepository {
    override suspend fun getSaleCatalogs(): List<SaleCatalog> {
        val response = apiService.getSaleCatalogs()
        return response.map { it.toSaleCatalog() }
    }
}