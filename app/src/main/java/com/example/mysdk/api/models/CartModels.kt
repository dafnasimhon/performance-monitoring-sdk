package com.example.mysdk.api.models

data class CartProduct(val productId: Int, val quantity: Int)

data class CartRequest(
    val userId: Int = 1,
    val date: String = "2024-01-01",
    val products: List<CartProduct>,
)

data class Cart(
    val id: Int,
    val userId: Int,
    val date: String,
    val products: List<CartProduct>,
)
