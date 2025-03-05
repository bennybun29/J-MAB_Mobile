package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.CartResponse
import com.example.j_mabmobile.model.CheckoutRequest
import com.example.j_mabmobile.model.CheckoutResponse
import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.OrderResponse
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.model.SignUpRequest
import com.example.j_mabmobile.model.UpdateCartRequest
import com.example.j_mabmobile.model.UpdateProfileRequest
import com.example.j_mabmobile.model.UpdateProfileResponse
import com.example.j_mabmobile.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // Users Methods
    @POST("users/register")
    fun register(@Body userRequest: SignUpRequest): Call<ApiResponse>

    @POST("users/login")
    fun login(@Body userRequest: LogInRequest): Call<ApiResponse>

    @GET("users/{id}")
    fun getUserProfile(@Path("id") userId: Int): Call<UserProfileResponse>

    @PUT("users/{id}")
    fun updateProfilePicture(
        @Path("id") userId: Int,
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>

    // Product Methods
    @GET("products")
    fun getProducts(): Call<ProductResponse>

    // Cart Methods
    @POST("carts")
    suspend fun addToCart(
        @Body cartRequest: CartRequest
    ): Response<CartResponse>

    @GET("carts/{user_id}")
    fun getCartItems(
        @Path("user_id") userId: Int
    ): Call<CartResponse>

    @DELETE("carts/{cart_id}")
    fun deleteCartItem(
        @Path("cart_id") cartIds: String
    ): Call<ApiResponse>

    @PUT("carts/{cart_id}")
    fun updateCartItem(
        @Path("cart_id") cartId: Int,
        @Body updateRequest: UpdateCartRequest
    ): Call<ApiResponse>

    //Order Methods
    @GET("orders/{user_id}")
    fun checkout(
        @Path("user_id") userId: Int,
    ): Call<OrderResponse>
}