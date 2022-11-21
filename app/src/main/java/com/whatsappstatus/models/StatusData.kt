package com.whatsappstatus.models

import android.net.Uri
import java.io.Serializable

data class StatusData(
    val uri: Uri?,
    val name: String?,
    val duration: Int? = 0,
    val size: Int? = 0
) : Serializable
