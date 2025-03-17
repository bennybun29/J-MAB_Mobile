package com.example.j_mabmobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


data class ApiResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val expiresIn: Long?,
    val user: User?,
)

data class User(
    val first_name: String?,
    val last_name: String?,
    val id: Int,
    val email: String,
    val profile_picture: String,
    val phone_number: String,
    val user_address: String,
    val birthday: String,
    val gender: String,
    val addresses: List<UserAddresses>
)

data class UserProfileResponse(
    val success: Boolean,
    val user: User?,
    val message: String?
)


data class AddressRequest(
    val addresses: List<UserAddresses>
)

@Parcelize
data class UserAddresses(
    val id: Int,
    val home_address: String,
    val barangay: String,
    val city: String,
    var is_default: Boolean
) : Parcelable

data class SignUpRequest(
    val first_name: String?,
    val last_name: String?,
    val email: String,
    val password: String
)

data class LogInRequest(
    val email: String,
    val password: String
)

data class ProductResponse(
    val success: Boolean,
    val products: List<Product>
)


data class Product(
    val product_id: Int,
    val name: String,
    val description: String,
    val category: String,
    val image_url: String,
    val price: Double,
    val brand: String,
    val stock: Int,
    val voltage: Int,
    val size: String
)

data class CartRequest(
    val user_id: Int,
    val product_id: Int,
    val quantity: Int,
)

data class UpdateCartRequest(
    val quantity: Int
)

data class CartResponse(
    val success: Boolean,
    val cart: List<CartItem>,
    val errors: List<String>
)

@Parcelize
data class CartItem(
    val cart_id: Int,
    val user_id: Int,
    val product_id: Int,
    val product_name: String,
    val product_image: String,
    val product_price: Double,
    val product_brand: String,
    val product_description: String,
    val product_stock: Int,
    var quantity: Int,


    ) : Parcelable {
    val total_price: Double
        get() = quantity * product_price
}

data class UpdateProfileRequest(
    val first_name: String?,
    val last_name: String?,
    val profile_picture: String?,
    val email: String?,
    val phone_number: String?,
    val address: String?,
    val gender: String?,
    val birthday: String?,
    val password: String?
)

data class UpdateProfileResponse(
    val success: Boolean,
    val errors: List<String>
)

data class CheckoutRequest(
    val cart_ids: List<Int>,
    val payment_method: String,
    val address_id: Int? = null
)

data class CheckoutResponse(
    val success: Boolean,
    val message: String,
    val payment_link: String?
)

data class OrderResponse(
    val success: Boolean,
    val orders: List<Order>?
)

data class Order(
    val order_id: Int,
    val user_id: Int,
    val total_price: Double,
    val payment_method: String,
    val status: String,
    val payment_status: String,
    val reference_number: String,
    val home_address: String,
    val barangay: String,
    val city: String,
    val created_at: String,
    val product_brand: String,
    val product_name: String,
    val quantity: Int,
    val product_image: String,
    val product_id: Int
)

data class CancelOrderRequest(
    val status: String
)

data class MessageRequest(
    val sender_id: Int,
    val receiver_id: Int,
    val message: String
)

data class Message(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val timestamp: String,
    val status: String,   // "delivered" or "read"
    val is_read: Int      // 0 (unread), 1 (read)
)

data class MessageResponse(
    val success: Boolean,
    val messages: List<Message>,
    val page: Int,
    val perPage: Int
)

data class NotificationResponse(
    val success: Boolean,
    val notifications: List<Notification>,
    val page: Int,
    val perPage: Int
)

data class ReadStatusResponse(
    val success: Boolean,
    val message: String
)

data class Notification(
    val id: Int,
    val user_id: Int,
    val title: String,
    val message: String,
    var is_read: Int,
    val created_at: String,
)


data class RatingByIDResponse(
    val success: Boolean,
    val rating: Rating // Single rating object
)

data class Rating(
    val rating_id: Int,
    val product_id: Int,
    val user_id: Int,
    val rating: Float,
    val created_at: String
)

data class RatingRequest(
    val product_id: Int,
    val rating: Float
)

data class RatingResponse(
    val success: Boolean,
    val ratings: List<Rating>,
    val page: Int,
    val perPage: Int,
    val totalRatings: Int,
    val totalPages: Int
)

data class AverageRatingResponse(
    val success: Boolean,
    val average_rating: Float,
    val rating_count: Int
)

data class PostRatingResponse(
    val success: Boolean,
    val message: String,
    val rating_id: Int
)
