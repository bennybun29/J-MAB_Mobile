package com.example.j_mabmobile.model

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

data class CartItem(
    val cart_id: Int,
    val user_id: Int,
    val product_id: Int,
    val product_name: String,
    val product_image: String,
    val product_price: Double,
    val productStock: Int,
    var quantity: Int,
    val product_brand: String
) {
    val total_price: Double
        get() = quantity * product_price
}


