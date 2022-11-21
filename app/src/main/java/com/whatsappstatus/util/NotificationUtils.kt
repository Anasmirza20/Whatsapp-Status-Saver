package com.whatsappstatus.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.HomeActivity
import com.whatsappstatus.util.Constants.CHANNEL_DESCRIPTION
import com.whatsappstatus.util.Constants.CHANNEL_ID
import com.whatsappstatus.util.Constants.CHANNEL_NAME
import com.whatsappstatus.util.Constants.NOTIFICATION_ID
import java.util.*
import kotlin.math.abs

object NotificationUtils {
    var REEL_UPLOAD_CHANNEL = "reel_upload_channel"
    var ACTIVITY_STARTED_FROM_NOTIFICATION = "activity_started_from_notification"
    const val NOTIFICATION_CHANNEL_REELSFIZ = "notification_channel_reelsfiz"


    fun cancelNotification(context: Context, uid: Int) {
        val notificationManager = NotificationManagerCompat.from(context.applicationContext)
        notificationManager.cancel(uid)
    }

    fun createVideoUploadNotificationBuilder(
        context: Context?,
        notificationManager: NotificationManagerCompat,
        channelId: String?,
        content: String?
    ): NotificationCompat.Builder? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = null
            channel = notificationManager.getNotificationChannel(REEL_UPLOAD_CHANNEL)
            if (channel == null) {
                channel = NotificationChannel(
                    REEL_UPLOAD_CHANNEL, channelId, NotificationManager.IMPORTANCE_HIGH
                )
            }
            notificationManager.createNotificationChannel(channel)
        }
        val activityIntent = Intent(context, HomeActivity::class.java)
        activityIntent.putExtra(ACTIVITY_STARTED_FROM_NOTIFICATION, true)
        val activityPendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            activityPendingIntent = PendingIntent.getActivity(
                context, abs(Random().nextInt()), activityIntent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            activityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            activityPendingIntent = PendingIntent.getActivity(
                context, abs(Random().nextInt()), activityIntent, PendingIntent.FLAG_ONE_SHOT
            )
        }
        val builder = NotificationCompat.Builder(context!!, channelId!!)
        builder.setContentTitle(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setOnlyAlertOnce(true)
            .setChannelId(REEL_UPLOAD_CHANNEL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(activityPendingIntent)
        return builder
    }

    fun downloadNotificationChannel(context: Context) {
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
    }


    fun showSmallNotification(
        icon: Int, title: String?, notificationId: Int,
        message: String?, tag: String?, resultPendingIntent: PendingIntent?, notificationSound: Uri?, context: Context
    ) {

//        if (!sessionManager.getBooleanPref(StorageVars.USER_NOT_FAMILIAR, false)) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_REELSFIZ)
            /* Create or update. */if (channel == null) channel = NotificationChannel(
                NOTIFICATION_CHANNEL_REELSFIZ,
                "ReelsFiz", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(message)
        val notification: Notification
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_REELSFIZ)
        mBuilder.setTicker(title).setWhen(0)
            .setAutoCancel(true)
            .setChannelId(NOTIFICATION_CHANNEL_REELSFIZ)
            .setContentTitle(title)
            .setContentIntent(resultPendingIntent)
            .setContentText(message)
            .setStyle(bigTextStyle)
            .setGroup("ReelsFiz-App")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, icon))
        if (notificationSound != null) mBuilder.setSound(notificationSound) else mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        notification = mBuilder.build()
        notificationManager.notify(tag, notificationId, notification)
    }
}