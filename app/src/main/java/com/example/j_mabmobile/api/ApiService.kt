package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.Product
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.model.SignUpRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("user/register")
    fun register(@Body userRequest: SignUpRequest): Call<ApiResponse>

    @POST("user/login")
    fun login(@Body userRequest: LogInRequest): Call<ApiResponse>

    @GET("product/products")
    fun getProducts(): Call<ProductResponse>
}
