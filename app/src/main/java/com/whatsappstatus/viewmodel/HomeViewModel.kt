package com.whatsappstatus.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.whatsappstatus.WhatsappPreviewFragment
import com.whatsappstatus.util.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedPref: SharedPref,
) : ViewModel() {
    val currentVideoUri = MutableLiveData<Uri>()

    var whatsappPreviewDialog: WhatsappPreviewFragment? = null
    var isDialogVisible = MutableLiveData<Boolean>()
    var message = MutableLiveData<String>()
}