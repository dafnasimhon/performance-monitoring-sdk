package com.example.perfsdk.network

import com.example.perfsdk.PerfSDK
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class PerfInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method   = request.method
        // Strip query params to avoid capturing PII (e.g. ?email=...)
        val endpoint = request.url.encodedPath

        val start = System.currentTimeMillis()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            // Network error — no HTTP status code
            PerfSDK.trackNetworkCall(method, endpoint, 0, System.currentTimeMillis() - start)
            throw e
        }

        PerfSDK.trackNetworkCall(method, endpoint, response.code, System.currentTimeMillis() - start)
        return response
    }
}
