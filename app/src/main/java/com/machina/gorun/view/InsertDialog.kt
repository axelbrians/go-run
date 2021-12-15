package com.machina.gorun.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.machina.gorun.databinding.DialogInsertBinding
import kotlinx.coroutines.Job

class InsertDialog(
    private val title: String,
    private val hint: String,
    private val callback: InsertDialogInterface
): DialogFragment() {

    private lateinit var listener: InsertDialogInterface

    interface InsertDialogInterface {
        fun onInsert(dialogFragment: DialogFragment, data: String)
    }

    private fun onClick(data: String) {
        listener.onInsert(this, data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private var job: Job? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(activity)
            val binding = DialogInsertBinding.inflate(layoutInflater)

            binding.dialogInsertTextField.hint = hint
            binding.dialogInsertTitle.text = title
            builder.setView(binding.root)

            binding.dialogInsertCancel.setOnClickListener {
                this.dismiss()
            }

            binding.dialogInsertSave.setOnClickListener {
                val data = binding.dialogInsertTextField.editText?.text.toString().trim()

                listener.onInsert(this, data)
            }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = callback
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement DialogOnQuestionClickListener")
        }
    }
}