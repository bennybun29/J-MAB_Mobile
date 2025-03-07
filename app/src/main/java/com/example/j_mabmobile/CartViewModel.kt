package com.example.j_mabmobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CartResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems

    private val _cartItemCount = MutableLiveData<Int>().apply { value = 0 }
    val cartItemCount: LiveData<Int> get() = _cartItemCount

    fun fetchCartItems(userId: Int, context: android.content.Context) {
        if (userId == -1) return

        val apiService = RetrofitClient.getRetrofitInstance(context).create(ApiService::class.java)

        apiService.getCartItemsCall(userId).enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val cartResponse = response.body()!!
                    if (cartResponse.success) {
                        _cartItems.postValue(cartResponse.cart.toMutableList())
                        _cartItemCount.postValue(cartResponse.cart.size)
                    } else {
                        _cartItems.postValue(mutableListOf())
                        _cartItemCount.postValue(0)
                    }
                } else {
                    _cartItems.postValue(mutableListOf())
                    _cartItemCount.postValue(0)
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                _cartItems.postValue(mutableListOf())
                _cartItemCount.postValue(0)
            }
        })
    }

    fun setCartItems(items: List<CartItem>) {
        _cartItems.value = items.toMutableList()
        _cartItemCount.postValue(items.size)
    }

    fun removeCheckedOutItems(checkedOutItems: List<CartItem>) {
        _cartItems.value?.let { cartList ->
            cartList.removeAll { item -> checkedOutItems.any { it.cart_id == item.cart_id } }
            _cartItems.postValue(cartList)
            _cartItemCount.postValue(cartList.size) // Update cart badge count
        }
    }
}
