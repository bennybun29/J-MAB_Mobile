package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.AverageRatingResponse
import com.example.j_mabmobile.model.Product
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        val context = holder.itemView.context

        // Load image using Picasso
        Picasso.get()
            .load(product.image_url)
            .placeholder(R.drawable.jmab_logo)
            .error(R.drawable.jmab_logo)
            .into(holder.imageView)

        // Fetch the average rating from API
        fetchAverageRating(context, product.product_id, holder)

        holder.itemView.setOnClickListener {
            val context = it.context
            val token = getTokenFromSharedPreferences(context)
            val intent = Intent(context, ProductScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("from_recommended", true)
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
                // Add the rating to the intent
                putExtra("product_rating", holder.currentRating)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val textView: TextView = itemView.findViewById(R.id.item_text)
        val priceView: TextView = itemView.findViewById(R.id.item_price)
        val ratingView: TextView = itemView.findViewById(R.id.ratingsTV)

        // Add a variable to store the current rating
        var currentRating: Float = 0f
    }

    private fun fetchAverageRating(context: Context, productId: Int, holder: ViewHolder) {
        RetrofitClient.getApiService(context).getAverageRating(productId)
            .enqueue(object : Callback<AverageRatingResponse> {
                override fun onResponse(
                    call: Call<AverageRatingResponse>,
                    response: Response<AverageRatingResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val avgRating = response.body()?.average_rating ?: 0f
                        if (avgRating > 0) {
                            holder.ratingView.text = "⭐ $avgRating"
                        } else {
                            holder.ratingView.text = "⭐ No ratings yet"
                        }
                        // Store the rating in the ViewHolder
                        holder.currentRating = avgRating
                    } else {
                        holder.ratingView.text = "⭐ No ratings yet"
                        holder.currentRating = 0f
                    }
                }

                override fun onFailure(call: Call<AverageRatingResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to fetch rating: ${t.message}")
                    holder.ratingView.text = "⭐ No ratings yet"
                    holder.currentRating = 0f
                }
            })
    }


    private fun getTokenFromSharedPreferences(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    private fun formatPrice(price: Double): String {
        return if (price > 100) {
            NumberFormat.getNumberInstance(Locale.US).format(price)
        } else {
            "%.2f".format(price)
        }
    }
}