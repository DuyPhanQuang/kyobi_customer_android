package com.kyobi.featurecommon.routes

import android.net.Uri
import com.kyobi.core.utils.CoreUtils.decodeBase64

fun Uri.getDecodedByKey(key: String?): String?  {
    return getQueryParameter(key)?.decodeBase64(Routes.BASE_64_URL_PREFIX)
}