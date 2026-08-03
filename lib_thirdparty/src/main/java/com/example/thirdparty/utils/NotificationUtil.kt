package com.example.thirdparty.utils

import android.Manifest
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
import android.support.v4.media.session.MediaSessionCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.fragment.app.FragmentActivity
import com.example.common.BaseApplication
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.builder.suspendingDownloadPic
import com.example.common.utils.function.color
import com.example.common.utils.function.decodeResource
import com.example.common.utils.function.dp
import com.example.common.utils.function.getActivityPendingIntent
import com.example.common.utils.function.pullUpNotification
import com.example.common.utils.function.safeRecycle
import com.example.common.utils.function.string
import com.example.common.utils.permission.RequestPermissionRegistrar
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.function.value.isMainThread
import com.example.thirdparty.R
import com.example.thirdparty.utils.NotificationUtil.hasNotificationPermission
import com.example.thirdparty.utils.NotificationUtil.requestNotificationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * 通知构建类
 * application中使用
 * private fun initNotification() {
 *    NotificationUtil.init()
 * }
 *
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
 * 3. MediaStyle
 * 作用：专为媒体播放设计，显示播放控制按钮。
 * 核心方法：
 * setMediaSession(MediaSession.Token)：关联媒体会话
 * setShowActionsInCompactView(int...)：设置折叠时显示的按钮索引
 * setShowCancelButton(boolean)：是否显示取消按钮
 * 适用场景：音乐播放器、视频应用。
 *
 * 4. DecoratedCustomViewStyle
 * 作用：增强自定义通知视图的显示效果，自动添加标准装饰（如小图标、时间）。
 * 核心方法：
 * 无特殊方法，需配合 setCustomContentView() 使用。
 * 适用场景：需要高度自定义布局的通知。
 */
object NotificationUtil {
    // 通知栏管理
    private var notificationManager: NotificationManager? = null
    // 切主线程-》使用 SupervisorJob 允许子协程独立失败，不会因某个通知发送失败而取消整个作用域，若无需处理子协程异常，也可直接使用 CoroutineScope(Main)（默认使用 Job()，但 SupervisorJob 更安全
    private val notificationScope by lazy { CoroutineScope(SupervisorJob() + Main.immediate) }
    // 线程安全的 ID 生成（初始值 100，每次自增）
    private val notificationIdCounter by lazy { AtomicInteger(100) }
    private val requestCodeCounter by lazy { AtomicInteger(100) }

    /**
     * 获取 ID
     * 1) 短生命周期录屏服务（随页面开关）/普通推送通知 -> 自增 ID
     * 2) 常驻前台服务 -> 固定 ID（<100）)
     */
    val notificationId get() = notificationIdCounter.getAndIncrement()
    val requestCode get() = requestCodeCounter.getAndIncrement()

    // 系统日志收集服务
    const val NOTIFY_ID_SYSTEM_LOG = 1
    // 高德定位前台服务
    const val NOTIFY_ID_LOCATION = 2
    // 录音前台服务
    const val NOTIFY_ID_AUDIO_RECORD = 3
    // 录屏前台服务
    const val NOTIFY_ID_SCREEN_RECORD = 4
    // 音频前台服务
    const val NOTIFY_ID_AUDIO_MEDIA = 5

    /**
     * BaseApplication 中初始化
     */
    fun init(applicationContext: Context) {
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        // 避免重复创建渠道（检查是否已存在）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = string(R.string.notificationChannelId)
            val channelName = string(R.string.notificationChannelName)
            notificationManager?.createNotificationChannelIfNeeded(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    /**
     * 避免重复创建渠道
     * @param channelId 必须唯一，系统以此作为渠道的主键，相同 ID = 同一个渠道
     * @param channelName 仅用于系统设置页面的展示文案，不参与身份识别
     * val channel = NotificationChannel("my_channel_id", "我的渠道名", NotificationManager.IMPORTANCE_DEFAULT)
     * val id: String = channel.id  // "my_channel_id"
     * val name: CharSequence = channel.name // "我的渠道名"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun NotificationManager.createNotificationChannelIfNeeded(channel: NotificationChannel) {
        getNotificationChannel(channel.id) ?: createNotificationChannel(channel)
    }

    /**
     * 创建通知栏构建器 (普通通知样式)
     * @param smallIconRes 通知栏小图标资源 ID，默认为 R.mipmap.ic_push_small （必传）
     * 必须设置：若不设置，通知将无法显示。
     * 尺寸要求：
     * 推荐使用 24dp × 24dp 的矢量图标（VectorDrawable）。
     * 需兼容不同屏幕密度（mdpi、hdpi、xhdpi 等），系统会自动缩放。
     * 格式要求：
     * 仅支持 alpha 通道（即图标应为透明背景，系统会自动应用主题色）。
     * 推荐使用 AndroidX 的 VectorAsset 或 VectorDrawable。
     * @param largeIconRes 通知栏展开大图标资源 ID，默认为 R.mipmap.ic_push_large （可空）
     * 建议设置：提升通知辨识度（如显示用户头像、应用 Logo）。
     * 尺寸要求：
     * 常规通知：推荐 64dp × 64dp（系统会自动裁剪为圆形）。
     * BigPictureStyle 样式：建议使用 128dp × 128dp 以适配展开视图。
     * 格式要求：
     * 支持任意格式（PNG、JPEG、Bitmap），但通常为正方形。
     * 背景建议透明，避免变形。
     * @param title 通知栏标题，默认为空
     * @param text 通知栏内容，默认为空
     * @param argb 通知栏颜色资源 ID，默认为 R.color.textWhite
     * @param autoCancel 点击通知后是否自动取消，默认为 true
     * @param sound 通知栏声音 Uri，默认为系统默认通知声音
     * @param silent 是否静音，默认 false（有声） 此参数仅控制单次通知的声音，不影响通知渠道的默认声音设置
     * @param ongoing true 无法手动滑动删除 false 可以左滑/右滑清除
     * @param priority 控制“视觉侵略性”
     *  PRIORITY_MAX: 全屏弹出 + 声音 + 震动 + 常驻顶部 -> 来电、闹钟、紧急警报
     *  PRIORITY_HIGH: 横幅弹出(Heads-Up) + 声音 + 排序靠前 -> IM消息、日程提醒、重要预警
     *  PRIORITY_DEFAULT: 状态栏图标 + 下拉可见 + 默认排序 -> 普通资讯、应用更新
     *  PRIORITY_LOW: 状态栏图标 + 下拉可见 + 无声音无震动 -> 前台服务、下载进度、媒体播放
     *  PRIORITY_MIN: 仅在下拉列表底部显示，状态栏无图标 -> 后台同步完成、调试日志
     * @param category 告诉系统“这是什么” 系统如何理解和分组这条通知
     *  CATEGORY_MESSAGE: 人与人通信 -> IM、短信、邮件
     *  CATEGORY_CALL: 通话 -> 来电、VoIP
     *  CATEGORY_ALARM: 闹钟/计时器 -> 闹钟、倒计时
     *  CATEGORY_EVENT: 日历事件 -> 会议提醒
     *  CATEGORY_SERVICE: 后台/前台服务 -> 下载、上传、定位
     *  CATEGORY_TRANSPORT: 媒体播放 -> MediaStyle 自动设置
     *  CATEGORY_PROGRESS: 进度条 -> 文件传输、安装
     *  CATEGORY_STATUS: 设备/账号状态 -> 电量低、登录异常
     * @param pendingIntent 点击通知后的跳转意图，默认为 null
     * @return 通知栏构建器实例
     */
    fun Context.builder(
        smallIconRes: Int = R.mipmap.ic_push_small,
        largeIconRes: Int? = R.mipmap.ic_push_large,
        title: String? = null,
        text: String? = null,
        argb: Int = R.color.appTheme,
        autoCancel: Boolean = true,
        sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        silent: Boolean = false,
        ongoing: Boolean = false,
        priority: Int = PRIORITY_DEFAULT,
        category: String? = null,
        pendingIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, string(R.string.notificationChannelId))
            // 24dp × 24dp (约 96px)
            .setSmallIcon(smallIconRes)
            // 64dp × 64dp (约 144px)
            .apply {
                largeIconRes?.let {
                    setLargeIcon(decodeResource(it))
                }
            }
            .setContentTitle(title)
            .setContentText(text)
            .setColor(color(argb))
            .setAutoCancel(autoCancel)
            .setSound(sound)
            .setSilent(silent)
            .setOngoing(ongoing)
            .setPriority(priority)
            .setCategory(category)
            // 不主动调用setWhen则通知默认会使用通知被构建并发送时的时间戳，也就是大致相当于 System.currentTimeMillis() 所获取的当前时间，此处 currentTimeStamp 做一个大致修正
            .setWhen(currentTimeStamp)
        // 仅在显式传入非空值时设置，null 等同于"未分类"
        category?.let {
            builder.setCategory(it)
        }
        pendingIntent?.let {
            builder.setContentIntent(it)
        }
        return builder
    }

    /**
     * 长文本样式扩展
     */
    fun NotificationCompat.Builder.asBigText(
        bigText: CharSequence,
        bigContentTitle: CharSequence? = null,
        summaryText: CharSequence? = null
    ): NotificationCompat.Builder {
        val style = NotificationCompat.BigTextStyle().bigText(bigText)
        bigContentTitle?.let {
            style.setBigContentTitle(it)
        }
        summaryText?.let {
            style.setSummaryText(it)
        }
        return setStyle(style)
    }

    /**
     * 大图样式扩展（仅设置样式，不涉及图片下载）
     */
    fun NotificationCompat.Builder.asBigPicture(
        picture: Bitmap,
        bigLargeIcon: Bitmap? = null,
        bigContentTitle: CharSequence? = null,
        summaryText: CharSequence? = null
    ): NotificationCompat.Builder {
        val style = NotificationCompat.BigPictureStyle().bigPicture(picture)
        bigLargeIcon?.let {
            style.bigLargeIcon(it)
        }
        bigContentTitle?.let {
            style.setBigContentTitle(it)
        }
        summaryText?.let {
            style.setSummaryText(it)
        }
        return setStyle(style)
    }

    /**
     * 媒体播放样式扩展（必须先创建并激活 MediaSession，否则通知无法响应播放控制， Style 不在 androidx.core 里，而是独立在 androidx.media 库中）
     * @param token MediaSession.Token，关联播放会话
     * @param showActionsInCompactView 折叠态显示的 Action 索引（对应 addAction 的顺序）
     *        例如传入 0,1,2 表示前三个按钮在折叠态可见，最多支持3个
     * @param showCancelButton Android 8.0以下显示取消按钮（高版本已废弃，传false即可）
     */
    fun NotificationCompat.Builder.asMedia(
        token: MediaSessionCompat.Token,
        vararg showActionsInCompactView: Int,
        showCancelButton: Boolean = false
    ): NotificationCompat.Builder {
        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(token)
            .setShowCancelButton(showCancelButton)
        if (showActionsInCompactView.isNotEmpty()) {
            style.setShowActionsInCompactView(*showActionsInCompactView)
        }
        return setStyle(style)
    }

//    /**
//     * 自定义视图样式扩展（保留系统标准装饰：小图标、时间、App名称等）
//     * 适用场景：下载进度条、自定义播放器控件、复杂业务卡片
//     * @param contentView 折叠态自定义布局
//     * @param bigContentView 展开态自定义布局（可选，不传则折叠/展开同布局）
//     */
//    fun NotificationCompat.Builder.asDecoratedCustomView(
//        contentView: RemoteViews,
//        bigContentView: RemoteViews? = null
//    ): NotificationCompat.Builder {
//        setStyle(NotificationCompat.DecoratedCustomViewStyle())
//        setCustomContentView(contentView)
//        bigContentView?.let {
//            setCustomBigContentView(it)
//        }
//        return this
//    }

    /**
     * 发送纯文本通知
     */
    fun Context.buildTextNotification(
        title: String,
        text: String,
        intent: Intent? = null,
        summaryText: String? = null,
        ongoing: Boolean = false,
        notify: Boolean = true,
        notifyId: Int? = null
    ): Notification {
        val pendingIntent = intent?.let {
            getActivityPendingIntent(requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = builder(title = title, text = text, ongoing = ongoing, pendingIntent = pendingIntent)
            .asBigText(bigText = text, summaryText = summaryText)
            .build()
        if (notify) {
            notification.notify(notifyId ?: notificationId)
        }
        return notification
    }

    /**
     * 发送带网络图片的通知（异步下载 + 失败回退）
     */
    fun Context.buildImageNotification(
        title: String,
        text: String,
        bitmap: Bitmap,
        intent: Intent? = null,
        summaryText: String? = null,
        ongoing: Boolean = false,
        notify: Boolean = true,
        notifyId: Int? = null
    ): Notification {
        val pendingIntent = intent?.let {
            getActivityPendingIntent(requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        /**
         * NotificationCompat.Builder 内部持有并传递给系统服务，手动回收会导致异步读取时出现空白或崩溃。交给系统 + GC 处理
         * 1) setLargeIcon(): 折叠状态下的左侧图标 (64dp × 64dp) -> 系统自动裁剪为圆形，建议提供正方形图片
         * 2) bigPicture(): 展开状态下的大图区域 (256dp × 256dp) -> 建议使用横向矩形（如 2:1 比例），否则可能被拉伸或裁剪
         * 3) bigLargeIcon(): 展开状态下替代 setLargeIcon() 的图标 (128dp × 128dp) -> 显式传入 null，告诉系统在展开态时清除右侧/左下角的图标
         */
        val largeIcon = bitmap.scale(64.dp, 64.dp, false)
        val bigPicture = bitmap.scale(256.dp, 256.dp, false)
//        val bigLargeIcon = bitmap.scale(128.dp, 128.dp, false)
        val bigLargeIcon = null
        val notification = builder(title = title, text = text, ongoing = ongoing, pendingIntent = pendingIntent)
            .setLargeIcon(largeIcon)
            .asBigPicture(picture = bigPicture, bigLargeIcon = bigLargeIcon, summaryText = summaryText)
            .build()
        if (notify) {
            notification.notify(notifyId ?: notificationId)
        }
        bitmap.safeRecycle()
        return notification
    }

    fun Context?.buildImageNotification(
        title: String,
        text: String,
        imageUrl: String? = null,
        intent: Intent? = null,
        summaryText: String? = null,
        ongoing: Boolean = false,
        notify: Boolean = true,
        notifyId: Int? = null,
        timeoutMs: Long = 5000L
    ) {
        this ?: return
        val resolvedNotifyId = if (notify) notifyId ?: notificationId else null
        if (!imageUrl.isNullOrEmpty()) {
            flow<Unit> {
                // 防止 Context 泄漏
                val context = WeakReference(this@buildImageNotification).get() ?: BaseApplication.instance.applicationContext
                // 5秒超时，根据实际图片大小调整
                val bitmap = withTimeoutOrNull(timeoutMs) {
                    BitmapFactory.decodeFile(requestAffair { suspendingDownloadPic(context, imageUrl) })
                } ?: throw RuntimeException("图片下载超时或失败")
                buildImageNotification(title, text, bitmap, intent, summaryText, ongoing, notify, resolvedNotifyId)
            }.withHandling({
                // 图片下载/处理失败时自动回退到 BigTextStyle 纯文本通知
                buildTextNotification(title, text, intent, summaryText, ongoing, notify, resolvedNotifyId)
            }).launchIn(notificationScope)
        } else {
            // 没有图片的，直接创建通知
            buildTextNotification(title, text, intent, summaryText, ongoing, notify, resolvedNotifyId)
        }
    }

    /**
     * 构建媒体播放通知
     * 此方法仅构建通知，不负责启动前台服务
     * 调用方需在 ForegroundService 中通过 startForeground(notifyId, notification) 启动
     * @param token 必须由调用方传入，从你的 MediaSessionCompat 实例获取
     * @param title 歌曲/视频标题
     * @param artist 艺术家/频道名
     * @param albumArt 专辑封面（可选）
     * @param actions 播放控制按钮列表
     * @param compactActionIndices 折叠态显示的按钮索引，默认 [1] 即播放/暂停
     * @param ongoing 是否常驻不可删除
     */
    fun Context.buildMediaNotification(
        token: MediaSessionCompat.Token,
        title: String,
        artist: String? = null,
        albumArt: Bitmap? = null,
        actions: List<NotificationCompat.Action>,
        compactActionIndices: IntArray = intArrayOf(1),
        silent: Boolean = true,
        ongoing: Boolean = true,
        notify: Boolean = false,
        notifyId: Int? = null
    ): Notification {
        val builder = builder(title = title, text = artist, silent = silent, ongoing = ongoing)
            .asMedia(token = token, showActionsInCompactView = compactActionIndices)
            // 媒体通知必须设置 Category
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            // 锁屏可见性：公开显示播放控件
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        // 添加所有播放控制按钮
        actions.forEach {
            builder.addAction(it)
        }
        // 设置专辑封面（展开态大图 + 折叠态小图标）
        albumArt?.let {
            // MediaStyle 展开时会自动使用 LargeIcon 作为封面，若需独立设置展开封面，可在此处额外处理
            builder.setLargeIcon(it)
        }
        // 是否开启
        val notification = builder.build()
        if (notify) {
            notification.notify(notifyId ?: notificationId)
        }
        return notification
    }

//    /**
//     * 构建带进度条的通知（基于 DecoratedCustomViewStyle）
//     * 此方法仅负责【创建/更新】通知，不负责发送
//     * 调用方需自行持有 notifyId 并调用 notification.notify(notifyId) 进行覆盖更新
//     * @param title 通知标题
//     * @param progress 当前进度 (0-100)
//     * @param max 最大进度值，默认 100
//     * @param isIndeterminate true=不确定进度(循环动画)，false=确定进度
//     * @param ongoing true=不可滑动删除(下载中)，false=可删除(下载完成/失败)
//     * @param contentLayoutRes 自定义布局资源ID，需包含 R.id.tv_title, R.id.progress_bar, R.id.tv_percent
//     *
//     * // 下载开始时
//     * val notifyId = NotificationUtil.notificationId
//     * val notification = context.buildProgressNotification(
//     *     title = "正在下载更新包",
//     *     progress = 0,
//     *     isIndeterminate = true,  // 初始阶段不确定进度
//     *     ongoing = true
//     * )
//     * notification.notify(notifyId)
//     * // 下载过程中（高频更新）
//     * val updatedNotification = context.buildProgressNotification(
//     *     title = "正在下载更新包",
//     *     progress = currentProgress,
//     *     isIndeterminate = false,
//     *     ongoing = true
//     * )
//     * updatedNotification.notify(notifyId)  // 相同ID覆盖更新
//     * // 下载完成
//     * val completeNotification = context.buildProgressNotification(
//     *     title = "下载完成",
//     *     progress = 100,
//     *     ongoing = false  // 允许用户滑动清除
//     * )
//     * completeNotification.notify(notifyId)
//     */
//    fun Context.buildProgressNotification(
//        title: String,
//        progress: Int,
//        max: Int = 100,
//        isIndeterminate: Boolean = false,
//        ongoing: Boolean = true,
//        @LayoutRes contentLayoutRes: Int = R.layout.notification_download_progress
//    ): Notification {
//        val remoteViews = RemoteViews(packageName, contentLayoutRes).apply {
//            setTextViewText(R.id.tv_title, title)
//            setProgressBar(R.id.progress_bar, max, progress, isIndeterminate)
//            // 不确定进度时隐藏百分比文本
//            if (!isIndeterminate) {
//                setTextViewText(R.id.tv_percent, "${progress}%")
//                setViewVisibility(R.id.tv_percent, View.VISIBLE)
//            } else {
//                setViewVisibility(R.id.tv_percent, View.GONE)
//            }
//        }
//
//        return builder(title = title, text = if (isIndeterminate) "准备中..." else "$progress%", ongoing = ongoing)
//            .asDecoratedCustomView(contentView = remoteViews)
//            // 更新进度时不重复响铃/震动/闪烁
//            .setOnlyAlertOnce(true)
//            .build()
//    }

    /**
     * 创建通知栏 (更新使用相同 id)
     * @param id 推送 ID，相同会覆盖，不同则区分
     * 1) 0–99：前台服务/系统级固定通知（录屏、定位等），预留充足
     * 2) 100+：动态业务通知构建器自增区间 (从 100 自增到 Integer.MAX_VALUE，即使每秒推一条也要 68 年)
     * 3) 1000+：订单等业务实体 ID 直接作为通知 ID
     * 4) 如果未来有业务实体的 ID 可能小于 100（比如某些内部测试订单、配置项）
     *  // require 的语义是“条件为 true 时通过，为 false 时抛异常”
     *  fun showOrderNotification(orderId: Int, ...) {
     *    require(orderId >= 1000) { "业务通知ID不应占用系统通知区间" }
     *    // ...
     *  }
     */
    fun Notification?.notify(notifyId: Int) {
        this ?: return
        val notifyAction = {
            notificationManager?.notify(notifyId, this)
        }
        // 在主线程调用 notify（确保 UI 相关操作安全）
        if (!isMainThread) {
            notificationScope.launch {
                notifyAction()
            }
        } else {
            notifyAction()
        }
    }

    /**
     * 判断是否具备通知
     */
    fun Context?.hasNotificationPermission(): Boolean {
        this ?: return false
        // Android 13及以上需要检查POST_NOTIFICATIONS权限
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
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
     *   if (isGranted) {
     *      startRecording()
     *   } else {
     *     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
     *       mActivity.navigateToNotificationSettings()
     *     }
     *   }
     * }
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun ActivityResultLauncher<String>?.requestNotificationPermission() {
        this ?: return
        launch(Manifest.permission.POST_NOTIFICATIONS)
    }

}

/**
 * 通知弹框的 Dialog 要与页面强管理,不能使用 object
 * 可在基类中初始化
 */
class NotificationPermissionHelper(private val mActivity: FragmentActivity, wrapper: RequestPermissionRegistrar) {
    private val dialog by lazy { AppDialog(mActivity) }
    private var listener: (hasPermissions: Boolean) -> Unit = {}
    private val requestPermissionResult = wrapper.registerResult { isGranted ->
        if (isGranted) {
            listener.invoke(true)
        } else {
            dialog
                .setParams(string(R.string.hint), string(R.string.permissionNotification))
                .setDialogListener({
                    mActivity.pullUpNotification()
                }, {
                    listener.invoke(false)
                })
                .show()
        }
    }

    init {
        mActivity.doOnDestroy {
            requestPermissionResult.unregister()
        }
    }

    /**
     * 尝试拉起通知,如果未授予权限,回调监听里处理
     */
    fun pullUpNotification() {
        if (mActivity.hasNotificationPermission()) {
            listener.invoke(true)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionResult.requestNotificationPermission()
            } else {
                listener.invoke(true)
            }
        }
    }

    /**
     * 权限监听
     */
    fun setOnNotificationListener(listener: (hasPermissions: Boolean) -> Unit = {}) {
        this.listener = listener
    }

}