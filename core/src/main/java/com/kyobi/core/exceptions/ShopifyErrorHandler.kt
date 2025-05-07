package com.kyobi.core.exceptions

import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.exception.ApolloHttpException
import com.apollographql.apollo3.exception.ApolloNetworkException
import javax.inject.Inject

class ShopifyErrorHandler @Inject constructor() {
    fun handleError(exception: Exception): ShopifyApiException {
        return when (exception) {
            is ApolloHttpException -> handleApolloHttpException(exception)
            is ApolloNetworkException -> ShopifyApiException(
                message = "Network error: Unable to connect to the server",
                errorCode = null
            )
            is ApolloException -> ShopifyApiException(
                message = "GraphQL error: ${exception.message ?: "Unknown GraphQL error"}",
                errorCode = null
            )
            else -> ShopifyApiException(
                message = "Unexpected error: ${exception.message ?: "Unknown error"}",
                errorCode = null
            )
        }
    }

    private fun handleApolloHttpException(exception: ApolloHttpException): ShopifyApiException {
        val code = exception.statusCode
        val defaultMessage = when (code) {
            400 -> "Bad request: Invalid query or parameters"
            401 -> "Unauthorized: Invalid or expired API key"
            403 -> "Forbidden: Access denied"
            429 -> "Too many requests: Rate limit exceeded"
            500 -> "Server error: Please try again later"
            else -> "HTTP error code: ${exception.message}"
        }

        // Lấy message từ exception hoặc dùng default
        val message = exception.message ?: defaultMessage
        return ShopifyApiException(message, code)
    }
}

data class ShopifyApiException(
    override val message: String,
    val errorCode: Int?
) : Exception(message)