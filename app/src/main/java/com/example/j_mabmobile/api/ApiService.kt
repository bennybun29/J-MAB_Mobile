package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.AddressRequest
import com.example.j_mabmobile.model.AdminResponse
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.AverageRatingResponse
import com.example.j_mabmobile.model.CancelOrderRequest
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.CartResponse
import com.example.j_mabmobile.model.CheckoutRequest
import com.example.j_mabmobile.model.CheckoutResponse
import com.example.j_mabmobile.model.DeleteAddressRequest
import com.example.j_mabmobile.model.DeleteAddressResponse
import com.example.j_mabmobile.model.DeleteNotificationResponse
import com.example.j_mabmobile.model.ForgotPasswordEmailRequest
import com.example.j_mabmobile.model.ForgotPasswordEmailResponse
import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.MessageRequest
import com.example.j_mabmobile.model.MessageResponse
import com.example.j_mabmobile.model.NotificationResponse
import com.example.j_mabmobile.model.Order
import com.example.j_mabmobile.model.OrderResponse
import com.example.j_mabmobile.model.PostRatingResponse
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.model.RatingByIDResponse
import com.example.j_mabmobile.model.RatingRequest
import com.example.j_mabmobile.model.RatingResponse
import com.example.j_mabmobile.model.ReadStatusResponse
import com.example.j_mabmobile.model.ResetPasswordRequest
import com.example.j_mabmobile.model.ResetPasswordResponse
import com.example.j_mabmobile.model.SignUpRequest
import com.example.j_mabmobile.model.UpdateCartRequest
import com.example.j_mabmobile.model.UpdateProfileRequest
import com.example.j_mabmobile.model.UpdateProfileResponse
import com.example.j_mabmobile.model.UserProfileResponse
import com.example.j_mabmobile.model.VerificationRequest
import com.example.j_mabmobile.model.VerificationResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @PUT("users/{id}")
    fun newAddress(
        @Path("id") userId: Int,
        @Body addressRequest: AddressRequest
    ): Call<UserProfileResponse>

    @GET("users/{id}")
    fun getAddresses(
        @Path("id") userId: Int
    ): Call<UserProfileResponse>

    @PUT("users/{userId}/addresses/{addressId}")
    fun updateAddress(
        @Path("userId") userId: Int,
        @Path("addressId") addressId: Int,
        @Body addressRequest: AddressRequest
    ): Call<UserProfileResponse>

    @HTTP(method = "DELETE", path = "users/{userId}/addresses", hasBody = true)
    fun deleteAddress(
        @Path("userId") userId: Int,
        @Body request: DeleteAddressRequest
    ): Call<DeleteAddressResponse>

    @POST("users/verify")
    suspend fun verifyEmail(
        @Body verificationRequest: VerificationRequest
    ): Response<VerificationResponse>

    @POST("users/forgotPassword")
    fun forgotPassword(
        @Body emailRequest: ForgotPasswordEmailRequest
    ): Call<ForgotPasswordEmailResponse>

    @POST("users/resetPassword")
    fun resetPassword(
        @Body resetPasswordRequest: ResetPasswordRequest
    ): Call<ResetPasswordResponse>

    // Product Methods
    @GET("products")
    fun getProducts(): Call<ProductResponse>

    // Cart Methods
    @POST("carts")
    suspend fun addToCart(
        @Body cartRequest: CartRequest
    ): Response<CartResponse>

    @GET("products/{id}")
    fun getProductById(@Path("id") productId: Int): Call<ProductResponse>

    @GET("carts/{user_id}")
    suspend fun getCartItemsSuspend(
        @Path("user_id") userId: Int
    ): Response<CartResponse>  // ✅ For coroutines

    @GET("carts/{user_id}")
    fun getCartItemsCall(
        @Path("user_id") userId: Int
    ): Call<CartResponse>  // ✅ For normal Retrofit calls


    @DELETE("carts/{cart_id}")
    fun deleteCartItem(
        @Path("cart_id") cartIds: String
    ): Call<ApiResponse>

    @DELETE("carts/{cartId}")
    suspend fun deleteCartItem(@Path("cartId") cartId: Int): Response<CartResponse>

    @PUT("carts/{cart_id}")
    fun updateCartItem(
        @Path("cart_id") cartId: Int,
        @Body updateRequest: UpdateCartRequest
    ): Call<ApiResponse>

    //Checkout Methods
    @POST("orders/{user_id}")
    fun checkout(
        @Path("user_id") userId: Int,
        @Body request: CheckoutRequest
    ): Call<CheckoutResponse>

    //Order Methods
    @GET("orders/{user_id}")
    fun getOrders(
        @Path("user_id") userId: Int
    ): Call<OrderResponse>

    @PUT("orders/status/{order_id}")
    fun cancelOrders(
        @Path("order_id") orderId: Int,
        @Body request: CancelOrderRequest
    ): Call<ApiResponse>

    // Messages Methods
    @POST("messages")
    fun sendMessage(@Body messageRequest: MessageRequest): Call<ApiResponse>

    @GET("messages/user/{user_id}")
    fun getMessages(@Path("user_id") userId: Int): Call<MessageResponse>

    @GET("messages/conversation/{admin_id}/{user_id}")
    fun getConversation(
        @Path("admin_id") adminId: Int,
        @Path("user_id") userId: Int
    ): Call<MessageResponse>

    @GET("users/admins")
    fun getAdmins(): Call<AdminResponse>

    //Notification Methods
    @GET("notifications/user/{user_id}")
    fun getNotifications(
        @Path("user_id") userId: Int
    ): Call<NotificationResponse>

    @PUT("notifications/read/{notification_id}")
    suspend fun markNotificationAsRead(
        @Path("notification_id") notificationId: Int,
        @Body body: Any = emptyMap<String, Any>()
    ): Response<ReadStatusResponse>

    @DELETE("notifications/{notification_id}")
    fun deleteNotification(
        @Path("notification_id") notificationId: Int
    ): Call<DeleteNotificationResponse>

    //Ratings
    @GET("ratings/average/{variant_id}/average")
    fun getAverageRating(@Path("variant_id") variantId: Int): Call<AverageRatingResponse>

    @GET("ratings/variant/{variant_id}")
    fun getRatingByVariantId(@Path("variant_id") variantId: Int): Call<RatingResponse>


    @POST("ratings")
    fun postRating(@Body request: RatingRequest): Call<PostRatingResponse>
}