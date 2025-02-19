package com.example.j_mabmobile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
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

        my_addresses_btn.setOnClickListener( {
            val intent = Intent(activity, MyAddressesActivity::class.java)
            startActivity(intent)
        })

        help_btn.setOnClickListener( {
            val intent = Intent(activity, HelpActivity::class.java)
            startActivity(intent)
        })

        log_out_btn.setOnClickListener({

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

        })



        return view
    }

    private fun clearUserData() {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Remove token and user info from SharedPreferences
        editor.remove("jwt_token") // Remove the token
        editor.remove("first_name") // Optionally remove first name
        editor.remove("last_name")  // Optionally remove last name
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
        val email = sharedPreferences.getString("user_email", null)
        Log.d("Email", "Retrieved email: $email")  // Log the email value
        return email
    }

    private fun getUserId(): Int {
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", 1)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
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