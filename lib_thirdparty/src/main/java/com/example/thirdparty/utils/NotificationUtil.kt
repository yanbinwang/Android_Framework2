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
import android.net.Uri
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.scale
import com.example.common.utils.function.color
import com.example.common.utils.function.dp
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.glide.ImageLoader
import com.example.thirdparty.R
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * @description 通知构建类
 * @author yan
 * application中使用
 * private fun initNotification() {
 *    NotificationUtil.init()
 * }
 */
object NotificationUtil {
    // 通知栏管理
    private var notificationManager: NotificationManager? = null
    // 弱handler切主线程
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
    // 线程安全的 ID 生成（初始值 100，每次自增）
    private val notificationIdCounter by lazy { AtomicInteger(100) }
    private val requestCodeCounter by lazy { AtomicInteger(100) }
    // 获取ID
    private val notificationId get() = notificationIdCounter.getAndIncrement()
    private val requestCode get() = requestCodeCounter.getAndIncrement()

    /**
     * BaseApplication中初始化
     */
    fun init(context: Context) {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        // 避免重复创建渠道（检查是否已存在）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = context.string(R.string.notificationChannelId)
            val channelName = context.string(R.string.notificationChannelName)
            notificationManager?.createNotificationChannelIfNeeded(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    /**
     * 带图片/跳转的通知栏
     */
    fun Context?.showSimpleNotification(title: String?, text: String?, imageUrl: String?, intent: Intent?) {
        this ?: return
        //确定是否具备跳转
        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            //创建通知栏跳转
            pendingIntent = getPendingIntent(requestCode, intent, getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT))
        }
        //创建通知栏构建器
        val notificationBuilder = builder(title = title.orEmpty(), text = text.orEmpty(), pendingIntent = pendingIntent)
        if (!imageUrl.isNullOrEmpty()) {
            // 防止 Context 泄漏
            val weakContext = WeakReference(this)
            val context = weakContext.get()
            context ?: return
            ImageLoader.instance.downloadImage(context, imageUrl, onDownloadComplete = {
                val bitmap = BitmapFactory.decodeFile(it?.absolutePath)
                if (bitmap != null) {
                    val smallIcon = bitmap.scale(64.dp, 64.dp, false)
                    val bigLargeIcon: Bitmap? = null
                    notificationBuilder
                        .setLargeIcon(smallIcon)
                        .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(bigLargeIcon))
                }
                //整体下载完成后，创建通知
                notify(notificationId, notificationBuilder.build())
            })
        } else {
            //没有图片的，直接创建通知
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
            notify(notificationId, notificationBuilder.build())
        }
    }

    /**
     * 创建通知栏
     */
    fun notify(id: Int, notification: Notification?) {
        notification ?: return
        // 在主线程调用 notify（确保 UI 相关操作安全）
        weakHandler.post {
            notificationManager?.notify(id, notification)
        }
    }

    /**
     * 避免重复创建渠道（Android 官方推荐）
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun NotificationManager.createNotificationChannelIfNeeded(channel: NotificationChannel) {
        getNotificationChannel(channel.id) ?: createNotificationChannel(channel)
    }

    /**
     * 创建通知栏构建器
     * @param icon 通知栏小图标资源 ID，默认为 R.mipmap.ic_notification
     * @param title 通知栏标题，默认为空字符串
     * @param text 通知栏内容，默认为空字符串
     * @param argb 通知栏颜色资源 ID，默认为 R.color.textWhite
     * @param autoCancel 点击通知后是否自动取消，默认为 true
     * @param sound 通知栏声音 Uri，默认为系统默认通知声音
     * @param pendingIntent 点击通知后的跳转意图，默认为 null
     * @return 通知栏构建器实例
     */
    fun Context.builder(
        icon: Int = R.mipmap.ic_notification,
        title: String = "",
        text: String = "",
        argb: Int = R.color.textWhite,
        autoCancel: Boolean = true,
        sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        pendingIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, string(R.string.notificationChannelId))
            .setSmallIcon(icon)//96*96
            .setContentTitle(title)
            .setContentText(text)
            .setColor(color(argb))
            .setAutoCancel(autoCancel)
            .setSound(sound)
            //不主动调用setWhen则通知默认会使用通知被构建并发送时的时间戳，也就是大致相当于 System.currentTimeMillis() 所获取的当前时间，此处currentTimeStamp做一个大致修正
            .setWhen(currentTimeStamp)
        if (null != pendingIntent) {
            builder.setContentIntent(pendingIntent)
        }
        return builder
    }

    /**
     * FLAG_UPDATE_CURRENT
     * 如果 PendingIntent 已经存在，系统会更新这个 PendingIntent 中的额外数据（Intent 中的 extra），
     * 但不会改变 PendingIntent 的其他属性（如动作、数据、类型等）。也就是说，它会复用已有的 PendingIntent 实例，并更新其携带的数据
     *
     * FLAG_ONE_SHOT
     * 标志表示这个 PendingIntent 只能被使用一次。一旦 PendingIntent 被触发，它就会被自动取消，后续再次尝试使用该 PendingIntent 时将不会生效
     *
     * FLAG_CANCEL_CURRENT
     * 如果 PendingIntent 已经存在，会先取消该 PendingIntent，然后重新创建一个新的 PendingIntent。常用于需要确保每次使用的 PendingIntent 都是全新的场景
     *
     * FLAG_IMMUTABLE
     * 从 Android 12（API 级别 31）开始引入，用于指定 PendingIntent 是不可变的。使用该标志可以提高应用的安全性，防止 PendingIntent 被恶意篡改。
     * 在 Android 12 及以上版本，对于一些特定的 PendingIntent 创建，要求必须使用 FLAG_IMMUTABLE 或 FLAG_MUTABLE 标志
     */
    fun Context.getPendingIntent(requestCode: Int, intent: Intent, flags: Int): PendingIntent {
        return PendingIntent.getActivity(this, requestCode, intent, flags)
    }

    /**
     * 配置可变性
     */
    fun getPendingIntentFlags(baseFlags: Int): Int {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) baseFlags or PendingIntent.FLAG_MUTABLE else baseFlags
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Android S 及以上必须显式指定可变性，推荐默认使用 FLAG_IMMUTABLE（更安全）
                baseFlags or PendingIntent.FLAG_IMMUTABLE
            }
            else -> baseFlags
        }
    }

}