package com.example.thirdparty.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.common.BaseApplication
import com.example.common.utils.function.color
import com.example.common.utils.function.dp
import com.example.common.utils.i18n.string
import com.example.glide.ImageLoader
import com.example.thirdparty.R

/**
 * @description 通知构建类
 * @author yan
 * application中使用
 * private fun initNotification() {
 *    NotificationUtil.init()
 * }
 */
object NotificationUtil {
    private val notificationManager by lazy { BaseApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager }
    private var notificationId = 100
        get() = ++field
    private var requestCode = 100
        get() = ++field

    /**
     * BaseApplication中初始化
     */
    fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager?.createNotificationChannel(NotificationChannel(string(R.string.notificationChannelId), string(R.string.notificationChannelName), NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    fun Context?.showSimpleNotification(title: String?, body: String?, pictureUrl: String?, intent: Intent) {
        this ?: return
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT))
        val notificationBuilder = NotificationCompat.Builder(this, string(R.string.notificationChannelId))
                .setSmallIcon(R.mipmap.ic_notification)//96*96
                .setContentTitle(title.orEmpty())
                .setContentText(body.orEmpty())
                .setColor(color(R.color.textWhite))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        if (!pictureUrl.isNullOrEmpty()) {
            ImageLoader.instance.download(this, pictureUrl, onComplete = {
                val bitmap = BitmapFactory.decodeFile(it?.absolutePath)
                if (bitmap != null) {
                    val b: Bitmap? = null
                    val smallIcon = Bitmap.createScaledBitmap(bitmap, 64.dp, 64.dp, false)
                    notificationBuilder
                        .setLargeIcon(smallIcon)
                        .setStyle(NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(b))
                }
                notificationManager?.notify(notificationId, notificationBuilder.build())
            })
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
            notificationManager?.notify(notificationId, notificationBuilder.build())
        }
    }

    fun Context.getSimpleNotification(title: String?, body: String?, intent: Intent?): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, string(R.string.notificationChannelId))
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(title.orEmpty())
                .setContentText(body.orEmpty())
                .setColor(color(R.color.textWhite))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, getPendingIntentFlags(PendingIntent.FLAG_ONE_SHOT))
            notificationBuilder.setContentIntent(pendingIntent)
        }
        return notificationBuilder.build()
    }

    private fun getPendingIntentFlags(baseFlags: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) baseFlags or PendingIntent.FLAG_MUTABLE else baseFlags
    }

}