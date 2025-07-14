package com.example.j_mabmobile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
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
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.j_mabmobile.viewmodels.AddressViewModel

class AddressAdapter(
    private var addressList: MutableList<UserAddresses>,
    private val onEditClick: (UserAddresses) -> Unit,
    private val onAddressSelected: (UserAddresses) -> Unit,
    private val addressViewModel: AddressViewModel // Pass ViewModel
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
        private val deleteAddressButton: ImageButton = itemView.findViewById(R.id.deleteAddressButton)

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

            editButton.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, EditAddressActivity::class.java).apply {
                    putExtra("id", address.id)
                    putExtra("is_default", address.is_default)
                    putExtra("address", address)
                }
                context.startActivity(intent)
            }

            deleteAddressButton.setOnClickListener {
                showDeleteAddressDialog(itemView.context, address.id)
            }



        }
    }

    private fun showDeleteAddressDialog(context: android.content.Context, addressId: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_delete_address_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val btnCancel = dialogView.findViewById<Button>(R.id.btnNo)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnYes)

        // Dismiss dialog when "No" is clicked
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        // Proceed to delete the address when "Yes" is clicked
        btnConfirm.setOnClickListener {
            addressViewModel.deleteAddress(addressId) // Call ViewModel to delete address
            alertDialog.dismiss()
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
