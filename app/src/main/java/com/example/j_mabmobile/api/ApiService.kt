package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.SignUpRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("register")
    fun register(@Body userRequest: SignUpRequest): Call<ApiResponse>

    @POST("login")
    fun login(@Body userRequest: LogInRequest): Call<ApiResponse>
}
