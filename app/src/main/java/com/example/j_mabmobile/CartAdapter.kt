package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.R
import com.example.j_mabmobile.model.CartItem
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class CartAdapter(private val cartItems: List<CartItem>) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemName: TextView = itemView.findViewById(R.id.item_text)
        val itemQuantity: TextView = itemView.findViewById(R.id.quantity)
        val productPrice: TextView = itemView.findViewById(R.id.productPriceTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_card, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.itemName.text = cartItem.product_name
        holder.itemQuantity.text = "Quantity: ${cartItem.quantity}"

        holder.productPrice.text = "Price: ₱${cartItem.total_price}"


        // Load product image using Picasso
        Picasso.get()
            .load(cartItem.product_image)
            .placeholder(R.drawable.jmab_fab) // Add a placeholder
            .error(R.drawable.jmab_fab) // Show error image if URL is broken
            .into(holder.itemImage)

    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}
