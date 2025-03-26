package com.example.j_mabmobile.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.27/JMAB/final-jmab/api/"
    //10.0.2.2:80

    private fun getToken(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt_token", null)?.trim()

        Log.d("TOKEN_CHECK", "Retrieved Token: '$token'")
        return token
    }

    private fun authInterceptor(context: Context) = Interceptor { chain ->
        val token = getToken(context)
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")

        } else {
            Log.e("RETROFIT_TOKEN", "No token found!")
        }

        val request = requestBuilder.build()
        Log.d("RETROFIT_DEBUG_URL", "Request URL: ${request.url}")

        chain.proceed(request)
    }

    fun getRetrofitInstance(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    fun getApiService(context: Context): ApiService {
        return getRetrofitInstance(context).create(ApiService::class.java)
    }
}


