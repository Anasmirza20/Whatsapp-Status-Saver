package com.whatsappstatus.viewmodel

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.ContentResolverCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.whatsappstatus.util.Constants.IMAGES_DIRECTORY
import com.whatsappstatus.util.Constants.REELS_DIRECTORY
import com.whatsappstatus.util.SharedPref
import com.whatsappstatus.models.StatusData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val sharedPref: SharedPref,
    private val contentResolver: ContentResolver
) : ViewModel() {

    /**
     * Need the READ_EXTERNAL_STORAGE permission if accessing video files that your app didn't create.
     *
     *
     *
    Show only videos that are at least 5 minutes in duration.
    val selectionArgs = arrayOf(
    TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
    )

    // Display videos in alphabetical order based on their display name.
    val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
     **/

    private val _videos = MutableLiveData<MutableList<StatusData?>>()
    val videos: LiveData<MutableList<StatusData?>> get() = _videos
    private val _images = MutableLiveData<MutableList<StatusData?>>()
    val images: LiveData<MutableList<StatusData?>> get() = _images


    // content://media/external/video/media/78441
    // Container for information about each video.
/*    private val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    } else {
        TODO("VERSION.SDK_INT < Q")
    }*/

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDownloadedVideos() {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val videoList = mutableListOf<StatusData?>()
        val query = contentResolver.let {
            ContentResolverCompat.query(
                it,
                collection,
                projection,
                null,
                null,
                null, null
            )
        }
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList += StatusData(contentUri, name, duration, size)
            }
        }
        _videos.postValue(videoList)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDownloadedImages() {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageList = mutableListOf<StatusData?>()
        val query = contentResolver.let {
            ContentResolverCompat.query(
                it,
                collection,
                projection,
                null,
                null,
                null, null
            )
        }
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given Images.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    collection,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                imageList += StatusData(contentUri, name, null, size)
            }
        }
        _images.postValue(imageList)
    }

    fun getDataIfPermissionGranted() {
        getImagesForAndroid10orLesser()
        getVideosForAndroid10orLesser()
    }

    private fun getVideosForAndroid10orLesser() {
        val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + REELS_DIRECTORY)
        val listFile = file.listFiles()
        if (listFile != null) {
            _videos.postValue(MutableList(listFile.size) { position ->
                listFile[position]?.let {
                    StatusData(it.toUri(), it.name, size = file.length().toInt())
                }
            })
        }
    }

    private fun getImagesForAndroid10orLesser() {
        val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + IMAGES_DIRECTORY)
        val listFile = file.listFiles()
        if (listFile != null) {
            _images.postValue(MutableList(listFile.size) { position ->
                listFile[position]?.let {
                    StatusData(it.toUri(), it.name, size = file.length().toInt())
                }
            })
        }
    }
}