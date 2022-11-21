package com.whatsappstatus.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.whatsapp.reelsfiz.R
import java.text.SimpleDateFormat
import java.util.*

object CommonMethods {
    fun setThumbnail(
        context: Context,
        videoThumbnailUrl: String?,
        image: ShapeableImageView,
        default: Int
    ) {
        val videoThumbnail = ContextCompat.getDrawable(context, default)
        /* image.visibility = if (videoThumbnailUrl != null && videoThumbnailUrl.isNotEmpty()) {
             loadImage(
                 context,
                 videoThumbnailUrl,
                 image,
                 videoThumbnail
             )
             View.VISIBLE
         } else View.GONE*/
    }


    fun loadImage(
        context: Context?,
        url: String?,
        image: ImageView,
        errorDrawable: Int = R.drawable.error_placeholder,
        width: Int = 0,
        height: Int = 0,
        placeholder: Int = R.color.placeholder_color
    ) {
        context?.let {
            Glide.with(it).apply {
                load(url).error(errorDrawable).placeholder(placeholder).override(width, height)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            image.setImageDrawable(resource)
                            return true
                        }
                    }).into(image)
            }
        }
    }

    fun getFormattedDate(date: String): String {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatDate: Date? = df.parse(date)
        if (formatDate != null)
            c.time = formatDate
        return c.get(Calendar.DATE).toString() + "-" + SimpleDateFormat(
            "MMM",
            Locale.getDefault()
        ).format(c.time)
            .toString() + "-" + c.get(
            Calendar.YEAR
        ).toString()
    }

/*
    fun setLike(isLike: Boolean, like: MaterialTextView) = if (isLike) {
        like.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_like, 0, 0, 0
        )
        true
    } else {
        like.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_like_line_, 0, 0, 0
        )
        false
    }

    fun setDislike(isLike: Boolean, dislike: MaterialTextView) = if (isLike) {
        dislike.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_dislike, 0, 0, 0
        )
        true
    } else {
        dislike.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_dislike_line_, 0, 0, 0
        )
        false
    }*/
}