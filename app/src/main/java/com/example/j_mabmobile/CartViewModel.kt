package com.example.j_mabmobile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartViewModel : ViewModel() {

    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> get() = _cartItemCount

    fun fetchCartItems(userId: Int, context: android.content.Context) {
        if (userId == -1) return

        val apiService = RetrofitClient.getRetrofitInstance(context).create(ApiService::class.java)

        apiService.getCartItems(userId).enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val cartResponse = response.body()!!
                    if (cartResponse.success) {
                        _cartItemCount.postValue(cartResponse.cart.size)
                    } else {
                        _cartItemCount.postValue(0)
                    }
                } else {
                    _cartItemCount.postValue(0)
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                _cartItemCount.postValue(0)
            }
        })
    }
}
