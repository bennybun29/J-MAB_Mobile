package com.example.j_mabmobile

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.Order
import com.example.j_mabmobile.model.OrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val _toPayCount = MutableLiveData<Int>().apply { value = 0 }
    val toPayCount: LiveData<Int> get() = _toPayCount

    private val _toPayOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val toPayOrders: LiveData<List<Order>> get() = _toPayOrders

    fun fetchOrders(userId: Int, context: Context) {
        if (userId == -1) return

        val apiService = RetrofitClient.getRetrofitInstance(context).create(ApiService::class.java)

        apiService.getOrders(userId).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                Log.d("DEBUG", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val orders = response.body()?.orders ?: emptyList()
                    //state ng orders, ayusin mo na lang requirements at logic ng mga state ng orders pra malipat sila sa other fragments
                    val toPayOrders = orders.filter { it.payment_status == "pending" }
                    val toShipOrders = orders.filter { it.payment_status == "okay" }
                    val toReceiveOrders = orders.filter { it.payment_status == "okay" }
                    val toRateOrders = orders.filter { it.payment_status == "okay" }

                    Log.d("DEBUG", "To-Pay Orders Count: ${toPayOrders.size}")
                    Log.d("DEBUG", "To-Pay Orders: $toPayOrders")

                    _toPayCount.postValue(toPayOrders.size)
                    _toPayOrders.postValue(toPayOrders)
                } else {
                    _toPayCount.postValue(0)
                    _toPayOrders.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                Log.e("DEBUG", "Failed to fetch orders", t)
                _toPayCount.postValue(0)
                _toPayOrders.postValue(emptyList())
            }
        })
    }

    fun updateToPayCount(count: Int) {
        _toPayCount.postValue(count)
    }
}