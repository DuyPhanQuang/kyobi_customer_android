package com.kyobi.featurecommon.product

import com.kyobi.domain.model.Product

data class ProductUiState(
    val id: String,
    val product: Product,
    val isFavourite: Boolean = false,
    val cartQuantity: Int = 0
) {
    companion object {
        fun fromProduct(product: Product): ProductUiState {
            return ProductUiState(
                id = product.id,
                product = product,
                isFavourite = false,
                cartQuantity = 0
            )
        }
    }
}