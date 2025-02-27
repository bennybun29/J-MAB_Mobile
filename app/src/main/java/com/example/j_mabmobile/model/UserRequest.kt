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
    val gender: String
)

data class UserProfileResponse(
    val success: Boolean,
    val user: User
)

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
    val id: Int,
    val first_name: String?,
    val last_name: String?,
    val profile_picture: String?,
    val email: String?,
    val phone_number: String?,
    val address: String?,
    val gender: String?,
    val birthday: String?
)

data class UpdateProfileResponse(
    val success: Boolean,
    val errors: List<String>
)

data class CheckoutRequest(
    val cart_ids: List<Int>,
    val payment_method: String
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
    val reference_number: String,
    val created_at: String
)



