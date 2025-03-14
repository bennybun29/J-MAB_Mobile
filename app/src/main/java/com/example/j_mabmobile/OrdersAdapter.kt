package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Order
import com.squareup.picasso.Picasso

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_purchases_item_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.itemText.text = order.product_name
        holder.itemBrand.text = "Brand: ${order.product_brand}"
        holder.itemQuantity.text = "Quantity: ${order.quantity}"
        holder.productPrice.text = "Price: ${order.total_price}"
        holder.status.text = "Payment Status: ${order.payment_status}"
        holder.order_status.text = "Order Status: ${order.status}"

        Picasso.get()
            .load(order.product_image)
            .placeholder(R.drawable.jmab_logo) // Use a placeholder drawable
            .error(R.drawable.jmab_logo) // Use an error drawable if loading fails
            .into(holder.itemImage)
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
