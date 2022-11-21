package com.whatsappstatus.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

object DownloadUtils {

    fun download(context: Context, url: String?, callback: (file: Uri?) -> Unit) {
        CoroutineScope(IO).launch {
            url?.let {
                downloadImage(context, url, callback)
            }
        }
    }

    private fun downloadImage(context: Context, url: String, callback: (file: Uri?) -> Unit) {
        val now = Date()
        val s = DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        val directory = Environment.DIRECTORY_PICTURES + File.separator + "Rooter/"
        val filename = "rooter_social_$s.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                }
                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    URL(url).openStream().use { input ->
                        resolver.openOutputStream(imageUri).use { output ->
                            input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                        }
                    }
                    callback(getRealPathFromUri(context, imageUri)?.toUri())
                }

            }


        } else {
            @Suppress("DEPRECATION")
            val imageDir = Environment.getExternalStoragePublicDirectory(directory)
            val image = File(imageDir, filename)

            URL(url).openStream().use { input ->
                FileOutputStream(image).use { output ->
                    input.copyTo(output)
                }
            }
            callback(image.toUri())
        }
    }

    fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }
}