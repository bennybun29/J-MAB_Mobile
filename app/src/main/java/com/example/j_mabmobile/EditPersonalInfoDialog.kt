package com.example.j_mabmobile

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class EditPersonalInfoDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_personal_info_dialog, null)

        val nameEditText = view.findViewById<EditText>(R.id.editName)
        val genderEditText = view.findViewById<EditText>(R.id.editGender)
        val birthdayEditText = view.findViewById<EditText>(R.id.editBirthday)
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
