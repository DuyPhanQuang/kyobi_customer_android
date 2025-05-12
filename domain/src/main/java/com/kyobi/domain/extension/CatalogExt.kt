package com.kyobi.domain.extension

import com.kyobi.domain.model.SaleCatalog

val SaleCatalog.toName: String
    get() = handle.replace("-", " ").uppercase()