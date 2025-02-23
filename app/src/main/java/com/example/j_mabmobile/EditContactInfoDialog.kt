package com.example.j_mabmobile

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class EditContactInfoDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_contact_info_dialog, null)

        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val editPhoneNumber = view.findViewById<EditText>(R.id.editPhoneNumber)
        val saveButton = view.findViewById<Button>(R.id.saveChangesButton)

        saveButton.setOnClickListener {
            // Handle saving the data
            dismiss()  // Close dialog
        }

        builder.setView(view)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}