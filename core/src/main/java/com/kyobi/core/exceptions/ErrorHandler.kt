package com.kyobi.core.exceptions

import com.squareup.moshi.Moshi
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class KyobiErrorHandler @Inject constructor(
    private val moshi: Moshi
) {
    fun handleError(exception: Exception): KyobiApiException {
        return when (exception) {
            is HttpException -> handleHttpException(exception)
            else -> KyobiApiException("Network error: ${exception.message ?: "Unknown error"}")
        }
    }

    private fun handleHttpException(exception: HttpException): KyobiApiException {
        val code = exception.code()
        val defaultMessage = when (code) {
            401 -> "Invalid or expired token. Please log in again."
            429 -> "Too many requests. Please try again later."
            500 -> "Server error. Please try again later."
            else -> "Error $code: ${exception.message()}"
        }

        // Try to parse error response from API
        val errorResponse = try {
            val errorBody = exception.response()?.errorBody()?.string()
            errorBody?.let {
                moshi.adapter(ErrorResponse::class.java).fromJson(it)
            }
        } catch (e: Exception) {
            Timber.tag("ErrorHandler").e(e, "Failed to parse error response")
            null
        }

        // Use API message if available, otherwise use default
        val message = errorResponse?.message ?: errorResponse?.error ?: defaultMessage
        return KyobiApiException(message, code)
    }
}