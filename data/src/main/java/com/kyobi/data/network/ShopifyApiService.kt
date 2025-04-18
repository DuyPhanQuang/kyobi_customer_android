package com.kyobi.data.network

import com.kyobi.core.model.NetworkResult
import com.kyobi.data.model.ProductResponse

interface ShopifyApiService {
    suspend fun getProducts(): NetworkResult<List<ProductResponse>>
}