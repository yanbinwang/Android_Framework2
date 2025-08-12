package com.example.thirdparty.utils

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.builder.shortToast
import com.example.common.utils.builder.suspendingDownloadPic
import com.example.common.utils.function.color
import com.example.common.utils.function.decodeResource
import com.example.common.utils.function.dp
import com.example.common.utils.function.string
import com.example.common.utils.permission.RequestPermissionRegistrar
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.function.value.isMainThread
import com.example.thirdparty.R
import com.example.thirdparty.utils.NotificationUtil.hasNotificationPermission
import com.example.thirdparty.utils.NotificationUtil.navigateToNotificationSettings
import com.example.thirdparty.utils.NotificationUtil.requestNotificationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * @description 通知构建类
 * @author yan
 * application中使用
 * private fun initNotification() {
 *    NotificationUtil.init()
 * }
 * NotificationCompat.Style 接口提供了多种样式来丰富通知的显示效果
 * 1. BigTextStyle
 * 作用：显示长文本内容，折叠时显示摘要，展开时显示完整文本。
 * 核心方法：
 * bigText(String)：设置展开时的完整文本
 * setBigContentTitle(String)：设置展开时的标题
 * setSummaryText(String)：设置摘要文本
 * 适用场景：新闻应用、长消息通知。
 *
 * 2. BigPictureStyle
 * 作用：显示大图片，适合展示照片、新闻配图等。
 * 核心方法：
 * bigPicture(Bitmap)：设置展开时的大图 128dp*128dp
 * bigLargeIcon(Bitmap)：设置展开时左侧的大图标（可选）
 * setSummaryText(String)：设置图片下方的摘要
 * 适用场景：社交媒体、图片分享应用。
 *
 * 3. InboxStyle
 * 作用：以列表形式显示多条内容（类似邮件收件箱）。
 * 核心方法：
 * addLine(CharSequence)：添加一行内容（最多 7 行）
 * setBigContentTitle(String)：设置展开时的标题
 * setSummaryText(String)：设置底部摘要
 * 适用场景：邮件客户端、即时通讯应用。
 *
 * 4. MediaStyle
 * 作用：专为媒体播放设计，显示播放控制按钮。
 * 核心方法：
 * setMediaSession(MediaSession.Token)：关联媒体会话
 * setShowActionsInCompactView(int...)：设置折叠时显示的按钮索引
 * setShowCancelButton(boolean)：是否显示取消按钮
 * 适用场景：音乐播放器、视频应用。
 *
 * 5. DecoratedCustomViewStyle
 * 作用：增强自定义通知视图的显示效果，自动添加标准装饰（如小图标、时间）。
 * 核心方法：
 * 无特殊方法，需配合 setCustomContentView() 使用。
 * 适用场景：需要高度自定义布局的通知。
 */
object NotificationUtil {
    // 通知栏管理
    private var notificationManager: NotificationManager? = null
    // 切主线程-》使用 SupervisorJob允许子协程独立失败，不会因某个通知发送失败而取消整个作用域，若无需处理子协程异常，也可直接使用 CoroutineScope(Main)（默认使用 Job()，但 SupervisorJob 更安全
    private val postScope by lazy { CoroutineScope(SupervisorJob() + Main.immediate) }
    // 线程安全的 ID 生成（初始值 100，每次自增）
    private val notificationIdCounter by lazy { AtomicInteger(100) }
    private val requestCodeCounter by lazy { AtomicInteger(100) }
    // 获取ID
    val notificationId get() = notificationIdCounter.getAndIncrement()
    val requestCode get() = requestCodeCounter.getAndIncrement()

    /**
     * BaseApplication中初始化
     */
    @JvmStatic
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
    @JvmStatic
    fun Context?.showSimpleNotification(
        title: String? = "",
        text: String? = "",
        imageUrl: String? = null,
        intent: Intent? = null
    ) {
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
            var bitmap: Bitmap? = null
            var largeIcon: Bitmap? = null
            var bigPicture: Bitmap? = null
            var bigLargeIcon: Bitmap? = null
            flow<Unit> {
                bitmap = BitmapFactory.decodeFile(requestAffair { suspendingDownloadPic(context, imageUrl) }) ?: throw RuntimeException("图片下载失败")
                /**
                 * setLargeIcon()	折叠状态下的左侧图标	64dp × 64dp	系统自动裁剪为圆形，建议提供正方形图片
                 * bigPicture()	展开状态下的大图区域	256dp × 256dp	建议使用横向矩形（如 2:1 比例），否则可能被拉伸或裁剪
                 * bigLargeIcon()	展开状态下替代 setLargeIcon() 的图标	128dp × 128dp	可选，若不设置则默认使用 setLargeIcon() 的图标（64dp 会被放大）
                 */
                largeIcon = bitmap?.scale(64.dp, 64.dp, false)
                bigPicture = bitmap?.scale(256.dp, 256.dp, false)
                bigLargeIcon = bitmap?.scale(128.dp, 128.dp, false)
                notificationBuilder
                    .setLargeIcon(largeIcon)
                    .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bigPicture).bigLargeIcon(bigLargeIcon))
            }.withHandling({
                notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
            }, {
                //整体下载完成后，创建通知
                notificationManager?.notify(notificationId, notificationBuilder.build())
                bitmap?.recycle()
                largeIcon?.recycle()
                bigPicture?.recycle()
                bigLargeIcon?.recycle()
            }).launchIn(postScope)
        } else {
            //没有图片的，直接创建通知
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
            notify(notificationId, notificationBuilder.build())
        }
    }

    /**
     * 创建通知栏
     */
    @JvmStatic
    fun notify(id: Int, notification: Notification?) {
        notification ?: return
        val notifyAction = {
            notificationManager?.notify(id, notification)
        }
        // 在主线程调用 notify（确保 UI 相关操作安全）
        if (!isMainThread) {
            postScope.launch {
                notifyAction()
            }
        } else {
            notifyAction()
        }
    }

    /**
     * 避免重复创建渠道（Android 官方推荐）
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun NotificationManager.createNotificationChannelIfNeeded(channel: NotificationChannel) {
        getNotificationChannel(channel.id) ?: createNotificationChannel(channel)
    }

    /**
     * 创建通知栏构建器
     * @param smallIconRes 通知栏小图标资源 ID，默认为 R.mipmap.ic_push_small
     * 必须设置：若不设置，通知将无法显示。
     * 尺寸要求：
     * 推荐使用 24dp × 24dp 的矢量图标（VectorDrawable）。
     * 需兼容不同屏幕密度（mdpi、hdpi、xhdpi 等），系统会自动缩放。
     * 格式要求：
     * 仅支持 alpha 通道（即图标应为透明背景，系统会自动应用主题色）。
     * 推荐使用 AndroidX 的 VectorAsset 或 VectorDrawable。
     * @param largeIconRes 通知栏展开大图标资源 ID，默认为 R.mipmap.ic_push_large
     * 建议设置：提升通知辨识度（如显示用户头像、应用 Logo）。
     * 尺寸要求：
     * 常规通知：推荐 64dp × 64dp（系统会自动裁剪为圆形）。
     * BigPictureStyle 样式：建议使用 128dp × 128dp 以适配展开视图。
     * 格式要求：
     * 支持任意格式（PNG、JPEG、Bitmap），但通常为正方形。
     * 背景建议透明，避免变形。
     * @param title 通知栏标题，默认为空字符串
     * @param text 通知栏内容，默认为空字符串
     * @param argb 通知栏颜色资源 ID，默认为 R.color.textWhite
     * @param autoCancel 点击通知后是否自动取消，默认为 true
     * @param sound 通知栏声音 Uri，默认为系统默认通知声音
     * @param pendingIntent 点击通知后的跳转意图，默认为 null
     * @return 通知栏构建器实例
     */
    @JvmStatic
    fun Context.builder(
        smallIconRes: Int = R.mipmap.ic_push_small,
        largeIconRes: Int = R.mipmap.ic_push_large,
        title: String = "",
        text: String = "",
        argb: Int = R.color.textWhite,
        autoCancel: Boolean = true,
        sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        pendingIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, string(R.string.notificationChannelId))
            .setSmallIcon(smallIconRes)//24dp × 24dp
            .setLargeIcon(decodeResource(largeIconRes))//64dp × 64dp
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
    @JvmStatic
    fun Context.getPendingIntent(requestCode: Int, intent: Intent, flags: Int): PendingIntent {
        return PendingIntent.getActivity(this, requestCode, intent, flags)
    }

    /**
     * 配置可变性
     */
    @JvmStatic
    fun getPendingIntentFlags(baseFlags: Int): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Android S 及以上必须显式指定可变性，推荐默认使用 FLAG_IMMUTABLE（更安全）
                baseFlags or PendingIntent.FLAG_IMMUTABLE
            }
            else -> baseFlags
        }
    }

    /**
     * 判断是否具备通知
     */
    @JvmStatic
    fun hasNotificationPermission(context: Context): Boolean {
        // Android 13及以上需要检查POST_NOTIFICATIONS权限
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12及以下默认拥有通知权限
            true
        }
    }

    /**
     * 通知权限(安卓13开始强制要求授予通知权限才能弹出通知)
     *  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
     * 请求权限的实现（需在Activity中）
     * private val requestPermissionLauncher = mActivity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
     * if (isGranted) {
     * startRecording()
     * } else {
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
     * mActivity.navigateToNotificationSettings()
     * }
     * }
     * }
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @JvmStatic
    fun ActivityResultLauncher<String>?.requestNotificationPermission() {
        this ?: return
        launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * 直接跳转对应通知设置
     */
    @JvmStatic
    fun Activity?.navigateToNotificationSettings() {
        this ?: return
        R.string.notificationGranted.shortToast()
        val intent = when {
            // Android 8.0+（API 26+）：直接跳通知设置
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    // 关键参数：指定应用包名
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            }
            // Android 6.0-7.1（API 23-25）：跳应用详情页
            else -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:$packageName".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }
        // 尝试启动Intent，防止仍有设备不支持
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 终极 fallback：跳系统设置首页
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

}

/**
 * 通知弹框的Dialog要与页面强管理,不能使用object
 * 可在基类中初始化
 */
class NotificationManager(private val mActivity: FragmentActivity, wrapper: RequestPermissionRegistrar) {
    private val mDialog by lazy { AppDialog(mActivity) }
    private var mListener: (hasPermissions: Boolean) -> Unit = {}
    private val mRequestPermissionResult = wrapper.registerResult { isGranted ->
        if (isGranted) {
            mListener.invoke(true)
        } else {
            mDialog
                .setParams(string(R.string.hint), string(R.string.permissionNotification))
                .setDialogListener({
                    mActivity.navigateToNotificationSettings()
                }, {
                    mListener.invoke(false)
                })
                .show()
        }
    }

    fun pullUpNotification() {
        if (hasNotificationPermission(mActivity)) {
            mListener.invoke(true)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mRequestPermissionResult.requestNotificationPermission()
            } else {
                mListener.invoke(true)
            }
        }
    }

    fun setOnNotificationListener(listener: (hasPermissions: Boolean) -> Unit = {}) {
        this.mListener = listener
    }

}