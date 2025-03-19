package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.CartItem
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class CheckoutAdapter(private val checkoutItems: List<CartItem>) :
    RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.item_text)
        val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        val itemPrice: TextView = itemView.findViewById(R.id.productPriceTV)
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemBrand: TextView = itemView.findViewById(R.id.item_brand)
        val itemSize: TextView = itemView.findViewById(R.id.item_size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.checkout_item, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val checkoutItem = checkoutItems[position]
        val totalItemPrice = checkoutItem.product_price * checkoutItem.quantity

        // Format price with commas if it exceeds 100
        val formattedPrice = if (totalItemPrice > 100) {
            NumberFormat.getNumberInstance(Locale.US).format(totalItemPrice)
        } else {
            "%.2f".format(totalItemPrice)
        }

        holder.itemName.text = checkoutItem.product_name
        holder.itemQuantity.text = "Quantity: ${checkoutItem.quantity}"
        holder.itemPrice.text = "₱$formattedPrice"
        holder.itemBrand.text = "Brand: ${checkoutItem.product_brand}"
        holder.itemSize.text = "Size: ${checkoutItem.variant_size}"
        Picasso.get()
            .load(checkoutItem.product_image)
            .placeholder(R.drawable.jmab_fab)
            .error(R.drawable.jmab_fab)
            .into(holder.itemImage)
    }

    override fun getItemCount(): Int = checkoutItems.size
}
