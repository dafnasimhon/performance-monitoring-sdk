package com.example.perfsdk.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface PerfApiService {

    @POST("api/v1/events/batch")
    suspend fun sendBatch(
        @Header("X-API-Key") apiKey: String,
        @Body request: BatchRequest
    ): BatchResponse
}
