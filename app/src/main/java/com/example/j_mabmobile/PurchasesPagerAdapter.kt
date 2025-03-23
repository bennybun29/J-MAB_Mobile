package com.example.j_mabmobile

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PurchasesPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        ToPayFragment(),
        ToShipFragment(),
        ToReceiveFragment(),
        ToRateFragment(),
        CancelledFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
