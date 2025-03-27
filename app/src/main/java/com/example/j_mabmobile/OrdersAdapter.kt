package com.example.j_mabmobile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Order
import com.squareup.picasso.Picasso
import java.text.NumberFormat

class OrdersAdapter(private val orders: MutableList<Order>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.item_text)
        val itemBrand: TextView = itemView.findViewById(R.id.item_brand)
        val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        val productPrice: TextView = itemView.findViewById(R.id.productPriceTV)
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val status: TextView = itemView.findViewById(R.id.statusTV)
        val order_status: TextView = itemView.findViewById(R.id.orderStatus)
        val sizeTV: TextView = itemView.findViewById(R.id.sizeTV)
        val viewItem: TextView = itemView.findViewById(R.id.viewTV)
        val separatorLine: View = itemView.findViewById(R.id.separatorLine)
        val rateButton: Button = itemView.findViewById(R.id.rateButton)
        val receiptButton: Button = itemView.findViewById(R.id.receiptButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_purchases_item_card, parent, false)
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
        holder.order_status.text = boldText("Order Status: ", order.status)
        holder.sizeTV.text = boldText("Size: ", order.variant_size)

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
        holder.order_status.text = orderStatusText
        holder.order_status.setTextColor(statusColor)

        Picasso.get()
            .load(order.product_image)
            .placeholder(R.drawable.jmab_logo)
            .error(R.drawable.jmab_logo)
            .into(holder.itemImage)

        val isToRate = order.payment_status == "paid" && order.status == "delivered"

        if (isToRate) {
            // Hide the "View" button and separator line for "to rate" orders
            holder.viewItem.visibility = View.GONE

            // Show the Rate button
            holder.rateButton.visibility = View.VISIBLE
            holder.receiptButton.visibility = View.VISIBLE

            holder.receiptButton.setOnClickListener{
                val context = holder.itemView.context
                val intent = Intent(context, ReceiptViewActivity::class.java).apply {
                    putExtra("ORDER_ID", order.order_id)
                }
                context.startActivity(intent)
            }

            // Set click listener for Rate button
            holder.rateButton.setOnClickListener {
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
                    putExtra("SIZE", order.variant_size)
                }
                context.startActivity(intent)
            }

        } else {
            // For other statuses, show View button and separator line, hide Rate button
            holder.viewItem.visibility = View.VISIBLE
            holder.rateButton.visibility = View.GONE

            // Set click listener for View button
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
                    putExtra("SIZE", order.variant_size)
                }
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