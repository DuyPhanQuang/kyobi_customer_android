package com.kyobi.domain.repository

import com.kyobi.core.model.NetworkResult
import com.kyobi.domain.model.Product

interface ProductRepository {
    suspend fun getProductsFromShopify(): NetworkResult<List<Product>>
}