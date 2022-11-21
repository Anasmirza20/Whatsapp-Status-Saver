package com.whatsappstatus.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.whatsapp.reelsfiz.databinding.UserPreferenceSheetBinding

class CommonUserPreferencesSheet : BottomSheetDialogFragment() {

    private lateinit var binding: UserPreferenceSheetBinding

    private var retryCallback: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = UserPreferenceSheetBinding.inflate(layoutInflater, container, false)
        setListeners()
        return binding.root
    }

    fun retryCallback(retryCallback: (() -> Unit)?) {
        this.retryCallback = retryCallback
    }

    private fun setListeners() {
        binding.apply {
            retry.setOnClickListener {
                retryCallback?.invoke()
                dismissAllowingStateLoss()
            }
            cancel.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }


}