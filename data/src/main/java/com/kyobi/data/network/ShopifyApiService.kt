package com.kyobi.data.network

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.kyobi.core.model.NetworkResult
import com.kyobi.data.graphql.GetProductsQuery
import com.kyobi.data.model.ProductResponse
import javax.inject.Inject
import javax.inject.Singleton

interface ShopifyApiService {
    suspend fun getProducts(): NetworkResult<List<ProductResponse>>
}

@Singleton
class ShopifyApiServiceImpl @Inject constructor(
    private val apolloClient: ApolloClient
) : ShopifyApiService {
    override suspend fun getProducts(): NetworkResult<List<ProductResponse>> {
       try {
           val response: ApolloResponse<GetProductsQuery.Data> = apolloClient.query(GetProductsQuery()).execute()
           if (response.hasErrors()) {
               throw Exception("GraphQL errors: ${response.errors?.joinToString { it.message }}")
           }
           val products = response.data?.products?.edges?.map { edge ->
               edge.node.let { node ->
                   ProductResponse(
                       id = node.id,
                       title = node.title,
                       price = 900000.0,
                       imageUrl = node.images.edges.firstOrNull()?.node?.url.toString()
                   )
               }
           } ?: emptyList()
           return NetworkResult.Success(products)
       } catch (e: ApolloException) {
           return NetworkResult.Error(Exception("Failed to execute GraphQL request: ${e.message}", e))
       } catch (e: Exception) {
           return NetworkResult.Error(Exception("Unexpected error: ${e.message}", e))
       }
    }
}