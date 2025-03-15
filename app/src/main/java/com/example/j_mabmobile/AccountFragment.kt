package com.example.j_mabmobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.UpdateProfileRequest
import com.example.j_mabmobile.model.UpdateProfileResponse
import com.example.j_mabmobile.model.UserProfileResponse
import com.github.dhaval2404.imagepicker.ImagePicker
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AccountFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesListener: SharedPreferences.OnSharedPreferenceChangeListener

    lateinit var account_and_sec_btn: Button
    lateinit var my_addresses_btn: Button
    lateinit var help_btn: Button
    lateinit var log_out_btn: Button
    lateinit var UsernameTV: TextView
    lateinit var toPayBtn: ImageButton
    lateinit var toShipBtn: ImageButton
    lateinit var toReceiveBtn: ImageButton
    lateinit var toRateBtn: ImageButton
    lateinit var cancelledBtn: ImageButton
    lateinit var emailAddressTV: TextView
    lateinit var userIdTV: TextView
    lateinit var changeProfilePicBtn: CircleImageView
    lateinit var userPhoneNumberTV: TextView
    private lateinit var ordersViewModel: OrdersViewModel
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var toPayBadge: TextView
    private lateinit var toShipBadge: TextView
    private lateinit var toReceiveBadge: TextView
    private lateinit var toRateBadge: TextView
    private lateinit var cancelledBadge: TextView
    lateinit var userFullAddressTV: TextView
    lateinit var editProfilePictureIcon: ImageButton


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
        cancelledBtn = view.findViewById(R.id.cancelledBtn)
        emailAddressTV = view.findViewById(R.id.emailAddressTV)
        userIdTV = view.findViewById(R.id.userIDNumberTV)
        changeProfilePicBtn = view.findViewById(R.id.ProfilePictureButton)
        editProfilePictureIcon = view.findViewById(R.id.editProfilePictureBtn)
        userPhoneNumberTV = view.findViewById(R.id.userPhoneNumberTV)
        toPayBadge = view.findViewById(R.id.toPayBadge)
        toShipBadge = view.findViewById(R.id.toShipBadge)
        toReceiveBadge = view.findViewById(R.id.toReceiveBadge)
        toRateBadge = view.findViewById(R.id.toRateBadge)
        cancelledBadge = view.findViewById(R.id.cancelledBadge)
        userFullAddressTV = view.findViewById(R.id.userFullAddressTV)
        sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "first_name", "last_name", "user_email", "phone_number" -> updateUI()
            }
        }

        loadUserProfile()

        val firstName = getUserFirstName()
        val lastName = getUserLastName()
        val email = getEmailAddress()
        val userId = getUserId()
        val phoneNumber = getPhoneNumber()

        UsernameTV.text = if (firstName != null && lastName != null) "$firstName $lastName" else "Welcome, User"
        emailAddressTV.text = email ?: "Email Address"
        userIdTV.text = if (userId != -1) "ID: $userId" else "User ID"
        userPhoneNumberTV.text = if (phoneNumber.isNullOrEmpty()) "(09##) ### ####" else phoneNumber

        addressViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AddressViewModel::class.java)

        addressViewModel.fetchAddresses()

        addressViewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            if (addresses.isNotEmpty()) {
                val defaultAddress = addresses.find { it.is_default }
                if (defaultAddress != null) {
                    userFullAddressTV.text = "${defaultAddress.home_address}, ${defaultAddress.barangay}, ${defaultAddress.city}, Pangasinan"
                } else {
                    userFullAddressTV.text = "No default address set."
                }
            } else {
                userFullAddressTV.text = "No address found."
            }
        }


        ordersViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(OrdersViewModel::class.java)

        //Duplicate mo tong block ng code tas palitan lang toPayCount para madisplay count ng state ng orders
        ordersViewModel.toPayCount.observe(viewLifecycleOwner) { count ->
            Log.d("DEBUG", "AccountFragment - To-Pay Badge Updated: $count")
            if (count > 0) {
                toPayBadge.text = count.toString()
                toPayBadge.visibility = View.VISIBLE
            } else {
                toPayBadge.visibility = View.GONE
            }
        }

        ordersViewModel.toShipCount.observe(viewLifecycleOwner) { count ->
            Log.d("DEBUG", "AccountFragment - To-Ship Badge Updated: $count")
            if (count > 0) {
                toShipBadge.text = count.toString()
                toShipBadge.visibility = View.VISIBLE
            } else {
                toShipBadge.visibility = View.GONE
            }
        }

        ordersViewModel.toReceiveCount.observe(viewLifecycleOwner) { count ->
            Log.d("DEBUG", "AccountFragment - To-Receive Badge Updated: $count")
            if (count > 0) {
                toReceiveBadge.text = count.toString()
                toReceiveBadge.visibility = View.VISIBLE
            } else {
                toReceiveBadge.visibility = View.GONE
            }
        }

        ordersViewModel.toRateCount.observe(viewLifecycleOwner) { count ->
            Log.d("DEBUG", "AccountFragment - To-Receive Badge Updated: $count")
            if (count > 0) {
                toRateBadge.text = count.toString()
                toRateBadge.visibility = View.VISIBLE
            } else {
                toRateBadge.visibility = View.GONE
            }
        }

        ordersViewModel.cancelledCount.observe(viewLifecycleOwner) { count ->
            Log.d("DEBUG", "AccountFragment - To-Receive Badge Updated: $count")
            if (count > 0) {
                cancelledBadge.text = count.toString()
                cancelledBadge.visibility = View.VISIBLE
            } else {
                cancelledBadge.visibility = View.GONE
            }
        }

        setupButtonListeners()
        return view
    }

    private fun setupButtonListeners() {
        toPayBtn.setOnClickListener {
            startActivity(Intent(activity, MyPurchasesActivity::class.java).putExtra("ACTIVE_TAB", "TO_PAY"))
        }
        toShipBtn.setOnClickListener {
            startActivity(Intent(activity, MyPurchasesActivity::class.java).putExtra("ACTIVE_TAB", "TO_SHIP"))
        }
        toReceiveBtn.setOnClickListener {
            startActivity(Intent(activity, MyPurchasesActivity::class.java).putExtra("ACTIVE_TAB", "TO_RECEIVE"))
        }
        toRateBtn.setOnClickListener {
            startActivity(Intent(activity, MyPurchasesActivity::class.java).putExtra("ACTIVE_TAB", "TO_RATE"))
        }
        cancelledBtn.setOnClickListener{
            startActivity(Intent(activity, MyPurchasesActivity::class.java).putExtra("ACTIVE_TAB", "CANCELLED"))
        }
        account_and_sec_btn.setOnClickListener {
            startActivity(Intent(activity, AccountAndSecurityActivity::class.java))
        }
        my_addresses_btn.setOnClickListener {
            startActivity(Intent(activity, MyAddressesActivity::class.java))
        }
        help_btn.setOnClickListener {
            startActivity(Intent(activity, HelpActivity::class.java))
        }
        changeProfilePicBtn.setOnClickListener {
            showImagePickerDialog()
        }
        editProfilePictureIcon.setOnClickListener{
            showImagePickerDialog()
        }
        log_out_btn.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take a Picture", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ImagePicker.with(this).cameraOnly().cropSquare().compress(1024).start()
                    1 -> ImagePicker.with(this).galleryOnly().cropSquare().compress(1024).start()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                clearUserData()
                startActivity(Intent(activity, SignInActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        val userId = getUserId()
        if (userId != -1) {
            ordersViewModel.fetchOrders(userId, requireContext())
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
        updateUI()
        addressViewModel.fetchAddresses()
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun updateUI() {
        val firstName = getUserFirstName()
        val lastName = getUserLastName()
        val email = getEmailAddress()
        val phoneNumber = getPhoneNumber()

        UsernameTV.text = if (firstName != null && lastName != null) "$firstName $lastName" else "Welcome, User"
        emailAddressTV.text = email ?: "Email Address"
        userPhoneNumberTV.text = if (phoneNumber.isNullOrEmpty()) "(09##) ### ####" else phoneNumber
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
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
        try {
            val inputStream = requireActivity().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val base64Image = convertBitmapToBase64(bitmap)
            if (base64Image == null) {
                Toast.makeText(requireContext(), "Failed to encode image", Toast.LENGTH_SHORT).show()
                return
            }

            val userId = getUserId()
            if (userId == -1) {
                Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
                return
            }

            val request = UpdateProfileRequest(
                first_name = getUserFirstName(),
                last_name = getUserLastName(),
                profile_picture = base64Image,
                email = getEmailAddress(),
                phone_number = getPhoneNumber(),
                address = getAddress(),
                gender = getGender(),
                birthday = getBirthday(),
                password = null
            )

            val token = getToken()
            if (token != null) {
                val call = RetrofitClient.getApiService(requireContext()).updateProfilePicture(
                    userId, request
                )

                call.enqueue(object : Callback<UpdateProfileResponse> {
                    override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            changeProfilePicBtn.setImageBitmap(bitmap)
                            loadUserProfile()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }


    private fun getToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    private fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    private fun getUserFirstName(): String? {
        return sharedPreferences.getString("first_name", null)
    }

    private fun getUserLastName(): String? {
        return sharedPreferences.getString("last_name", null)
    }

    private fun getEmailAddress(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    private fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1) // Default to -1 if not found
    }

    private fun getPhoneNumber(): String? {
        return sharedPreferences.getString("phone_number", null)
    }

    private fun getAddress(): String? {
        return sharedPreferences.getString("user_address", null)
    }

    private fun getGender(): String? {
        return sharedPreferences.getString("gender", null)
    }

    private fun getBirthday(): String? {
        return sharedPreferences.getString("birthday", null)
    }

    private fun loadUserProfile() {
        val userId = getUserId()
        if (userId == -1) {
            Log.e("AccountFragment", "User ID not found for profile load")
            return
        }

        RetrofitClient.getApiService(requireContext()).getUserProfile(userId)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val userProfile = response.body()
                        val base64Image = userProfile?.user?.profile_picture
                        val phoneNumber = userProfile?.user?.phone_number ?: ""

                        sharedPreferences.edit()
                            .putString("phone_number", phoneNumber)
                            .apply()

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
        updateUI()
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        Log.d("AccountFragment", "Base64 String: $base64String")
        if (base64String.isEmpty() || base64String.length < 10) {
            Log.e("AccountFragment", "Base64 string is empty or too short")
            return null
        }
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

}