package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.CartResponse
import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.Product
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.model.SignUpRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("user/register")
    fun register(@Body userRequest: SignUpRequest): Call<ApiResponse>

    @POST("user/login")
    fun login(@Body userRequest: LogInRequest): Call<ApiResponse>

    @GET("product/products")
    fun getProducts(): Call<ProductResponse>

    @GET("product/search")
    fun searchProducts(
        @Query("name") name: String? = null,
        @Query("brand") brand: String? = null,
        @Query("category") category: String? = null,
        @Query("subcategory") subcategory: String? = null,
        @Query("tags") tags: String? = null // Pass tags as a comma-separated string
    ): Call<ProductResponse>

    @POST("cart/createCart")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body cartRequest: CartRequest
    ): Response<CartResponse>

    @GET("cart/cart")
    fun getCartItems(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int
    ): Call<CartResponse>  // Changed from Response<CartResponse> to Call<CartResponse>

}


