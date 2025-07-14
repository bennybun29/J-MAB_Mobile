package com.example.j_mabmobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ReceiptResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.getApiService(application.applicationContext)
    private val _receiptLiveData = MutableLiveData<Result<ReceiptResponse>>()
    val receiptLiveData: LiveData<Result<ReceiptResponse>> = _receiptLiveData

    fun fetchReceipt(orderId: Int) {
        apiService.getReceiptByOrderID(orderId).enqueue(object : Callback<ReceiptResponse> {
            override fun onResponse(call: Call<ReceiptResponse>, response: Response<ReceiptResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        _receiptLiveData.value = Result.success(it)
                    } ?: run {
                        _receiptLiveData.value = Result.failure(Exception("Empty response"))
                    }
                } else {
                    _receiptLiveData.value = Result.failure(
                        Exception("Error: ${response.code()} - ${response.message()}")
                    )
                }
            }

            override fun onFailure(call: Call<ReceiptResponse>, t: Throwable) {
                _receiptLiveData.value = Result.failure(t)
            }
        })
    }
}
