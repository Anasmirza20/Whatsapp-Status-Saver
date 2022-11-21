package com.whatsappstatus.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.whatsapp.reelsfiz.R
import com.whatsapp.reelsfiz.databinding.CommonUserPreferenceDialogLayoutBinding
import com.whatsappstatus.util.DialogBuilders


class CommonUserPreferenceDialog(
    private var positiveCallback: () -> Unit,
    private val isLogout: Boolean = false
) : DialogFragment() {


    private lateinit var binding: CommonUserPreferenceDialogLayoutBinding
    private var title: String? = null
    fun setMessage(title: String?) {
        this.title = title
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CommonUserPreferenceDialogLayoutBinding.inflate(layoutInflater)
        binding.apply {
            if (isLogout) {
                include.yes.text = getString(R.string.logout)
                include.no.text = getString(R.string.cancel)
            }
            dialogLabel.text = title
            include.yes.setOnClickListener {
                positiveCallback()
                dismiss()
            }
            close.setOnClickListener {
                dismiss()
            }
            include.no.setOnClickListener {
                dismiss()
            }
        }

        return DialogBuilders.getDialogBuilder(requireActivity(), binding.root)
    }

    companion object {
        lateinit var dialog: CommonUserPreferenceDialog;
        fun getInstance(
            msg: String?,
            positiveCallback: () -> Unit,
            isLogout: Boolean = false, newInstance: Boolean = false
        ): CommonUserPreferenceDialog {
            return if (this::dialog.isInitialized && !newInstance)
                dialog
            else {
                dialog = CommonUserPreferenceDialog(positiveCallback, isLogout)
                dialog
            }.apply {
                setMessage(msg)
            }
        }
    }
}