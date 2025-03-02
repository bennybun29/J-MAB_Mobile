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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    //Users Methods HAHAHAH
    @POST("user/register")
    fun register(@Body userRequest: SignUpRequest): Call<ApiResponse>

    @POST("user/login")
    fun login(@Body userRequest: LogInRequest): Call<ApiResponse>

    @GET("user/user")
    fun getUserProfile(@Query("id") userId: Int): Call<UserProfileResponse>

    @PUT("user/update")
    fun updateProfilePicture(
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>


    //Product Methods
    @GET("product/products")
    fun getProducts(): Call<ProductResponse>


    //Cart Methods
    @POST("cart/createCart")
    suspend fun addToCart(
        @Body cartRequest: CartRequest
    ): Response<CartResponse>

    @GET("cart/cart")
    fun getCartItems(
        @Query("user_id") userId: Int
    ): Call<CartResponse>

    @DELETE("cart/deleteCart")
    fun deleteCartItem(
        @Query("cart_id") cartIds: String
    ): Call<ApiResponse>

    @PUT("cart/updateCart")
    fun updateCartItem(
        @Query("cart_id") cartId: Int,
        @Body updateRequest: UpdateCartRequest
    ): Call<ApiResponse>


    //Checkout Methods
    @POST("order/checkout")
    fun checkout(
        @Body request: CheckoutRequest
    ): Call<CheckoutResponse>
}


