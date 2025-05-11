package com.kyobi.data.network.impl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.core.exceptions.ShopifyErrorHandler
import com.kyobi.data.graphql.GetHomepageKeyDataQuery
import com.kyobi.data.graphql.GetMediaImagesByIdsQuery
import com.kyobi.data.graphql.GetProductsQuery
import com.kyobi.data.graphql.type.HasMetafieldsIdentifier
import com.kyobi.data.graphql.type.ProductSortKeys
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.BannerStatus
import com.kyobi.domain.model.Money
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.ProductImage
import com.kyobi.domain.model.ProductOption
import com.kyobi.domain.model.ProductPriceRange
import com.kyobi.domain.model.ProductVariant
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.ShopifyMediaImage
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TopCatalogStatus
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

    override suspend fun getBanners(handle: String, key: String): List<Banner> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(
                    GetHomepageKeyDataQuery(
                        handle = handle,
                        key = key,
                    )
                )
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null
                )
            }
            val banners = response.data?.page?.metafield?.references?.nodes?.let { nodes ->
                mapBanners(nodes)
            } ?: emptyList()
            return banners.filter { it.status == BannerStatus.ACTIVE }
                .sortedBy { it.order }
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getTopCatalogs(handle: String, key: String): List<TopCatalog> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(
                    GetHomepageKeyDataQuery(
                        handle = handle,
                        key = key,
                    )
                )
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null
                )
            }
            val topCatalogs = response.data?.page?.metafield?.references?.nodes?.let { nodes ->
                mapTopCatalogs(nodes)
            } ?: emptyList()
            return topCatalogs.filter { it.status == TopCatalogStatus.ACTIVE }
                .sortedBy { it.order }
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getMediaImagesByIds(mediaIds: List<String>): List<ShopifyMedia> {
        try {
            val response: ApolloResponse<GetMediaImagesByIdsQuery.Data> = apolloClient
                .query(GetMediaImagesByIdsQuery(ids = mediaIds))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null
                )
            }
            val nodes = response.data?.nodes
            return nodes?.mapNotNull { node ->
                node?.onMediaImage?.let { media ->
                    ShopifyMedia(
                        id = media.id,
                        image = media.image?.let {
                            ShopifyMediaImage(
                                url = it.url.toString(),
                                altText = it.altText,
                                width = it.width?.toFloat() ?: 0f,
                                height = it.height?.toFloat() ?: 0f
                            )
                        },
                        previewImage = null,
                        sources = null
                    )
                }
            } ?: emptyList()
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

    private suspend fun mapBanners(nodes: List<GetHomepageKeyDataQuery.Node>): List<Banner> {
        val mediaIds = nodes.mapNotNull { node ->
            node.onMetaobject?.fields?.find { it.key == "image" }?.value
        }.filter { it.isNotEmpty() }
        // Fetch media details
        val mediaData = if (mediaIds.isNotEmpty()) {
            try {
                val media = getMediaImagesByIds(mediaIds)
                media
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        return nodes.mapNotNull { node ->
            node.onMetaobject?.let { metaobject ->
                try {
                    val fieldMap = metaobject.fields.associate { it.key to it.value }
                    val imageValue = fieldMap["image"]
                    val image = mediaData.find { it.id == imageValue }
                    Banner(
                        link = fieldMap["link"] ?: "",
                        order = fieldMap["order"]?.toIntOrNull() ?: 0,
                        tag = fieldMap["tag"] ?: "",
                        title = fieldMap["title"] ?: "",
                        image = image,
                        mobileImage = null,
                        status = fieldMap["status"]?.let {
                            if (it == "active") BannerStatus.ACTIVE else BannerStatus.INACTIVE
                        } ?: BannerStatus.INACTIVE,
                        type = null
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }.filter { it.status == BannerStatus.ACTIVE }
            .sortedBy { it.order }
    }

    private suspend fun mapTopCatalogs(nodes: List<GetHomepageKeyDataQuery.Node>): List<TopCatalog> {
        val mediaIds = nodes.mapNotNull { node ->
            node.onMetaobject?.fields?.find { it.key == "image" }?.value
        }.filter { it.isNotEmpty() }
        // Fetch media details
        val mediaData = if (mediaIds.isNotEmpty()) {
            try {
                val media = getMediaImagesByIds(mediaIds)
                media
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        return nodes.mapNotNull { node ->
            node.onMetaobject?.let { metaobject ->
                try {
                    val fieldMap = metaobject.fields.associate { it.key to it.value }
                    val imageValue = fieldMap["image"]
                    val image = mediaData.find { it.id == imageValue }
                    TopCatalog(
                        link = fieldMap["link"] ?: "",
                        order = fieldMap["order"]?.toIntOrNull() ?: 0,
                        tag = fieldMap["tag"] ?: "",
                        title = fieldMap["title"] ?: "",
                        image = image,
                        status = fieldMap["status"]?.let {
                            if (it == "active") TopCatalogStatus.ACTIVE else TopCatalogStatus.INACTIVE
                        } ?: TopCatalogStatus.INACTIVE,
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }.filter { it.status == TopCatalogStatus.ACTIVE }
            .sortedBy { it.order }
    }
}