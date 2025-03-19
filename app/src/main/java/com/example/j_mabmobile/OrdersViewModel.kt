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

    //All Orders Chuchu
    private val _allOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val allOrders: LiveData<List<Order>> get() = _allOrders

    //To Pay Chuchu
    private val _toPayCount = MutableLiveData<Int>().apply { value = 0 }
    val toPayCount: LiveData<Int> get() = _toPayCount

    private val _toPayOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val toPayOrders: LiveData<List<Order>> get() = _toPayOrders

    //To Ship Chuchu
    private val _toShipCount = MutableLiveData<Int>().apply { value = 0 }
    val toShipCount: LiveData<Int> get() = _toShipCount

    private val _toShipOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val toShipOrders: LiveData<List<Order>> get() = _toShipOrders

    //To Deliver Chuchu
    private val _toReceiveCount = MutableLiveData<Int>().apply { value = 0 }
    val toReceiveCount: LiveData<Int> get() = _toReceiveCount

    private val _toReceiveOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val toReceiveOrders: LiveData<List<Order>> get() = _toReceiveOrders

    //To Rate Chuchu
    private val _toRateCount = MutableLiveData<Int>().apply { value = 0 }
    val toRateCount: LiveData<Int> get() = _toRateCount

    private val _toRateOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val toRateOrders: LiveData<List<Order>> get() = _toRateOrders

    //Cancelled
    private val _cancelledCount = MutableLiveData<Int>().apply { value = 0 }
    val cancelledCount: LiveData<Int> get() = _cancelledCount

    private val _cancelledOrders = MutableLiveData<List<Order>>().apply { value = emptyList() }
    val cancelledOrders: LiveData<List<Order>> get() = _cancelledOrders

    fun fetchOrders(userId: Int, context: Context) {
        if (userId == -1) return

        val apiService = RetrofitClient.getRetrofitInstance(context).create(ApiService::class.java)

        apiService.getOrders(userId).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                Log.d("DEBUG", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val orders = response.body()?.orders ?: emptyList()

                    val validOrders = orders.filterNot {
                        it.status == "failed delivery"  // Keep cancelled orders
                    }

                    _allOrders.postValue(validOrders)

                    val toPayOrders = validOrders.filter {
                        (it.payment_status == "pending" && it.payment_method == "gcash") ||
                                (it.payment_status == "pending" && it.payment_method == "cod" && it.status == "pending")
                    }

                    val toShipOrders = validOrders.filter {
                        (it.payment_status == "paid" && it.status == "processing") ||
                                (it.payment_status == "pending" && it.payment_method == "cod" && it.status == "processing")
                    }

                    val toReceiveOrders = validOrders.filter {
                        (it.payment_status == "paid" && it.status == "out for delivery") ||
                                (it.payment_status == "pending" && it.payment_method == "cod" && it.status == "out for delivery")
                    }

                    val toRateOrders = validOrders.filter {
                        it.payment_status == "paid" && it.status == "delivered"
                    }

                    val cancelledOrders = validOrders.filter {
                        ( it.payment_status == "failed" && it.status == "cancelled" ) || (it.status == "cancelled")
                    }

                    // Update LiveData
                    _toPayCount.postValue(toPayOrders.size)
                    _toPayOrders.postValue(toPayOrders)

                    _toShipCount.postValue(toShipOrders.size)
                    _toShipOrders.postValue(toShipOrders)

                    _toReceiveCount.postValue(toReceiveOrders.size)
                    _toReceiveOrders.postValue(toReceiveOrders)

                    _toRateCount.postValue(toRateOrders.size)
                    _toRateOrders.postValue(toRateOrders)

                    _cancelledCount.postValue(cancelledOrders.size)
                    _cancelledOrders.postValue(cancelledOrders)


                } else {
                    clearAllLists()
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                Log.e("DEBUG", "Failed to fetch orders", t)
                clearAllLists()
            }
        })
    }


    private fun clearAllLists() {
        _allOrders.postValue(emptyList())

        _toPayCount.postValue(0)
        _toPayOrders.postValue(emptyList())

        _toShipCount.postValue(0)
        _toShipOrders.postValue(emptyList())

        _toReceiveCount.postValue(0)
        _toReceiveOrders.postValue(emptyList())

        _toRateCount.postValue(0)
        _toRateOrders.postValue(emptyList())

        _cancelledCount.postValue(0)
        _cancelledOrders.postValue(emptyList())
    }
}
