package com.example.j_mabmobile.model

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
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val image_url: String, // For image loading
    val price: Double
)

data class Item(
    val imageResId: Int,
    val text: String
)
