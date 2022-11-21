package com.whatsappstatus.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.*
import java.util.zip.DeflaterOutputStream


class FileUtils private constructor() {
    var sComparator =
        Comparator<File> { f1, f2 -> // Sort alphabetically by lower case, which is much cleaner
            f1.name.lowercase().compareTo(
                f2.name.lowercase()
            )
        }
    var sFileFilter: FileFilter = FileFilter { file ->
        val fileName = file.name
        // Return files only (not directories) and skip hidden files
        file.isFile && !fileName.startsWith(HIDDEN_PREFIX)
    }
    var sDirFilter: FileFilter = FileFilter { file ->
        val fileName = file.name
        // Return directories only and skip hidden directories
        file.isDirectory && !fileName.startsWith(HIDDEN_PREFIX)
    }

    /**
     * Get the JSON string from the dummy assets file
     */
    fun getJSONFromAssetsFile(context: Context, filename: String?): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = context.assets.open(filename!!)
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }
        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    fun getFileNameFromUrl(uri: String?): String? {
        if (uri == null) {
            return null
        }
        val slash = uri.lastIndexOf("/") + 1
        return if (slash >= 1) {
            uri.substring(slash)
        } else {
            // No filename.
            ""
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    fun isMediaUri(uri: Uri): Boolean {
        return "media".equals(uri.authority, ignoreCase = true)
    }

    fun getUri(file: File?): Uri? {
        return if (file != null) {
            Uri.fromFile(file)
        } else null
    }

    fun getPathWithoutFilename(file: File?): File? {
        return if (file != null) {
            if (file.isDirectory) {
                // no file to be split off. Return everything
                file
            } else {
                val filename = file.name
                val filepath = file.absolutePath

                // Construct path without file name.
                var pathwithoutname = filepath.substring(
                    0,
                    filepath.length - filename.length
                )
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length - 1)
                }
                File(pathwithoutname)
            }
        } else null
    }

    /**
     * @return The MIME type for the given file.
     */
    fun getMimeType(file: File): String {
        val extension = getExtension(file.name)
        return if (extension!!.isNotEmpty()) MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            extension.substring(1)
        ).toString() else "application/octet-stream"
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = uri?.let {
                context.contentResolver.query(
                    it, projection, selection, selectionArgs,
                    null
                )
            }
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG) DatabaseUtils.dumpCursor(cursor)
                var column_index = 0
                try {
                    column_index = cursor.getColumnIndexOrThrow(column)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (column_index != -1) cursor.getString(column_index) else ""
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     * @see .isLocal
     * @see .getFile
     */
    fun getPath(context: Context, uri: Uri): String? {
        if (DEBUG)
            Log.d(
                ",",
                "Authority: " + uri.authority +
                        ", Fragment: " + uri.fragment +
                        ", Port: " + uri.port +
                        ", Query: " + uri.query +
                        ", Scheme: " + uri.scheme +
                        ", Host: " + uri.host +
                        ", Segments: " + uri.pathSegments.toString()
            )

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId: String
                docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id: String = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }
    /**
     * method to get content uri usually used for metadat retriever
     *
     * @param context of the calling actvity
     * @param uri     should be having authority of MediaTypeProvider
     * @return
     */
    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @author paulburke
     * @see .getPath
     */
    fun getFile(context: Context, uri: Uri?): File? {
        if (uri != null) {
            val path = getPath(context, uri)
            if (path != null && isLocal(path)) {
                return File(path)
            }
        }
        return null
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     * @author paulburke
     */
    fun getReadableFileSize(size: Int): String {
        val BYTES_IN_KILOBYTES = 1024
        val dec = DecimalFormat("###.#")
        val KILOBYTES = " KB"
        val MEGABYTES = " MB"
        val GIGABYTES = " GB"
        var fileSize = 0f
        var suffix = KILOBYTES
        if (size > BYTES_IN_KILOBYTES) {
            fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES
                    suffix = GIGABYTES
                } else {
                    suffix = MEGABYTES
                }
            }
        }
        return dec.format(fileSize.toDouble()) + suffix
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     * @author paulburke
     */
    fun createGetContentIntent(): Intent {
        // Implicitly allow the user to select a particular kind of data
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // The MIME data type filter
        intent.type = "*/*"
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    fun createRooterDirectory(): Boolean {
        return try {
            val file: File = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "Rooter"
            )
            file.mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /*fun isImageFilePresent(fileName: String): File? {
        return try {
            val file: File = File(StaticVars.STORAGE_LOCATION.toString() + fileName + ".jpg")
            if (file.exists()) file else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/

    private fun createDirectory(path: String) {
        try {
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun deleteFile(path: String?) {
        try {
            val file = File(path)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteTempFiles() {
        val bichooserDirec: File = File(
            Environment.getExternalStorageDirectory().toString() + File.separator + "bichooser"
        )
        if (bichooserDirec.exists() && bichooserDirec.isDirectory) {
            for (file1 in bichooserDirec.listFiles()) {
                file1.delete()
            }
        }
    }

    fun getGzipFileName(ext: String?, fileName: String): String {
        return fileName.replace(ext!!, "gzip")
    }

    fun getZlibFileName(ext: String?, fileName: String): String {
        return fileName.replace(ext!!, "zlib")
    }

    fun convertBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        path: String?,
        recyclerBitmap: Boolean
    ): String? {
        var imageFile: File? = null
        return try {
            imageFile = File(path)
            val os: OutputStream = BufferedOutputStream(FileOutputStream(imageFile))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os)
            os.flush()
            os.close()
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(imageFile)
                )
            )
            if (recyclerBitmap) {
                bitmap.recycle()
            }
            "file://" + imageFile.absolutePath
        } catch (e: Exception) {
//            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    fun convertBitmapToPNGFile(context: Context, bitmap: Bitmap, path: String?): String? {
        var imageFile: File? = null
        return try {
            imageFile = File(path)
            val os: OutputStream = BufferedOutputStream(FileOutputStream(imageFile))
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, os)
            os.flush()
            os.close()
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(imageFile)
                )
            )
            bitmap.recycle()
            "file://" + imageFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun deflate(source_filepath: String?, destinaton_zip_filepath: String?) {
        val buffer = ByteArray(1024)
        try {
            val fileOutputStream = FileOutputStream(destinaton_zip_filepath)
            val deflaterOutputStream = DeflaterOutputStream(fileOutputStream)
            val fileInput = FileInputStream(source_filepath)
            var bytesRead: Int
            var offset = 0
            while (fileInput.read(buffer).also { bytesRead = it } > 0) {
                deflaterOutputStream.write(buffer, offset, bytesRead)
                offset += bytesRead
            }
            fileInput.close()
            deflaterOutputStream.flush()
            deflaterOutputStream.finish()
            deflaterOutputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()
            println("The file was compressed successfully!")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun deflateBase64String(source_filepath: String?, destinaton_zip_filepath: String?) {
        var buffer: ByteArray? = ByteArray(1024)
        try {
            val fileOutputStream = FileOutputStream(destinaton_zip_filepath)
            val deflaterOutputStream = DeflaterOutputStream(fileOutputStream)
            val fileInput = FileInputStream(source_filepath)
            var bytesRead: Int
            var offset = 0
            while (fileInput.read(buffer).also { bytesRead = it } > 0) {
                buffer = Base64.encode(buffer, Base64.DEFAULT)
                deflaterOutputStream.write(buffer, offset, bytesRead)
                offset += bytesRead
            }
            fileInput.close()
            deflaterOutputStream.flush()
            deflaterOutputStream.finish()
            deflaterOutputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()
            println("The file was compressed successfully!")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }


    fun getThumbnailDir(): File {
        val picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val rooterDir = File(picDir, "Rooter")
        if (!rooterDir.exists()) {
            rooterDir.mkdirs()
        }
        val thumbnailDir = File(rooterDir, "Thumbnail")
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs()
        }
        return thumbnailDir
    }

    companion object {
        var singleton: FileUtils? = null

        @get:Synchronized
        val instance: FileUtils?
            get() {
                if (singleton == null) {
                    singleton = FileUtils()
                }
                return singleton
            }
        const val MIME_TYPE_AUDIO = "audio/*"
        const val MIME_TYPE_TEXT = "text/*"
        const val MIME_TYPE_IMAGE = "image/*"
        const val MIME_TYPE_VIDEO = "video/*"
        const val MIME_TYPE_APP = "application/*"
        const val HIDDEN_PREFIX = "."

        /**
         * TAG for log messages.
         */
        const val TAG = "FileUtils"
        private const val DEBUG = false // Set to true to enable logging
    }
}