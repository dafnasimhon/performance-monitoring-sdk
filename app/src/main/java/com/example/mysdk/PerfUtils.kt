package com.example.mysdk

import com.example.perfsdk.PerfSDK
import retrofit2.HttpException

suspend fun <T> tracked(method: String, endpoint: String, block: suspend () -> T): T? {
    val start = System.currentTimeMillis()
    return try {
        val result = block()
        PerfSDK.trackNetworkCall(method, endpoint, 200, System.currentTimeMillis() - start)
        result
    } catch (e: HttpException) {
        PerfSDK.trackNetworkCall(method, endpoint, e.code(), System.currentTimeMillis() - start)
        null
    } catch (e: Exception) {
        PerfSDK.trackNetworkCall(method, endpoint, 0, System.currentTimeMillis() - start)
        null
    }
}
