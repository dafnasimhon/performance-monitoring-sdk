package com.example.perfsdk.network

import com.example.perfsdk.model.PerformanceEvent

internal data class BatchRequest(
    val sentAt: Long,
    val events: List<PerformanceEvent>
)

internal data class BatchResponse(
    val status: String,
    val accepted: Int
)
