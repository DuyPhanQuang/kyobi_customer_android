package com.kyobi.core.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object ImageUtils {
    @OptIn(ExperimentalEncodingApi::class)
    fun String.encodeBase64(withPrefix: String = ""): String = withPrefix + Base64.encode(this.toByteArray(
        Charset.forName("UTF-8")))

    @OptIn(ExperimentalEncodingApi::class)
    fun String.decodeBase64(ifPrefixed: String = ""): String = if (ifPrefixed.isEmpty() || this.startsWith(ifPrefixed)) {
        Base64.decode(this.removePrefix(ifPrefixed)).decodeToString()
    } else {
        this
    }

    @Composable
    fun <T> NavHostController.getParcelable(key: String): T? = previousBackStackEntry
        ?.savedStateHandle
        ?.get<T>(key)

}