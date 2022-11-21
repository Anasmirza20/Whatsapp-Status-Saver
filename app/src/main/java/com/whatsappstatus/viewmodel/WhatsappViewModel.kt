package com.whatsappstatus.viewmodel

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.whatsappstatus.WhatsappStatusProvider.WHATSAPP_STATUS_FOLDER_PATH
import com.whatsappstatus.models.StatusData
import com.whatsappstatus.util.Extensions.getImageFiles
import com.whatsappstatus.util.Extensions.getVideoFiles
import com.whatsappstatus.util.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WhatsappViewModel @Inject constructor(
    private val sharedPref: SharedPref,
) : ViewModel() {

    val statusVideos = MutableLiveData<MutableList<StatusData>>()
    val statusImages = MutableLiveData<MutableList<StatusData>>()

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun manageStatus(uri: Uri?, context: Context?) {
        withContext(IO) {
            uri?.let { it1 ->
                context?.contentResolver?.takePersistableUriPermission(
                    it1,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                fetchFiles(context, uri)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun fetchFiles(context: Context?, uri: Uri) {
        val targetDirector = context?.let { it2 -> DocumentFile.fromTreeUri(it2, uri) }
        val listFiles = targetDirector?.listFiles()
        filterStatuses(listFiles)
    }

    private fun <T> filterStatuses(listFiles: Array<T?>?) {
        val list = listFiles?.toMutableList()?.filter {
            when (it) {
                is DocumentFile? -> it?.name?.endsWith(".nomedia")?.not() == true
                is File? -> it?.name?.endsWith(".nomedia")?.not() == true
                else -> false
            }
        }

/*        listFiles?.let { item ->
            Arrays.sort(item) { f1, f2 ->
                f2.lastModified().compareTo(f1.lastModified())
            }
        }*/


        statusImages.postValue(list?.getImageFiles())
        statusVideos.postValue(list?.getVideoFiles())

    }


    fun saveStatus(isImage: Boolean, status: StatusData, activity: Activity, savedCallback: (Uri?) -> Unit) {
        val directory =
            if (isImage) Environment.DIRECTORY_PICTURES + "/Reelsfiz images/" else Environment.DIRECTORY_DOWNLOADS + "/Reelsfiz videos/"

        if (isImage) {
            val bitmap = MediaStore.Images.Media.getBitmap(
                activity.contentResolver,
                Uri.parse(status.uri.toString())
            )
            val filename = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activity.contentResolver.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                    }
                    val imageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                    savedCallback(imageUri)
                }
            } else {
                val imageDir = Environment.getExternalStoragePublicDirectory(directory)
                val image = File(imageDir, filename)
                if (!image.parentFile?.exists()!!)
                    image.parentFile?.mkdir()
                fos = FileOutputStream(image)
                savedCallback(image.toUri())
            }
            fos.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } else {
            val inputStream =
                activity.contentResolver.openInputStream(Uri.parse(status.uri.toString()))
            val filename = "${System.currentTimeMillis()}.mp4"
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                    val uri = activity.contentResolver.insert(
                        MediaStore.Files.getContentUri("external"),
                        values
                    )
                    val outputStream: OutputStream = uri?.let {
                        activity.contentResolver.openOutputStream(it)
                    }!!
                    if (inputStream != null) {
                        outputStream.write(inputStream.readBytes())
                    }
                    outputStream.close()
                    inputStream?.close()
                    savedCallback(uri)
                } else {
                    val videoDir = Environment.getExternalStoragePublicDirectory(directory)
                    val image = File(videoDir, filename)
                    if (!image.parentFile?.exists()!!)
                        image.parentFile?.mkdir()
                    val fos = FileOutputStream(image)
                    fos.use {
                        it.write(inputStream?.readBytes())
                    }
                    fos.close()
                    inputStream?.close()
                    savedCallback(image.toUri())
                }
            } catch (e: IOException) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getStatusesForAndroid10() {
        val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + WHATSAPP_STATUS_FOLDER_PATH.replace("%2F", File.separator))
        val listFile = file.listFiles()
        filterStatuses(listFile)
    }
}