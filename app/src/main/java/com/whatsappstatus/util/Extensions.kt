package com.whatsappstatus.util

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.whatsappstatus.models.StatusData
import java.io.File

object Extensions {

    fun <T> List<T>.getImageFiles(): MutableList<StatusData> {
        val list = mutableListOf<StatusData>()
        for (imgFile in this) {
            if (imgFile is DocumentFile) {
                if (
                    imgFile.name?.endsWith(".jpg") == true
                    || imgFile.name?.endsWith(".jpeg") == true
                    || imgFile.name?.endsWith(".png") == true
                ) {
                    list.add(StatusData(imgFile.uri, imgFile.name))
                }
            } else if (imgFile is File) {
                if (
                    imgFile.name.endsWith(".jpg") || imgFile.name.endsWith(".jpeg") || imgFile.name.endsWith(".png")
                ) {
                    list.add(StatusData(imgFile.toUri(), imgFile.name))
                }
            }
        }
        return list
    }


    fun <T> List<T>.getVideoFiles(): MutableList<StatusData> {
        val list = mutableListOf<StatusData>()
        for (imgFile in this) {
            if (imgFile is DocumentFile) {
                if (imgFile.name?.endsWith(".mp4") == true) {
                    list.add(StatusData(imgFile.uri, imgFile.name))
                }
            } else if (imgFile is File) {
                if (imgFile.name.endsWith(".mp4")) {
                    list.add(StatusData(imgFile.toUri(), imgFile.name))
                }
            }
        }
        return list
    }


    internal fun View.findParentById(@IdRes id: Int): ViewGroup? {
        return if (this.id == id) {
            this as? ViewGroup
        } else {
            (parent as? View)?.findParentById(id)
        }
    }

    fun View?.showKeyBoard() {
        this?.requestFocus()
        val methodManager = this?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (this != null) methodManager?.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }

    fun View?.hideKeyboard() {
        this?.requestFocus()
        val methodManager = this?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (this != null)
            methodManager?.hideSoftInputFromWindow(this.windowToken, 0)
    }


    fun View.setOnClickListenerWithDebounce(debounceTime: Long = 600L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()

                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    /**
     * Show a standard toast just contains text.
     * @param text - The text to show. Can be formatted text.
     */
    fun Context.showToast(text: CharSequence, length: Int) {
        Toast.makeText(this, text, length).show()
    }

    /**
     * Show a standard toast of short duration that just contains text.
     * @param text - The text to show. Can be formatted text.
     */
    fun Context.showShortToast(text: CharSequence) {
        showToast(text, Toast.LENGTH_SHORT)
    }

    /**
     * Show a standard toast of short duration that contains text from a resource.
     * @param resId – The resource id of the string resource to use. Can be formatted text.
     */
    fun Context.showShortToast(@StringRes resId: Int) {
        showToast(getText(resId), Toast.LENGTH_SHORT)
    }

    /**
     * Show a standard toast of short duration that contains text from a resource.
     * @param resId – The resource id of the string resource to use. Can be formatted text.
     */
    fun Context.showShortToast(@StringRes resId: Int, vararg formatArgs: Any?) {
        showToast(getString(resId, *formatArgs), Toast.LENGTH_SHORT)
    }

    /**
     * Show a standard toast of long duration that just contains text.
     * @param text - The text to show. Can be formatted text.
     */
    fun Context.showLongToast(@StringRes resId: Int) {
        showToast(getText(resId), Toast.LENGTH_LONG)
    }

    /**
     * Show a standard toast of long duration that contains text from a resource.
     * @param resId – The resource id of the string resource to use. Can be formatted text.
     */
    fun Context.showLongToast(text: CharSequence) {
        showToast(text, Toast.LENGTH_LONG)
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.postValue(this.value)
    }


    fun View?.showIndefiniteSnackBarWithAction( message: String?, actionMsg: String?, snackCallBack: () -> Unit): Snackbar? {
        return try {
            val snackBar = this?.let { Snackbar.make(it, message ?: "", Snackbar.LENGTH_INDEFINITE) }
            snackBar?.setAction(actionMsg) { snackCallBack() }
            snackBar?.show()
            snackBar
        } catch (e: Exception) {
            Utils.logException(e)
            null
        }
    }

}