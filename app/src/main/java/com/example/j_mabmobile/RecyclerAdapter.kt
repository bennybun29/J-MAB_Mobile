package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Product
import com.squareup.picasso.Picasso

class RecyclerAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.textView.text = product.name
        holder.priceView.text = "₱${product.price}"

        // Load image using Picasso
        Picasso.get()
            .load(product.image_url)
            .placeholder(R.drawable.jmab_fab) // Use a placeholder drawable
            .error(R.drawable.jmab_fab) // Use an error drawable if loading fails
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val textView: TextView = itemView.findViewById(R.id.item_text)
        val priceView: TextView = itemView.findViewById(R.id.item_price)
    }
}
