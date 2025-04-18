package com.kyobi.data.repository

import com.kyobi.core.model.NetworkResult
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.Product
import com.kyobi.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val shopifyApiService: ShopifyApiService
): ProductRepository {
    override suspend fun getProductsFromShopify(): NetworkResult<List<Product>> {
        return when (val result = shopifyApiService.getProducts()) {
            is NetworkResult.Success -> {
                val products = result.data.map { product ->
                    Product(
                        id = product.id,
                        title = product.title,
                        price = product.price,
                        imageUrl = product.imageUrl
                    )
                }
                NetworkResult.Success(products)
            }
            is NetworkResult.Error -> {
                NetworkResult.Error(result.exception)
            }
            is NetworkResult.Loading -> {
                NetworkResult.Loading
            }
        }
    }
}