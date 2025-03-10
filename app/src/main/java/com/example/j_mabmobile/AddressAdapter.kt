package com.example.j_mabmobile

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.UserAddresses
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan

class AddressAdapter(
    private var addressList: MutableList<UserAddresses>,
    private val onEditClick: (UserAddresses) -> Unit,
    private val onAddressSelected: (UserAddresses) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private var selectedPosition: Int = -1
    private val handler = Handler(Looper.getMainLooper())

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressNumber: TextView = itemView.findViewById(R.id.textViewTitle)
        private val homeAddress: TextView = itemView.findViewById(R.id.homeAddress)
        private val barangay: TextView = itemView.findViewById(R.id.baranggay)
        private val city: TextView = itemView.findViewById(R.id.city)
        private val checkBox: CheckBox = itemView.findViewById(R.id.selectAddressCheckBox)
        private val editButton: ImageButton = itemView.findViewById(R.id.editAddressButton)

        fun bind(address: UserAddresses, position: Int) {
            addressNumber.text = "Address #${position + 1}"
            homeAddress.text = formatLabelValue("Home Address: ", address.home_address)
            barangay.text = formatLabelValue("Barangay: ", address.barangay)
            city.text = formatLabelValue("City/Municipality: ", address.city)

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = (position == selectedPosition)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = position

                    if (previousSelectedPosition != -1) {
                        addressList[previousSelectedPosition].is_default = false
                        handler.post { notifyItemChanged(previousSelectedPosition) }
                    }

                    addressList[position].is_default = true

                    // 🚀 IMMEDIATELY CALL THE API TO UPDATE DEFAULT ADDRESS
                    onAddressSelected(addressList[position])
                }

                handler.post { notifyItemChanged(position) }
            }

            editButton.setOnClickListener { onEditClick(address) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.addresses_card, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addressList[position], position)
    }

    override fun getItemCount(): Int = addressList.size

    fun updateData(newList: List<UserAddresses>) {
        addressList.clear()
        addressList.addAll(newList)
        selectedPosition = addressList.indexOfFirst { it.is_default }
        notifyDataSetChanged()
    }

    private fun formatLabelValue(label: String, value: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(label + value)
        spannable.setSpan(StyleSpan(Typeface.BOLD), label.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
}
