package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var account_and_sec_btn: Button
    lateinit var my_addresses_btn: Button
    lateinit var help_btn: Button

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Find the button in the view
        account_and_sec_btn = view.findViewById(R.id.account_and_security_btn)
        my_addresses_btn = view.findViewById(R.id.my_addresses_btn)
        help_btn = view.findViewById(R.id.help_btn)


        // Set up the OnClickListener for the button
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



        return view
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