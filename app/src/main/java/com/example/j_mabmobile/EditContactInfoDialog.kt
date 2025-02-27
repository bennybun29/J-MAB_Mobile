package com.example.j_mabmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.UpdateProfileRequest
import com.example.j_mabmobile.model.UpdateProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.JsonObject

class EditContactInfoDialog : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var originalEmail: String? = null
    private var originalPhoneNumber: String? = null
    private lateinit var apiService: ApiService

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_contact_info_dialog, null)

        sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        apiService = RetrofitClient.getApiService(requireContext())

        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val editPhoneNumber = view.findViewById<EditText>(R.id.editPhoneNumber)
        val saveButton = view.findViewById<Button>(R.id.saveChangesButton)
        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)

        originalEmail = sharedPreferences.getString("user_email", "")
        originalPhoneNumber = sharedPreferences.getString("phone_number", "")

        editEmail.setText(originalEmail)
        editPhoneNumber.setText(originalPhoneNumber)

        updateSaveButtonState(saveButton, false)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val emailText = editEmail.text.toString().trim()
                val phoneText = editPhoneNumber.text.toString().trim()

                val isChanged = emailText != originalEmail || phoneText != originalPhoneNumber
                val isValid = emailText.isNotEmpty() && phoneText.isNotEmpty()

                updateSaveButtonState(saveButton, isChanged && isValid)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        editEmail.addTextChangedListener(textWatcher)
        editPhoneNumber.addTextChangedListener(textWatcher)

        saveButton.setOnClickListener {
            val updatedEmail = editEmail.text.toString().trim()
            val updatedPhoneNumber = editPhoneNumber.text.toString().trim()

            updateUserInfo(updatedEmail, updatedPhoneNumber)
        }

        cancelButton.setOnClickListener {
            val updatedEmail = editEmail.text.toString().trim()
            val updatedPhoneNumber = editPhoneNumber.text.toString().trim()

            if (updatedEmail != originalEmail || updatedPhoneNumber != originalPhoneNumber) {
                showCancelConfirmationDialog()
            } else {
                dismiss()
            }
        }

        builder.setView(view)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun updateSaveButtonState(button: Button, isEnabled: Boolean) {
        button.isEnabled = isEnabled
        val color = if (isEnabled) {
            ContextCompat.getColor(requireContext(), R.color.j_mab_blue)
        } else {
            ContextCompat.getColor(requireContext(), R.color.LTGRAY)
        }
        button.setBackgroundColor(color)
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard Changes?")
            .setMessage("Are you sure you want to discard your changes?")
            .setPositiveButton("Yes") { _, _ -> dismiss() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateUserInfo(email: String, phoneNumber: String) {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateProfileRequest(
            id = userId,
            first_name = null,
            last_name = null,
            profile_picture = null,
            email = email,
            phone_number = phoneNumber,
            address = null,
            gender = null,
            birthday = null
        )

        val token = sharedPreferences.getString("jwt_token", "") ?: ""

        apiService.updateProfilePicture("Bearer $token", updateRequest)
            .enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                        // Save the updated email and phone number
                        sharedPreferences.edit()
                            .putString("user_email", email)
                            .putString("phone_number", phoneNumber)
                            .apply()

                        dismiss()

                        (activity as? AccountAndSecurityActivity)?.updateUserInfoUI()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = extractErrorMessage(errorBody)

                        if (errorMessage.isNotEmpty()) {
                            Log.e("EditContactInfo", "API error: $errorMessage")
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditContactInfo", "API call failed", t)
                }
            })
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            val jsonObject = Gson().fromJson(errorBody, JsonObject::class.java)
            jsonObject.getAsJsonArray("errors")?.joinToString("\n") { it.asString } ?: "Unknown error occurred"
        } catch (e: Exception) {
            "Error parsing response"
        }
    }
}
