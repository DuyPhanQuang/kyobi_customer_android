package com.kyobi.core.utils

import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object CoreUtils {
    @OptIn(ExperimentalEncodingApi::class)
    fun String.encodeBase64(withPrefix: String = ""): String = withPrefix + Base64.encode(this.toByteArray(
        Charset.forName("UTF-8")))

    @OptIn(ExperimentalEncodingApi::class)
    fun String.decodeBase64(ifPrefixed: String = ""): String = if (ifPrefixed.isEmpty() || this.startsWith(ifPrefixed)) {
        Base64.decode(this.removePrefix(ifPrefixed)).decodeToString()
    } else {
        this
    }
}