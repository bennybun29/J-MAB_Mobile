package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.R
import com.squareup.picasso.Picasso

class BannerAdapter(private val imageUrls: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val imageResId = imageUrls[position] // This is an integer resource ID

        Picasso.get()
            .load(imageResId) // Picasso now correctly loads the drawable resource
            .placeholder(R.drawable.jmab_logo)
            .error(R.drawable.jmab_logo)
            .fit()
            .into(holder.imageView)
    }


    override fun getItemCount(): Int = imageUrls.size

    class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.bannerImage)
    }
}
