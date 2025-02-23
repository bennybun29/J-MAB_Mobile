package com.example.j_mabmobile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.CartItem
import com.squareup.picasso.Picasso

class CartAdapter(private val cartItems: List<CartItem>,
                  private val totalPriceTV: TextView,
                  val selectAllChkBox: CheckBox,
                  private val onItemRemoved: (CartItem) -> Unit,
                  private val onQuantityUpdated: (cartId: Int, quantity: Int) -> Unit) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    val selectedItems = mutableSetOf<CartItem>()

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
        val userId = getUserId(holder.itemView.context)
        val token = getToken(holder.itemView.context)


        holder.itemName.text = cartItem.product_name
        holder.itemQuantity.text = "${cartItem.quantity}"

        val updatedPrice = cartItem.product_price * cartItem.quantity
        holder.productPrice.text = "Price: ₱${"%.2f".format(updatedPrice)}"
        holder.brand.text = "Brand: ${cartItem.product_brand}"

        Picasso.get()
            .load(cartItem.product_image)
            .placeholder(R.drawable.jmab_fab)
            .error(R.drawable.jmab_fab)
            .into(holder.itemImage)

        holder.cartCheckBox.isChecked = selectedItems.contains(cartItem)

        holder.cartCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(cartItem)
            } else {
                selectedItems.remove(cartItem)
            }
            updateTotalPrice()
            checkIfAllSelected()
        }

        checkIfAllSelected()

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductScreenActivity::class.java).apply {
                putExtra("product_id", cartItem.product_id)
                putExtra("product_image_url", cartItem.product_image)
                putExtra("product_name", cartItem.product_name)
                putExtra("product_brand", cartItem.product_brand)
                putExtra("product_price", cartItem.product_price)
                putExtra("product_description", cartItem.product_description)
                putExtra("product_stock", cartItem.product_stock)
                putExtra("quantity", cartItem.quantity)
                putExtra("jwt_token", token)
                putExtra("user_id", userId)
            }
            context.startActivity(intent)
        }

        holder.minusBtn.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                holder.itemQuantity.text = "${cartItem.quantity}"
                val updatedPrice = cartItem.product_price * cartItem.quantity
                holder.productPrice.text = "Price: ₱${"%.2f".format(updatedPrice)}"
                updateTotalPrice()
                onQuantityUpdated(cartItem.cart_id, cartItem.quantity)
            }
        }

        holder.plusBtn.setOnClickListener {
            cartItem.quantity++
            holder.itemQuantity.text = "${cartItem.quantity}"
            val updatedPrice = cartItem.product_price * cartItem.quantity
            holder.productPrice.text = "Price: ₱${"%.2f".format(updatedPrice)}"
            updateTotalPrice()
            onQuantityUpdated(cartItem.cart_id, cartItem.quantity)
        }

        holder.removeFromCartBtn.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context).apply {
                setTitle("Remove Item")
                setMessage("Are you sure you want to remove ${cartItem.product_name} from the cart?")
                setPositiveButton("Yes") { _, _ ->
                    selectedItems.remove(cartItem)
                    onItemRemoved(cartItem)
                }
                setNegativeButton("Cancel", null)
                show()
            }
        }
    }


    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun updateTotalPrice() {
        val total = selectedItems.sumOf { it.product_price * it.quantity }
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

    fun getSelectedCartIds(): String {
        return selectedItems.joinToString(",") { it.cart_id.toString() }
    }

    private fun getUserId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }

    private fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    private fun checkIfAllSelected() {
        selectAllChkBox.setOnCheckedChangeListener(null)
        selectAllChkBox.isChecked = selectedItems.size == cartItems.size
        selectAllChkBox.setOnCheckedChangeListener { _, isChecked ->
            selectAllItems(isChecked)
        }
    }

}
