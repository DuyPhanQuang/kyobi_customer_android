package com.kyobi.customer.global.firebase

interface FcmUsecaseService {
    suspend fun refreshAndUploadToken(userId: String)
    suspend fun removeCurrentToken(userId: String)
}