package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Product
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class RecyclerAdapter(private val products: List<Product>, private val userId: Int) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.textView.text = product.name
        holder.priceView.text = "₱${formatPrice(product.price)}"


        // Load image using Picasso
        Picasso.get()
            .load(product.image_url)
            .placeholder(R.drawable.jmab_logo) // Use a placeholder drawable
            .error(R.drawable.jmab_logo) // Use an error drawable if loading fails
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val context = it.context
            val token = getTokenFromSharedPreferences(context)
            val intent = Intent(context, ProductScreenActivity::class.java).apply {
                // Add flags to clear the back stack
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                // Add a flag to identify this is from recommended products
                putExtra("from_recommended", true)

                // Your existing extras
                putExtra("product_id", product.product_id)
                putExtra("product_name", product.name)
                putExtra("product_description", product.description)
                putExtra("product_category", product.category)
                putExtra("product_image_url", product.image_url)
                putExtra("product_price", product.price)
                putExtra("product_brand", product.brand)
                putExtra("product_stock", product.stock)
                putExtra("voltage", product.voltage.toString())
                putExtra("size", product.size)
                putExtra("user_id", userId)
                putExtra("jwt_token", token)
            }
            context.startActivity(intent)
        }
    }

    private fun getTokenFromSharedPreferences(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val textView: TextView = itemView.findViewById(R.id.item_text)
        val priceView: TextView = itemView.findViewById(R.id.item_price)
    }

    private fun formatPrice(price: Double): String {
        return if (price > 100) {
            NumberFormat.getNumberInstance(Locale.US).format(price)
        } else {
            "%.2f".format(price)
        }
    }
}
