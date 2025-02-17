package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.R
import com.example.j_mabmobile.model.CartItem
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class CartAdapter(private val cartItems: List<CartItem>, private val totalPriceTV: TextView, val selectAllChkBox: CheckBox, private val onItemRemoved: (CartItem) -> Unit) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val selectedItems = mutableSetOf<CartItem>()


    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cartCheckBox: CheckBox = itemView.findViewById(R.id.cartItemCheckBox)
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemName: TextView = itemView.findViewById(R.id.item_text)
        val itemQuantity: TextView = itemView.findViewById(R.id.quantityText)
        val productPrice: TextView = itemView.findViewById(R.id.productPriceTV)
        val brand: TextView = itemView.findViewById(R.id.item_brand)
        val minusBtn: TextView = itemView.findViewById(R.id.minusBtn)
        val plusBtn: TextView = itemView.findViewById(R.id.plusBtn)
        val removeFromCartBtn: ImageButton = itemView.findViewById(R.id.removeFromCartBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_card, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.itemName.text = cartItem.product_name
        holder.itemQuantity.text = "${cartItem.quantity}"
        holder.productPrice.text = "Price: ₱${cartItem.total_price}"
        holder.brand.text = "Brand: ${cartItem.product_brand}"

        Picasso.get()
            .load(cartItem.product_image)
            .placeholder(R.drawable.jmab_fab)
            .error(R.drawable.jmab_fab)
            .into(holder.itemImage)

        // Set listener for checkbox
        holder.cartCheckBox.setOnCheckedChangeListener(null)
        holder.cartCheckBox.isChecked = selectedItems.contains(cartItem)
        holder.cartCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.productPrice.visibility = View.VISIBLE
                selectedItems.add(cartItem)
            } else {
                selectedItems.remove(cartItem)
            }
            updateTotalPrice()

            selectAllChkBox.setOnCheckedChangeListener(null)
            selectAllChkBox.isChecked = selectedItems.size == cartItems.size
            selectAllChkBox.setOnCheckedChangeListener { _, isChecked ->
                selectAllItems(isChecked)
            }
        }

        holder.minusBtn.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                holder.itemQuantity.text = "${cartItem.quantity}"
                updateTotalPrice()
            }
        }

        holder.plusBtn.setOnClickListener {
            cartItem.quantity++
            holder.itemQuantity.text = "${cartItem.quantity}"
            updateTotalPrice()
        }

        holder.removeFromCartBtn.setOnClickListener {
            onItemRemoved(cartItem)
        }

    }


    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun updateTotalPrice() {
        val total = selectedItems.sumOf { it.total_price }
        totalPriceTV.text = "Total: ₱ ${"%.2f".format(total)}"
    }


    fun selectAllItems(selectAll: Boolean) {
        selectedItems.clear()
        if (selectAll) {
            selectedItems.addAll(cartItems)
        }
        notifyDataSetChanged()
        updateTotalPrice()
    }


}

