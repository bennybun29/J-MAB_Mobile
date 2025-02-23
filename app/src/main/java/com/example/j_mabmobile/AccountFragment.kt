package com.example.j_mabmobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.UpdateProfileRequest
import com.github.dhaval2404.imagepicker.ImagePicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import android.util.Base64
import com.example.j_mabmobile.model.UserProfileResponse


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AccountFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var account_and_sec_btn: Button
    lateinit var my_addresses_btn: Button
    lateinit var help_btn: Button
    lateinit var log_out_btn: Button
    lateinit var UsernameTV: TextView
    lateinit var toPayBtn: ImageButton
    lateinit var toShipBtn: ImageButton
    lateinit var toReceiveBtn: ImageButton
    lateinit var toRateBtn: ImageButton
    lateinit var emailAddressTV: TextView
    lateinit var userIdTV: TextView
    lateinit var changeProfilePicBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        account_and_sec_btn = view.findViewById(R.id.account_and_security_btn)
        my_addresses_btn = view.findViewById(R.id.my_addresses_btn)
        help_btn = view.findViewById(R.id.help_btn)
        log_out_btn = view.findViewById(R.id.log_out_btn)
        UsernameTV = view.findViewById(R.id.UsernameTV)
        toPayBtn = view.findViewById(R.id.toPayBtn)
        toShipBtn = view.findViewById(R.id.toShipBtn)
        toReceiveBtn = view.findViewById(R.id.toReceiveBtn)
        toRateBtn = view.findViewById(R.id.toRateBtn)
        emailAddressTV = view.findViewById(R.id.emailAddressTV)
        userIdTV = view.findViewById(R.id.userIDNumberTV)
        changeProfilePicBtn = view.findViewById(R.id.ProfilePictureButton)

        loadUserProfile()

        val firstName = getUserFirstName()
        val lastName = getUserLastName()
        val email = getEmailAddress()
        val userId = getUserId()

        if (firstName != null && lastName != null) {
            UsernameTV.text = "$firstName $lastName"
        } else {
            UsernameTV.text = "Welcome, User"
        }

        if (email != null) {
            emailAddressTV.text = "$email"
        } else {
            emailAddressTV.text = "Email Address"
        }

        if (userId != null) {
            userIdTV.text = "ID: $userId"
        } else {
            userIdTV.text = "User ID"
        }

        toPayBtn.setOnClickListener {
            val intent = Intent(activity, MyPurchasesActivity::class.java)
            intent.putExtra("ACTIVE_TAB", "TO_PAY")
            startActivity(intent)
        }

        toShipBtn.setOnClickListener {
            val intent = Intent(activity, MyPurchasesActivity::class.java)
            intent.putExtra("ACTIVE_TAB", "TO_SHIP")
            startActivity(intent)
        }

        toReceiveBtn.setOnClickListener {
            val intent = Intent(activity, MyPurchasesActivity::class.java)
            intent.putExtra("ACTIVE_TAB", "TO_RECEIVE")
            startActivity(intent)
        }

        toRateBtn.setOnClickListener {
            val intent = Intent(activity, MyPurchasesActivity::class.java)
            intent.putExtra("ACTIVE_TAB", "TO_RATE")
            startActivity(intent)
        }

        account_and_sec_btn.setOnClickListener {
            val intent = Intent(activity, AccountAndSecurityActivity::class.java)
            startActivity(intent)
        }

        my_addresses_btn.setOnClickListener {
            val intent = Intent(activity, MyAddressesActivity::class.java)
            startActivity(intent)
        }

        help_btn.setOnClickListener {
            val intent = Intent(activity, HelpActivity::class.java)
            startActivity(intent)
        }

        changeProfilePicBtn.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .galleryOnly()
                .start()
        }

        log_out_btn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    clearUserData()
                    val intent = Intent(activity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                Log.d("AccountFragment", "Image URI: $imageUri")
                uploadImage(imageUri)
            } else {
                Log.e("AccountFragment", "Failed to get image URI")
                Toast.makeText(requireContext(), "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadImage(imageUri: Uri) {
        val filePath = getRealPathFromURI(imageUri)
        if (filePath == null) {
            Log.e("AccountFragment", "Failed to get real path from URI: $imageUri")
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val base64Image = convertImageToBase64(filePath)
        if (base64Image == null) {
            Toast.makeText(requireContext(), "Failed to encode image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = getUserId()
        val phoneNumber = getPhoneNumber()
        val address = getAddress()

        val request = UpdateProfileRequest(userId, base64Image, phoneNumber, address)

        val token = getToken()
        if (token != null) {
            val call = RetrofitClient.getApiService(requireContext()).updateProfilePicture(
                "Bearer $token", request
            )

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("AccountFragment", "Error: ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("AccountFragment", "API Failure: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun convertImageToBase64(filePath: String): String? {
        return try {
            val file = File(filePath)
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(file.length().toInt())

            inputStream.read(buffer)
            inputStream.close()

            Base64.encodeToString(buffer, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("AccountFragment", "Base64 Encoding Error: ${e.message}")
            null
        }
    }





    private fun getRealPathFromURI(uri: Uri): String? {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val columnIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DATA)
                if (columnIndex != -1) {
                    cursor.moveToFirst()
                    return cursor.getString(columnIndex)
                }
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }


    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "temp_image.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file
    }


    private fun getToken(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    private fun clearUserData() {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("jwt_token")
        editor.remove("first_name")
        editor.remove("last_name")
        editor.remove("user_email")
        editor.remove("user_id")
        editor.apply()
    }

    private fun getUserFirstName(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("first_name", null)
    }

    private fun getUserLastName(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("last_name", null)
    }

    private fun getEmailAddress(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_email", null)
    }

    private fun getUserId(): Int {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", 1)
    }

    private fun getPhoneNumber(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("phone_number", "") ?: ""
    }

    private fun getAddress(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_address", "") ?: ""
    }

    private fun loadUserProfile() {
        val userId = getUserId()
        val url = "http://localhost/old_jmab/api/user/user?id=$userId"

        RetrofitClient.getApiService(requireContext()).getUserProfile(userId)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val userProfile = response.body()
                        val base64Image = userProfile?.user?.profile_picture

                        if (!base64Image.isNullOrEmpty()) {
                            val bitmap = decodeBase64ToBitmap(base64Image)
                            if (bitmap != null) {
                                changeProfilePicBtn.setImageBitmap(bitmap)
                                changeProfilePicBtn.invalidate()
                            } else {
                                Log.e("AccountFragment", "Failed to decode Base64 image")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Log.e("AccountFragment", "Failed to load profile: ${t.message}")
                }
            })
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val cleanBase64 = base64String.replace("data:image/jpeg;base64,", "")
                .replace("data:image/png;base64,", "")

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("AccountFragment", "Base64 Decoding Error: ${e.message}")
            null
        }
    }





    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
