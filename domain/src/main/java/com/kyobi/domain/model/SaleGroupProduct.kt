package com.kyobi.domain.model

data class SaleGroupProduct(
    val catalog: SaleCatalog,
    val products: List<Product>,
)