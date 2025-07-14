package com.example.j_mabmobile.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.AddressRequest
import com.example.j_mabmobile.model.DeleteAddressRequest
import com.example.j_mabmobile.model.DeleteAddressResponse
import com.example.j_mabmobile.model.UserAddresses
import com.example.j_mabmobile.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddressViewModel(application: Application) : AndroidViewModel(application) {

    private val _addresses = MutableLiveData<List<UserAddresses>>()
    val addresses: LiveData<List<UserAddresses>> get() = _addresses

    private val sharedPreferences = application.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
    private val apiService = RetrofitClient.getApiService(application)

    fun fetchAddresses() {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId == -1) return

        apiService.getAddresses(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.user?.addresses != null) {
                    _addresses.postValue(response.body()?.user?.addresses ?: emptyList())
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                _addresses.postValue(emptyList()) // Handle failure
            }
        })
    }

    fun updateAddress(updatedAddress: UserAddresses) {
        val currentList = _addresses.value?.map {
            if (it.id == updatedAddress.id) updatedAddress else it
        } ?: emptyList()
        _addresses.postValue(currentList)
    }

    fun updateDefaultAddress(newDefaultAddress: UserAddresses) {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId == -1) {
            Log.e("AddressViewModel", "User ID not found")
            return
        }

        // Update the local list: Only one address should be marked as default
        val updatedList = _addresses.value?.map {
            it.copy(is_default = it.id == newDefaultAddress.id) // Ensure only one is default
        } ?: emptyList()

        _addresses.postValue(updatedList) // Update LiveData

        // Send the updated list to the API
        val request = AddressRequest(updatedList)

        apiService.newAddress(userId, request).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    Log.d("AddressViewModel", "Default address updated successfully")
                } else {
                    Log.e("AddressViewModel", "Failed to update default address")
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("AddressViewModel", "Error: ${t.message}")
            }
        })
    }

    fun deleteAddress(addressId: Int) {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId == -1) {
            Log.e("AddressViewModel", "User ID not found")
            return
        }

        val request = DeleteAddressRequest(address_ids = listOf(addressId))

        apiService.deleteAddress(userId, request).enqueue(object : Callback<DeleteAddressResponse> {
            override fun onResponse(call: Call<DeleteAddressResponse>, response: Response<DeleteAddressResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // Remove from local list and update LiveData
                    val updatedList = _addresses.value?.filterNot { it.id == addressId } ?: emptyList()
                    _addresses.postValue(updatedList)
                } else {
                    Log.e("AddressViewModel", "Failed to delete address: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DeleteAddressResponse>, t: Throwable) {
                Log.e("AddressViewModel", "Error: ${t.message}")
            }
        })
    }



}
