package com.kyobi.data.network.impl

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.data.model.AnonymousLoginResponse
import com.kyobi.data.model.AuthUserResponse
import com.kyobi.data.model.LoginResponse
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.request.LoginRequest
import retrofit2.HttpException
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KyobiApiServiceImpl @Inject constructor(
    retrofit: Retrofit
) : KyobiApiService {
    private val api = retrofit.create(KyobiApiService::class.java)

    override suspend fun login(request: LoginRequest): RestNetworkResult<LoginResponse> {
        return try {
            val response = api.login(request)
            response
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> RestNetworkResult.Error("Sai email hoặc mật khẩu", e.code())
                400 -> RestNetworkResult.Error("Dữ liệu không hợp lệ", e.code())
                else -> RestNetworkResult.Error("Lỗi server: ${e.message()}", e.code())
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Lỗi kết nối: ${e.message}")
        }
    }

    override suspend fun loginAnonymously(): RestNetworkResult<AnonymousLoginResponse> {
        return try {
            val response = api.loginAnonymously()
            response
        } catch (e: HttpException) {
            RestNetworkResult.Error("Lỗi server: ${e.message()}", e.code())
        } catch (e: Exception) {
            RestNetworkResult.Error("Lỗi kết nối: ${e.message}")
        }
    }

    override suspend fun getAuthUser(): RestNetworkResult<AuthUserResponse> {
        return try {
            val response = api.getAuthUser()
            response
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> RestNetworkResult.Error("Phiên đăng nhập hết hạn", e.code())
                404 -> RestNetworkResult.Error("Không tìm thấy người dùng", e.code())
                else -> RestNetworkResult.Error("Lỗi server: ${e.message()}", e.code())
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Lỗi kết nối: ${e.message}")
        }
    }
}

