package com.example.j_mabmobile.model

import android.os.Parcelable
import com.example.j_mabmobile.api.ProductListDeserializer
import com.google.gson.annotations.JsonAdapter
import kotlinx.android.parcel.Parcelize


data class ApiResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val expiresIn: Long?,
    val user: User?,
)

data class AdminResponse(
    val success: Boolean,
    val admins: List<AdminInfo>
)

data class AdminInfo(
    val id: Int,
    val first_name: String,
    val last_name: String
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

data class VerificationRequest(
    val email: String,
    val code: Int
)

data class VerificationResponse(
    val success: Boolean,
    val message: String,
)

data class VerifyCodeRequest(
    val email: String,
    val reset_code: Int
)

data class ResetPasswordRequest(
    val email: String,
    val reset_code: Int,
    val new_password: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val errors: List<String>
)


data class ForgotPasswordEmailRequest(
    val email: String
)

data class ForgotPasswordEmailResponse(
    val success: Boolean,
    val message: String,
)


data class AddressRequest(
    val addresses: List<UserAddresses>
)

data class DeleteAddressRequest(
    val address_ids: List<Int>
)

data class DeleteAddressResponse(
    val success: Boolean,
    val message: String
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
    @JsonAdapter(ProductListDeserializer::class)
    val products: List<Product>
)

data class Product(
    val product_id: Int,
    val name: String,
    val variant_id: Int,
    val description: String,
    val category: String,
    val subcategory: String?,
    val image_url: String,
    val model: String,
    val brand: String,
    val created_at: String,
    val updated_at: String,
    val variants: List<Variant>
)

data class Variant(
    val variant_id: Int,
    val product_id: Int,
    val price: String,
    val stock: Int,
    val size: String,
    val created_at: String,
    val updated_at: String
)

data class CartRequest(
    val user_id: Int,
    val variant_id: Int,
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
    val variant_id: Int,
    val variant_price: String,  // Price is stored as a String in API response
    val variant_stock: Int,
    val variant_size: String,
    val product_model: String,
    val product_id: Int,
    val product_name: String,
    val product_image: String,
    val product_brand: String,
    val product_description: String,
    var quantity: Int,
) : Parcelable {
    val product_price: Double
        get() = variant_price.toDoubleOrNull() ?: 0.0 // Convert variant_price safely

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
    val variant_id: Int,
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
    val product_id: Int,
    val variant_size: String
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

data class SendNotifRequest(
    val user_id: Int,
    val title: String,
    val message: String
)

data class NotificationResponse(
    val success: Boolean,
    val notifications: List<Notification>,
    val page: Int,
    val perPage: Int
)

data class DeleteNotificationResponse(
    val success: Boolean,
    val message: String
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
    val variant_id: Int,
    val rating: Float
)

data class RatingResponse(
    val success: Boolean,
    val items: List<OrderItem> = emptyList(),
    val ratings: List<Rating>,
    val page: Int,
    val perPage: Int,
    val totalRatings: Int,
    val totalPages: Int
)

data class OrderItem(
    val order_item_id: Int,
    val order_id: Int,
    val product_id: Int,
    val variant_id: Int,
    val reference_number: String,
    val product_name: String,
    val variant_size: String,
    val brand: String,
    val model: String,
    val category: String,
    val quantity: Int,
    val price: String,
    val subtotal: String,
    val payment_method: String,
    val status: String,
    val order_created_at: String,
    val home_address: String,
    val city: String,
    val barangay: String,
    val rating_status: String,
    val rating: Int
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

data class ReceiptResponse(
    val success: Boolean,
    val receipt: Receipt
)

@Parcelize
data class Receipt(
    val receipt_id: Int,
    val order_id: Int,
    val user_id: Int,
    val order_reference: String,
    val total_amount: String,
    val payment_method: String,
    val payment_status: String,
    val bill_to: ReceiptAddress,
    val ship_to: ReceiptAddress,
    val items: List<ReceiptItem>
) : Parcelable

@Parcelize
data class ReceiptAddress(
    val name: String,
    val address: String
) : Parcelable

@Parcelize
data class ReceiptItem(
    val model: String,
    val variant: String,
    val quantity: Int,
    val description: String,
    val unit_price: String,
    val amount: String
) : Parcelable
