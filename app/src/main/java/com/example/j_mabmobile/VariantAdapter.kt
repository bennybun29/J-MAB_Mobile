package com.example.j_mabmobile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class VariantAdapter(
    private val variants: List<String>,
    private val onVariantSelected: (position: Int) -> Unit
) : RecyclerView.Adapter<VariantAdapter.VariantViewHolder>() {

    private var selectedPosition = 0

    inner class VariantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val variantText: TextView = itemView.findViewById(R.id.variantText)
        val cardView: CardView = itemView as CardView

        fun bind(variant: String, isSelected: Boolean) {
            variantText.text = variant

            if (isSelected) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.j_mab_blue))
                variantText.setTextColor(Color.WHITE)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
                variantText.setTextColor(ContextCompat.getColor(itemView.context, R.color.j_mab_blue))
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousSelected = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)
                    onVariantSelected(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_variant, parent, false)
        return VariantViewHolder(view)
    }

    override fun getItemCount(): Int = variants.size

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        holder.bind(variants[position], position == selectedPosition)
    }


    fun setSelectedPosition(position: Int) {
        if (position >= 0 && position < variants.size) {
            val previousSelected = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
        }
    }
}