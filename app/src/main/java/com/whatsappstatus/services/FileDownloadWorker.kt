package com.whatsappstatus.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.util.Constants.CHANNEL_DESCRIPTION
import com.whatsappstatus.util.Constants.CHANNEL_ID
import com.whatsappstatus.util.Constants.CHANNEL_NAME
import com.whatsappstatus.util.Constants.KEY_FILE_NAME
import com.whatsappstatus.util.Constants.KEY_FILE_TYPE
import com.whatsappstatus.util.Constants.KEY_FILE_URI
import com.whatsappstatus.util.Constants.KEY_FILE_URL
import com.whatsappstatus.util.Constants.NOTIFICATION_ID
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FileDownloadWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {


        val fileUrl = inputData.getString(KEY_FILE_URL) ?: ""
        val fileName = inputData.getString(KEY_FILE_NAME) ?: ""
        val fileType = inputData.getString(KEY_FILE_TYPE) ?: ""

        Log.d("TAG", "doWork: $fileUrl | $fileName | $fileType")





        if (fileName.isEmpty()
            || fileType.isEmpty()
            || fileUrl.isEmpty()
        ) {
            return Result.failure()
        }

        val directory = Environment.DIRECTORY_DOWNLOADS + "/Reelsfiz videos/"

/*
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setDescription("Selected Video is being downloaded")
        request.allowScanningByMediaScanner()
        request.setTitle("Downloading Status")
        request.setMimeType("video/mp4")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//Set the local destination for the downloaded file to a path within the application's external files directory
//Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS , fileName) //To Store file in External Public Directory use "setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)"

        val manager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        return Result.success(workDataOf(KEY_FILE_URI to fileUrl.toString()))*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = CHANNEL_NAME
            val description = CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)

        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading your file...")
            .setOngoing(true)
            .setProgress(0, 0, true)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())

        val uri = getSavedFileUri(
            fileName = fileName,
            fileType = fileType,
            fileUrl = fileUrl,
            context = context
        )

        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        return if (uri != null) {
            Result.success(workDataOf(KEY_FILE_URI to uri.toString()))
        } else {
            Result.failure()
        }
    }
}

private fun getSavedFileUri(
    fileName: String,
    fileType: String,
    fileUrl: String,
    context: Context
): Uri? {
    val mimeType = when (fileType) {
        "PDF" -> "application/pdf"
        "PNG" -> "image/png"
        "MP4" -> "video/mp4"
        else -> ""
    } // different types of files will have different mime type

    if (mimeType.isEmpty()) return null

    val directory = Environment.DIRECTORY_DOWNLOADS + "/Reelsfiz videos/"


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, directory)
        }

        val resolver = context.contentResolver


        val uri = resolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )

        return if (uri != null) {
            URL(fileUrl).openStream().use { input ->
                resolver.openOutputStream(uri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
            Log.i("TAG", "getSavedFileUri: $uri")
            uri
        } else {
            null
        }
    } else {
        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        URL(fileUrl).openStream().use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }

        return target.toUri()
    }
}

