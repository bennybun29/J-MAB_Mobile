package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.CartResponse
import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.model.SignUpRequest
import com.example.j_mabmobile.model.UpdateCartRequest
import com.example.j_mabmobile.model.UpdateProfileRequest
import com.example.j_mabmobile.model.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("user/register")
    fun register(@Body userRequest: SignUpRequest): Call<ApiResponse>

    @POST("user/login")
    fun login(@Body userRequest: LogInRequest): Call<ApiResponse>

    @GET("product/products")
    fun getProducts(): Call<ProductResponse>

    @POST("cart/createCart")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body cartRequest: CartRequest
    ): Response<CartResponse>

    @GET("cart/cart")
    fun getCartItems(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int
    ): Call<CartResponse>

    @DELETE("cart/deleteCart")
    fun deleteCartItem(
        @Header("Authorization") token: String,
        @Query("cart_id") cartIds: String
    ): Call<ApiResponse>

    @PUT("cart/updateCart")
    fun updateCartItem(
        @Header("Authorization") token: String,
        @Query("cart_id") cartId: Int,
        @Body updateRequest: UpdateCartRequest
    ): Call<ApiResponse>

    @PUT("user/update")
    fun updateProfilePicture(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<ApiResponse>

    @GET("user/user")
    fun getUserProfile(@Query("id") userId: Int): Call<UserProfileResponse>

}


