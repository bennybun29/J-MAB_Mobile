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
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

class RecyclerAdapter(private val products: List<Product>, private val userId: Int) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val availableProducts = products.filter { it.variants.any { variant -> variant.stock > 0 } }
        val product = availableProducts[position]

        // Get the first available variant or default to first variant
        val variant = product.variants.firstOrNull { it.stock > 0 } ?: product.variants.firstOrNull()

        holder.textView.text = product.name

        // Use price from variant
        val price = variant?.price?.toDoubleOrNull() ?: 0.0
        holder.priceView.text = "₱${formatPrice(price)}"

        val context = holder.itemView.context

        // Load image using Picasso
        Picasso.get()
            .load(product.image_url)
            .placeholder(R.drawable.jmab_logo)
            .error(R.drawable.jmab_logo)
            .into(holder.imageView)

        fetchAverageRating(context, product, holder)

        holder.itemView.setOnClickListener {
            val context = it.context
            val token = getTokenFromSharedPreferences(context)

            // Extract variant names (size or another identifier)
            val variantNames = product.variants.map { it.size ?: "Default" }

            val intent = Intent(context, ProductScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("from_recommended", true)
                putExtra("variant_id", variant?.variant_id)
                putExtra("product_id", product.product_id)
                putExtra("product_name", product.name)
                putExtra("product_description", product.description)
                putExtra("product_category", product.category)
                putExtra("product_image_url", product.image_url)
                putExtra("product_price", price) // Pass variant price
                putExtra("product_brand", product.brand)
                putExtra("product_stock", variant?.stock ?: 0)
                putExtra("size", variant?.size ?: "") // ✅ Keep only one "size" key
                putExtra("user_id", userId)
                putExtra("jwt_token", token)
                putExtra("product_rating", holder.currentRating)
                putExtra("has_variants", product.variants.size > 1)
                putStringArrayListExtra("product_variants", ArrayList(variantNames))

                // Pass variant mapping
                val variantMap: Map<String, Triple<Int, Double, Int>> = product.variants.associate {
                    (it.size ?: "Default") to Triple(it.variant_id ?: 0, it.price.toDoubleOrNull() ?: 0.0, it.stock)
                }

                putExtra("variant_map", HashMap(variantMap) as Serializable)

            }

            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = products.count { it.variants.any { variant -> variant.stock > 0 } }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val textView: TextView = itemView.findViewById(R.id.item_text)
        val priceView: TextView = itemView.findViewById(R.id.item_price)
        val ratingView: TextView = itemView.findViewById(R.id.ratingsTV)

        // Add a variable to store the current rating
        var currentRating: Float = 0f
    }

    private fun fetchAverageRating(context: Context, product: Product, holder: ViewHolder) {
        // Create a list to track all rating calls
        val ratingCalls = mutableListOf<Call<AverageRatingResponse>>()
        val apiService = RetrofitClient.getApiService(context)

        // Map to store variant ID and its rating
        val variantRatings = mutableMapOf<Int, Float>()
        val totalVariants = product.variants.size
        val countDownLatch = AtomicInteger(totalVariants)

        // Fetch ratings for all variants
        for (variant in product.variants) {
            val variantId = variant.variant_id

            apiService.getAverageRating(variantId)
                .enqueue(object : Callback<AverageRatingResponse> {
                    override fun onResponse(
                        call: Call<AverageRatingResponse>,
                        response: Response<AverageRatingResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val avgRating = response.body()?.average_rating ?: 0f
                            if (avgRating > 0) {
                                // Store this variant's rating
                                synchronized(variantRatings) {
                                    variantRatings[variantId] = avgRating
                                }
                            }
                        }

                        // Decrement the counter after each response
                        if (countDownLatch.decrementAndGet() == 0) {
                            // All calls completed, now find the highest rated variant
                            updateRatingDisplay(holder, variantRatings)
                        }
                    }

                    override fun onFailure(call: Call<AverageRatingResponse>, t: Throwable) {
                        Log.e("API_ERROR", "Failed to fetch rating for variant $variantId: ${t.message}")

                        // Decrement the counter even on failure
                        if (countDownLatch.decrementAndGet() == 0) {
                            // All calls completed, now find the highest rated variant
                            updateRatingDisplay(holder, variantRatings)
                        }
                    }
                })
        }
    }

    private fun updateRatingDisplay(holder: ViewHolder, variantRatings: Map<Int, Float>) {
        if (variantRatings.isEmpty()) {
            // No ratings found for any variant
            holder.ratingView.text = "⭐ No ratings yet"
            holder.currentRating = 0f
            return
        }

        // Find the highest rated variant
        val highestRatedEntry = variantRatings.maxByOrNull { it.value }

        if (highestRatedEntry != null) {
            val highestRating = highestRatedEntry.value
            holder.ratingView.text = "⭐ ${String.format("%.1f", highestRating)}"
            holder.currentRating = highestRating
        } else {
            holder.ratingView.text = "⭐ No ratings yet"
            holder.currentRating = 0f
        }
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