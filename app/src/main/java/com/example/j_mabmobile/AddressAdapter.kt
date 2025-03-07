package com.example.j_mabmobile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Address

class AddressAdapter(
    private var addressList: List<Address>,
    private val onEditClick: (Address) -> Unit // Callback for edit button
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textViewTitle)
        private val homeAddress: TextView = itemView.findViewById(R.id.homeAddress)
        private val barangay: TextView = itemView.findViewById(R.id.baranggay)
        private val city: TextView = itemView.findViewById(R.id.city)
        private val checkBox: CheckBox = itemView.findViewById(R.id.selectAddressCheckBox)
        private val editButton: ImageButton = itemView.findViewById(R.id.editAddressButton)

        fun bind(address: Address) {
            title.text = address.title
            homeAddress.text = address.street
            barangay.text = address.barangay
            city.text = address.city
            checkBox.isChecked = address.isSelected

            // Handle checkbox toggle
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                address.isSelected = isChecked
            }

            // Handle edit button click
            editButton.setOnClickListener {
                val intent = Intent(itemView.context, EditAddressActivity::class.java)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.addresses_card, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addressList[position])
    }

    override fun getItemCount(): Int = addressList.size
}
