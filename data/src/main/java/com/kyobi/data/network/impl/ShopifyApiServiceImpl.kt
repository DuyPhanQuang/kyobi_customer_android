package com.kyobi.data.network.impl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.core.exceptions.ShopifyErrorHandler
import com.kyobi.data.graphql.GetProductsQuery
import com.kyobi.data.graphql.type.HasMetafieldsIdentifier
import com.kyobi.data.graphql.type.ProductSortKeys
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.Money
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.ProductImage
import com.kyobi.domain.model.ProductOption
import com.kyobi.domain.model.ProductPriceRange
import com.kyobi.domain.model.ProductVariant
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopifyApiServiceImpl @Inject constructor(
    private val apolloClient: ApolloClient,
    private val errorHandler: ShopifyErrorHandler
) : ShopifyApiService {
    override suspend fun getProducts(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): List<Product> {
        try {
            val includeMetafields = !identifiers.isNullOrEmpty()
            val productSortKey = sortKey?.let {
                ProductSortKeys.valueOf(it.uppercase())
            }
            val effectiveFirst = first ?: 250
            val indentifiers = if (includeMetafields) {
                identifiers!!.map {
                    HasMetafieldsIdentifier(
                        namespace = Optional.present(it.namespace),
                        key = it.key
                    )
                }
            } else { emptyList() }
            val response: ApolloResponse<GetProductsQuery.Data> = apolloClient
                .query(
                    GetProductsQuery(
                        first = Optional.present(effectiveFirst),
                        query = Optional.presentIfNotNull(query),
                        reverse = Optional.presentIfNotNull(reverse),
                        sortKey = Optional.presentIfNotNull(productSortKey),
                        identifiers = indentifiers
                    )
                )
                .execute()

            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null
                )
            }
            val products = response.data?.products?.let { products ->
                removeEdgesAndNodes(products).mapNotNull { node ->
                    reshapeProduct(node)
                }
            } ?: emptyList()
            return products
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    private fun removeEdgesAndNodes(products: GetProductsQuery.Products): List<GetProductsQuery.Node> {
        return products.edges.map { edge -> edge.node }
    }

    private fun reshapeProduct(node: GetProductsQuery.Node): Product? {
        return try {
            Product(
                id = node.id,
                handle = node.handle,
                availableForSale = node.availableForSale,
                title = node.title,
                description = node.description,
                options = node.options.map { option ->
                    ProductOption(
                        id = option.id,
                        name = option.name,
                        values = option.values
                    )
                },
                priceRange = ProductPriceRange(
                    maxVariantPrice = Money(
                        amount = node.priceRange.maxVariantPrice.amount.toString(),
                        currencyCode = node.priceRange.maxVariantPrice.currencyCode.rawValue
                    ),
                    minVariantPrice = Money(
                        amount = node.priceRange.minVariantPrice.amount.toString(),
                        currencyCode = node.priceRange.minVariantPrice.currencyCode.rawValue
                    )
                ),
                compareAtPriceRange = ProductPriceRange(
                    maxVariantPrice = Money(
                        amount = node.compareAtPriceRange.maxVariantPrice.amount.toString(),
                        currencyCode = node.compareAtPriceRange.maxVariantPrice.currencyCode.rawValue
                    ),
                    minVariantPrice = Money(
                        amount = node.compareAtPriceRange.minVariantPrice.amount.toString(),
                        currencyCode = node.compareAtPriceRange.minVariantPrice.currencyCode.rawValue
                    )
                ),
                variants = node.variants.edges.map { edge ->
                    edge.node.let { variant ->
                        ProductVariant(
                            id = variant.id,
                            sku = variant.sku,
                            title = variant.title,
                            availableForSale = variant.availableForSale,
                            price = Money(
                                amount = variant.price.amount.toString(),
                                currencyCode = variant.price.currencyCode.rawValue
                            ),
                            image = variant.image?.let { img ->
                                ProductImage(
                                    url = img.url.toString(),
                                    altText = img.altText,
                                    width = img.width,
                                    height = img.height
                                )
                            },
                            quantityAvailable = variant.quantityAvailable
                        )
                    }
                },
                featuredImage = node.featuredImage?.let { img ->
                    ProductImage(
                        url = img.url.toString(),
                        altText = img.altText,
                        width = img.width,
                        height = img.height
                    )
                },
                images = node.images.edges.map { edge ->
                    edge.node.let { img ->
                        ProductImage(
                            url = img.url.toString(),
                            altText = img.altText,
                            width = img.width,
                            height = img.height
                        )
                    }
                },
                tags = node.tags,
                updatedAt = node.updatedAt.toString()
            )
        } catch (e: Exception) {
            null
        }
    }
}