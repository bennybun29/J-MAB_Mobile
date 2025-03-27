package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.Order
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat

class ViewAllOrdersAdapter(private val orders: MutableList<Order>) :
    RecyclerView.Adapter<ViewAllOrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.item_text)
        val itemBrand: TextView = itemView.findViewById(R.id.item_brand)
        val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        val productPrice: TextView = itemView.findViewById(R.id.productPriceTV)
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val status: TextView = itemView.findViewById(R.id.statusTV)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val viewItem: TextView = itemView.findViewById(R.id.viewTV)
        val buyAgainBtn: Button = itemView.findViewById(R.id.buyAgainBtn)
        val sizeTV: TextView = itemView.findViewById(R.id.sizeTV)
        val receiptButton: Button = itemView.findViewById(R.id.receiptButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_orders_item_card, parent, false) // Using the correct layout
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.itemText.text = order.product_name
        holder.itemBrand.text = boldText("Brand: ", order.product_brand)
        holder.itemQuantity.text = boldText("Quantity: ", order.quantity.toString())
        val formattedPrice = NumberFormat.getNumberInstance().format(order.total_price)
        holder.productPrice.text = boldText("Price: ₱", formattedPrice)
        holder.status.text = boldText("Payment Status: ", order.payment_status)
        holder.orderStatus.text = boldText("Order Status: ", order.status)
        holder.sizeTV.text = boldText("Size: ", order.variant_size)

        /*val statusBackground = when (order.status.lowercase()) {
            "pending" -> R.drawable.bg_status_pending
            "processing" -> R.drawable.bg_status_processing
            "out for delivery" -> R.drawable.bg_status_out_for_delivery
            "delivered" -> R.drawable.bg_status_delivered
            "failed delivery" -> R.drawable.bg_status_failed_delivery
            "cancelled" -> R.drawable.bg_status_cancelled
            else -> R.drawable.bg_status_default
        }
        holder.orderStatus.setBackgroundResource(statusBackground)*/

        val statusColor = when (order.status.lowercase()) {
            "pending" -> Color.parseColor("#455A64")
            "processing" -> Color.parseColor("#4A90E2")
            "out for delivery" -> Color.parseColor("#FFA500")
            "delivered" -> Color.parseColor("#4CAF50")
            "failed delivery" -> Color.parseColor("#F44336")
            "cancelled" -> Color.parseColor("#9E9E9E")
            else -> Color.BLACK
        }

        val orderStatusText = boldText("Order Status: ", order.status)
        holder.orderStatus.text = orderStatusText
        holder.orderStatus.setTextColor(statusColor)


        Picasso.get()
            .load(order.product_image)
            .placeholder(R.drawable.jmab_logo)
            .error(R.drawable.jmab_logo)
            .into(holder.itemImage)

        holder.receiptButton.visibility = if (order.status.lowercase() == "delivered") {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.receiptButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ReceiptViewActivity::class.java).apply {
                putExtra("ORDER_ID", order.order_id)
            }
            context.startActivity(intent)
        }

        holder.viewItem.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, OrderInfoActivity::class.java).apply {
                putExtra("ORDER_ID", order.order_id)
                putExtra("PRODUCT_ID", order.product_id)
                putExtra("VARIANT_ID", order.variant_id)
                putExtra("PRODUCT_NAME", order.product_name)
                putExtra("PRODUCT_BRAND", order.product_brand)
                putExtra("QUANTITY", order.quantity)
                putExtra("TOTAL_PRICE", order.total_price)
                putExtra("PAYMENT_METHOD", order.payment_method)
                putExtra("PAYMENT_STATUS", order.payment_status)
                putExtra("ORDER_STATUS", order.status)
                putExtra("PRODUCT_IMAGE", order.product_image)
                putExtra("HOME_ADDRESS", order.home_address)
                putExtra("BARANGAY", order.barangay)
                putExtra("CITY", order.city)
                putExtra("PRODUCT_IMAGE", order.product_image)
                putExtra("REQUEST_TIME", order.created_at)
                putExtra("REFERENCE", order.reference_number)
            }
            context.startActivity(intent)
        }

        // Add the Buy Again button functionality
        holder.buyAgainBtn.setOnClickListener {
            val context = holder.itemView.context
            val userId = getUserIdFromSharedPreferences(context)

            // Get variant ID if available, otherwise use product ID
            val variantId = order.variant_id ?: order.product_id

            // Call addToCart with a fixed quantity of 1
            addToCart(context, userId, variantId, 1) {
                // This is a callback that will be called after the item is successfully added to cart
                // Open the CartActivity
                val intent = Intent(context, CartActivity::class.java)
                context.startActivity(intent)
            }
        }

        holder.itemText.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductScreenActivity::class.java).apply {
                putExtra("product_id", order.product_id)
            }
            context.startActivity(intent)
        }
    }

    // Helper methods that should be added to your adapter class
    private fun getUserIdFromSharedPreferences(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }

    private fun addToCart(
        context: Context,
        userId: Int,
        variantId: Int,
        quantity: Int,
        onSuccess: () -> Unit
    ) {
        Log.d("DEBUG", "User ID: $userId, Variant ID: $variantId, Quantity: $quantity")

        val cartRequest = CartRequest(userId, variantId, quantity)
        val apiService = RetrofitClient.getApiService(context)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Add to cart with fixed quantity of 1
                val response = apiService.addToCart(cartRequest)
                if (response.isSuccessful) {
                    val cartResponse = response.body()
                    if (cartResponse?.success == true) {
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                        // Call the success callback to open CartActivity
                        onSuccess()
                    } else {
                        val errorMessage = cartResponse?.errors?.get(0) ?: "Unknown error"
                        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun boldText(label: String, value: String): SpannableString {
        val capitalizedValue = value.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val spannable = SpannableString("$label$capitalizedValue")
        spannable.setSpan(StyleSpan(Typeface.BOLD), label.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }



    override fun getItemCount(): Int {
        return orders.size
    }

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
