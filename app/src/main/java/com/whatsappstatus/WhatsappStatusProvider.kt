package com.whatsappstatus

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

object WhatsappStatusProvider {
    val WHATSAPP_STATUS_FOLDER_PATH = if (Build.VERSION.SDK_INT <= 30) "WhatsApp%2FMedia%2F.Statuses" else "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"

    @RequiresApi(Build.VERSION_CODES.R)
    fun askPermission(context: Context): Intent {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString()
        Log.d("TAG", "INITIAL_URI scheme: $scheme")
        scheme = scheme.replace("/root/", "/document/")
        scheme += "%3A$WHATSAPP_STATUS_FOLDER_PATH"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        Log.d("TAG", "uri: $uri")
        return intent
    }
}