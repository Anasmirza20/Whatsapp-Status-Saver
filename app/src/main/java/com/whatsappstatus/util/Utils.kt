package com.whatsappstatus.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.util.Constants.IS_CONNECTED
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object Utils {

    fun showMessage(view: View?, msg: String?) {
        try {
            val snackBar = Snackbar.make(view!!, msg!!, Snackbar.LENGTH_LONG)
            snackBar.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // Network Check
    @Suppress("DEPRECATION")
    fun registerNetworkConnections(context: Context, view: View?) {
        kotlin.runCatching {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        IS_CONNECTED = true
                    }

                    override fun onLost(network: Network) {
                        IS_CONNECTED = false // Global Static Variable
                    }
                }
                )
            } else {
                val activeNetwork = connectivityManager.activeNetworkInfo
                IS_CONNECTED = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting
            }
        }.onFailure {
            IS_CONNECTED = false
        }
    }

    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags: Int = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        var isDarkMode = false
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> isDarkMode = true
            Configuration.UI_MODE_NIGHT_NO -> isDarkMode = false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> isDarkMode = false
        }
        return isDarkMode
    }

    fun openBrowser(context: Context, url: String) {
        val result = kotlin.runCatching {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
        result.onFailure {
            Log.i("TAG", "openBrowser: ${it.message}")
        }
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    fun initRecyclerview(
        recyclerView: RecyclerView,
        context: Context?,
        nestedScroll: Boolean = false,
        isHorizontal: Boolean = false,
        _layoutManager: RecyclerView.LayoutManager? = null,
        listeners: RecyclerView.OnScrollListener? = null
    ) {
        recyclerView.apply {
            listeners?.let { addOnScrollListener(it) }
            setHasFixedSize(true)
            layoutManager = _layoutManager
                ?: if (isHorizontal) LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                ) else LinearLayoutManager(context)
            isNestedScrollingEnabled = nestedScroll
        }
    }

    fun initGridRecyclerview(
        recyclerView: RecyclerView,
        context: Context?,
        nestedScroll: Boolean,
        spanCount: Int
    ) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = nestedScroll
        }
    }

    fun loadImageFromLocal(uri: Uri?, imageView: ImageView) {
        val imgFile = File(uri?.path)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imageView.setImageBitmap(myBitmap)
        }
    }


    fun showFullScreenDialog(
        parentFragmentManager: FragmentManager,
        fragment: Fragment
    ) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction
            .add(R.id.main_layout, fragment)
            .addToBackStack(null)
            .commit()
    }


    fun parseMediaSource(uri: Uri, dataSourceFactory: DataSource.Factory?): MediaSource? {
        return when {
            uri.lastPathSegment?.endsWith("m3u8") == true -> {
                val mediaItem = MediaItem.fromUri(uri)
                HlsMediaSource.Factory(dataSourceFactory!!)
                    .createMediaSource(mediaItem)
            }
            uri.lastPathSegment?.endsWith("mp4") == true -> {
                val mediaItem = MediaItem.fromUri(uri)
                ProgressiveMediaSource.Factory(dataSourceFactory!!)
                    .createMediaSource(mediaItem)
            }
            else -> {
                null
            }
        }
    }

    fun logException(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }


    @WorkerThread
    fun getThumbnailForVideo(videoPath: String?): Bitmap? {
        val thumbnailFileDir: File? = FileUtils.instance?.getThumbnailDir()
        if (thumbnailFileDir != null) {
            if (thumbnailFileDir.exists()) {
                val files = thumbnailFileDir.listFiles()
                if (files != null && files.isNotEmpty()) {
                    for (i in files.indices) {
                        val deleteFile = File(files[i].absolutePath)
                        deleteFile.delete()
                    }
                }
            }
        }
        var bMap: Bitmap? = null
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                val mmr = MediaMetadataRetriever()
                if (videoPath != null) {
                    val file = File(videoPath)
                    mmr.setDataSource(file.absolutePath)
                    var width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
                    var height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
                    if (width.coerceAtMost(height) > 720) {
                        val ratio = width / height.toFloat()
                        if (width > height) {
                            height = 720
                            width = (height * ratio).toInt()
                        } else {
                            width = 720
                            height = (width / ratio).toInt()
                        }
                    }
                    bMap = ThumbnailUtils.createVideoThumbnail(file, Size(width, height), null)
                    Log.d("thumnail", "w-->" + bMap.width + " h-->" + bMap.height)
                }
            } else {
                // do something for phones running an SDK before lollipop
                bMap = ThumbnailUtils.createVideoThumbnail(videoPath!!, MediaStore.Video.Thumbnails.MINI_KIND)
                Log.d("thumnail", "<29 " + "w-->" + bMap!!.width + " h-->" + bMap.height)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return bMap
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            @Suppress("DEPRECATION")
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
                context.packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            else context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

