package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.ReceiptItem

// Adapter for Receipt Items
class ReceiptItemAdapter(private val items: List<ReceiptItem>) :
    RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt_line, parent, false)
        return ReceiptItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class ReceiptItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        private val tvItemQuantity: TextView = view.findViewById(R.id.tvItemQuantity)
        private val tvItemPrice: TextView = view.findViewById(R.id.tvItemPrice)

        fun bind(item: ReceiptItem) {
            tvItemName.text = "${item.model} (${item.variant})"
            tvItemQuantity.text = item.quantity.toString()
            tvItemPrice.text = "₱${item.amount}"
        }
    }
}