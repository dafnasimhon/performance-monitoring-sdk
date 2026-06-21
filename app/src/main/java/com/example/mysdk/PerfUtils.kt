package com.example.mysdk

import retrofit2.HttpException

// Network calls are now tracked automatically by PerfSDK.okHttpInterceptor() in RetrofitClient.
// This wrapper only handles error swallowing so activities don't crash on network failures.
suspend fun <T> tracked(method: String, endpoint: String, block: suspend () -> T): T? {
    return try {
        block()
    } catch (e: HttpException) {
        null
    } catch (e: Exception) {
        null
    }
}
