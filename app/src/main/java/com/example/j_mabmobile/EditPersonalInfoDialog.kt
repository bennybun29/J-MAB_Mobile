package com.example.j_mabmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.UpdateProfileRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.Spinner
import androidx.core.content.ContextCompat
import com.example.j_mabmobile.model.UpdateProfileResponse
import java.text.SimpleDateFormat
import java.util.Locale

class EditPersonalInfoDialog : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_personal_info_dialog, null)

        sharedPreferences =
            requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        apiService = RetrofitClient.getApiService(requireContext())

        val firstNameEditText = view.findViewById<EditText>(R.id.editFirstName)
        val lastNameEditText = view.findViewById<EditText>(R.id.editLastName)
        val genderSpinner = view.findViewById<Spinner>(R.id.editGender)
        val birthdayEditText = view.findViewById<EditText>(R.id.editBirthday)
        val saveButton = view.findViewById<Button>(R.id.saveChangesButton)
        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)

        firstNameEditText.setText(sharedPreferences.getString("first_name", ""))
        lastNameEditText.setText(sharedPreferences.getString("last_name", ""))
        birthdayEditText.setText(sharedPreferences.getString("birthday", ""))


        val originalFirstName = sharedPreferences.getString("first_name", "") ?: ""
        val originalLastName = sharedPreferences.getString("last_name", "") ?: ""
        val originalGender = sharedPreferences.getString("gender", "") ?: ""
        val originalBirthday = sharedPreferences.getString("birthday", "") ?: ""

        val genderOptions = arrayOf("Male", "Female", "Others")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genderOptions
        )
        genderSpinner.adapter = adapter

        val genderPosition = genderOptions.indexOf(originalGender)
        if (genderPosition != -1) {
            genderSpinner.setSelection(genderPosition)
        }

        updateSaveButtonState(saveButton, false)

        birthdayEditText.setOnClickListener {
            val initialDate = if (originalBirthday.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = format.parse(originalBirthday)
                    val cal = Calendar.getInstance()
                    cal.time = date!!
                    cal
                } catch (e: Exception) {
                    val defaultCal = Calendar.getInstance()
                    defaultCal.add(Calendar.YEAR, -18)
                    defaultCal
                }
            } else {
                val defaultCal = Calendar.getInstance()
                defaultCal.add(Calendar.YEAR, -18)
                defaultCal
            }

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    birthdayEditText.setText(dateFormat.format(selectedDate.time))
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
            )

            val maxDate = Calendar.getInstance()
            maxDate.add(Calendar.DAY_OF_YEAR, -1)
            datePicker.datePicker.maxDate = maxDate.timeInMillis

            datePicker.show()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkForChanges(firstNameEditText, lastNameEditText, genderSpinner, birthdayEditText, saveButton,
                    originalFirstName, originalLastName, originalGender, originalBirthday)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        firstNameEditText.addTextChangedListener(textWatcher)
        lastNameEditText.addTextChangedListener(textWatcher)
        birthdayEditText.addTextChangedListener(textWatcher)

        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                checkForChanges(firstNameEditText, lastNameEditText, genderSpinner, birthdayEditText, saveButton,
                    originalFirstName, originalLastName, originalGender, originalBirthday)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        saveButton.setOnClickListener {
            val updatedFirstName = firstNameEditText.text.toString().trim()
            val updatedLastName = lastNameEditText.text.toString().trim()
            val updatedGender = genderSpinner.selectedItem.toString()
            val updatedBirthday = birthdayEditText.text.toString().trim()

            updateUserInfo(updatedFirstName, updatedLastName, updatedGender, updatedBirthday)
        }

        cancelButton.setOnClickListener {
            val updatedFirstName = firstNameEditText.text.toString().trim()
            val updatedLastName = lastNameEditText.text.toString().trim()
            val updatedGender = genderSpinner.selectedItem.toString()
            val updatedBirthday = birthdayEditText.text.toString().trim()

            if (updatedFirstName != originalFirstName ||
                updatedLastName != originalLastName ||
                updatedGender != originalGender ||
                updatedBirthday != originalBirthday
            ) {
                showCancelConfirmationDialog()
            } else {
                dismiss()
            }
        }

        builder.setView(view)
        return builder.create()
    }

    private fun checkForChanges(
        firstNameEditText: EditText,
        lastNameEditText: EditText,
        genderSpinner: Spinner,
        birthdayEditText: EditText,
        saveButton: Button,
        originalFirstName: String,
        originalLastName: String,
        originalGender: String,
        originalBirthday: String
    ) {
        val updatedFirstName = firstNameEditText.text.toString().trim()
        val updatedLastName = lastNameEditText.text.toString().trim()
        val updatedGender = genderSpinner.selectedItem.toString()
        val updatedBirthday = birthdayEditText.text.toString().trim()

        val isChanged = updatedFirstName != originalFirstName ||
                updatedLastName != originalLastName ||
                updatedGender != originalGender ||
                updatedBirthday != originalBirthday

        val isValid = updatedFirstName.isNotEmpty() && updatedLastName.isNotEmpty() && updatedBirthday.isNotEmpty()

        updateSaveButtonState(saveButton, isChanged && isValid)
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
            .setMessage("You have unsaved changes. Are you sure you want to cancel?")
            .setPositiveButton("Yes") { _, _ -> dismiss() }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun updateUserInfo(
        firstName: String,
        lastName: String,
        gender: String,
        birthday: String
    ) {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateProfileRequest(
            first_name = firstName.ifEmpty { null },
            last_name = lastName.ifEmpty { null },
            profile_picture = null,
            email = null,
            phone_number = null,
            address = null,
            gender = gender.ifEmpty { null },
            birthday = birthday.ifEmpty { null },
            password = null
        )

        // Corrected call with userId and updateRequest
        apiService.updateProfilePicture(userId, updateRequest)
            .enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(
                    call: Call<UpdateProfileResponse>,
                    response: Response<UpdateProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            requireContext(),
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        sharedPreferences.edit()
                            .putString("first_name", firstName)
                            .putString("last_name", lastName)
                            .putString("gender", gender)
                            .putString("birthday", birthday)
                            .apply()

                        dismiss()

                        (activity as? AccountAndSecurityActivity)?.updateUserInfoUI()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = errorBody ?: "Failed to update profile!"
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("EditPersonalInfo", "API error: $errorMessage")
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("EditPersonalInfo", "API call failed", t)
                }
            })
    }
}