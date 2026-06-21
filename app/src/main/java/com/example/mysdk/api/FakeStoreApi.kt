package com.example.mysdk.api

import com.example.mysdk.api.models.Cart
import com.example.mysdk.api.models.CartRequest
import com.example.mysdk.api.models.LoginRequest
import com.example.mysdk.api.models.LoginResponse
import com.example.mysdk.api.models.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FakeStoreApi {

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    @GET("products")
    suspend fun getProducts(@Query("limit") limit: Int = 6): List<Product>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Product

    @POST("carts")
    suspend fun addToCart(@Body cart: CartRequest): Cart

    @GET("carts/user/{userId}")
    suspend fun getCartByUser(@Path("userId") userId: Int = 1): List<Cart>

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse
}
