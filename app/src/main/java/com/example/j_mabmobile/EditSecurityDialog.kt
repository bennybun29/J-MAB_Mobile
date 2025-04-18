package com.example.j_mabmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditSecurityDialog : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var saveButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_security_info_dialog, null)

        sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        apiService = RetrofitClient.getApiService(requireContext())

        passwordInputLayout = view.findViewById(R.id.passwordInputLayout)
        passwordEditText = view.findViewById(R.id.editPassword)
        saveButton = view.findViewById(R.id.saveChangesButton)
        val cancelButton: TextView = view.findViewById(R.id.cancelButton)

        updateSaveButtonState(saveButton, false)

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        saveButton.setOnClickListener {
            if (validatePassword()) {
                val newPassword = passwordEditText.text.toString().trim()
                updatePassword(newPassword)
            }
        }

        cancelButton.setOnClickListener {
            if (passwordEditText.text.toString().isNotEmpty()) {
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

    private fun validatePassword(): Boolean {
        val password = passwordEditText.text.toString()

        // Clear previous errors
        passwordInputLayout.error = null

        when {
            password.isEmpty() -> {
                passwordInputLayout.error = "Password cannot be empty"
                updateSaveButtonState(saveButton, false)
                return false
            }
            password.length < 8 -> {
                passwordInputLayout.error = "Password must be at least 8 characters long"
                updateSaveButtonState(saveButton, false)
                return false
            }
            !password.any { it.isUpperCase() } -> {
                passwordInputLayout.error = "Password must contain at least one uppercase letter"
                updateSaveButtonState(saveButton, false)
                return false
            }
            !password.any { it.isDigit() } -> {
                passwordInputLayout.error = "Password must contain at least one number"
                updateSaveButtonState(saveButton, false)
                return false
            }
            else -> {
                passwordInputLayout.error = null
                updateSaveButtonState(saveButton, true)
                return true
            }
        }
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

    private fun updatePassword(newPassword: String) {
        val userId = sharedPreferences.getInt("user_id", -1)
        val oldPassword = sharedPreferences.getString("user_password", "") ?: ""

        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the new password is the same as the old password
        if (newPassword == oldPassword) {
            Toast.makeText(requireContext(), "New password must be different from the current password", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateProfileRequest(
            first_name = null,
            last_name = null,
            profile_picture = null,
            email = null,
            phone_number = null,
            address = null,
            gender = null,
            birthday = null,
            password = newPassword
        )

        apiService.updateProfilePicture(userId, request)
            .enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Update the stored password in SharedPreferences
                        sharedPreferences.edit().putString("user_password", newPassword).apply()

                        Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update password!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}