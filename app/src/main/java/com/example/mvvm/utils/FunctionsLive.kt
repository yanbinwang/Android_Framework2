package com.example.mvvm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.removeSelf
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAParser.ParseCompletion
import com.opensource.svgaplayer.SVGAParser.PlayCallback
import com.opensource.svgaplayer.SVGAVideoEntity
import java.lang.ref.WeakReference
import java.net.URL

/**
 * CountdownView 倒计时视图库 -> 应用于限时促销、活动倒计时、游戏计时等场景
 * https://github.com/iwgang/CountdownView
 *
 * banner 轮播图库 -> 适用于广告展示、图片轮播、引导页等多种场景
 * https://github.com/youth5201314/banner
 *
 * RollingText -> 实现文本滚动效果的自定义控件，支持走马灯、跑马灯等动态文本展示效果
 * https://github.com/YvesCheung/RollingText
 * https://blog.csdn.net/mqdxiaoxiao/article/details/135101003
 *
 * DanmakuFlameMaster -> 支持多种弹幕效果和自定义配置，能够在应用中实现类似视频网站的弹幕功能。
 * 其核心优势在于流畅的渲染性能、灵活的自定义能力和丰富的交互支持，适用于视频播放、直播、互动界面等场景
 * https://github.com/ctiao/DanmakuFlameMaster
 *
 * RangeSeekBar -> 滑动选择条库，支持双滑块区间选择（Range Selection），可用于音量调节、价格筛选、进度范围设置等场景
 * https://github.com/Jay-Goo/RangeSeekBar
 *
 * realtimeblurview -> 为 Android 应用提供实时模糊背景效果的高性能控件，支持动态模糊、高斯模糊等效果，适配多种场景
 * https://github.com/mmin18/realtimeblurview
 *
 * EdgeTransparentView -> 实现任意 View 边沿渐变透明效果
 * https://gitee.com/tryohang/EdgeTranslucent
 *
 * SVGAPlayer -> 支持svg文件播放
 * https://github.com/svga/SVGAPlayer-Android/tree/2.5.6
 */

/**
 * 代码创建一个svg
 * @videoItem 使用parserSource解析
 * 解析资源，拿到 SVGAVideoEntity
 * context.parserSVGASource("https://xxx/anim.svga") { videoItem ->
 *     // 传入 createSVGAView 生成 SVGAImageView
 *     val svgaView = context.createSVGAView(videoItem)
 *     // 配置循环、回调、添加到布局
 *     svgaView?.loops = 0
 *     svgaView?.startAnimation()
 * }
 */
fun Context?.createSVGAView(item: SVGAVideoEntity?): SVGAImageView? {
    if (this == null || item == null) return null
    // 创建 SVGA 视图
    val view = SVGAImageView(this)
    // 再加载动画
    view.setVideoItem(item)
    // 返回本体
    return view
}

/**
 * 销毁svg
 */
fun SVGAImageView?.releaseSVGAView() {
    this ?: return
    stopAnimation(true)
    removeSelf()
}

/**
 * 用代码手动加载 SVGA，不依赖 XML 自动解析
 * // 加载 assets
 * context.parserSource("gift_rocket.svga") { videoEntity ->
 *     svgaView.setVideoItem(videoEntity)
 *     svgaView.startAnimation()
 * }
 *
 * // 加载网络（不用改任何代码）
 * context.parserSource("https://xxx.svga") { videoEntity ->
 *     svgaView.setVideoItem(videoEntity)
 *     svgaView.startAnimation()
 * }
 *
 * // 设置只播放1次，不循环
 * svgaView.loops = 1
 *
 * // 播放完成隐藏
 * svgaView.callback = object : SVGACallback {
 *     override fun onFinished() {
 *         visibility = View.GONE
 *     }
 * }
 *
 * // xml中无需写代码解析
 * <com.opensource.svgaplayer.SVGAImageView
 *     android:id="@+id/svga_gift"
 *     android:layout_width="120dp"
 *     android:layout_height="120dp
 *     app:source="posche.svga""
 *     app:autoPlay="true"/>
 */
fun Context?.parserSVGASource(source: String, callback: ParseCompletion? = null, playCallback: PlayCallback? = null) {
    this ?: return
    // 获取一个全局共用的 SVGA 解析器实例（单例）
    val parser = SVGAParser.shareParser()
    parser.init(this)
    if (source.startsWith("http://") || source.startsWith("https://")) {
        parser.decodeFromURL(URL(source), callback, playCallback)
    } else {
        parser.decodeFromAssets(source, callback, playCallback)
    }
}

/**
 * ExoPlayer -> 创建
 * 1) 代码创建播放器
 * val exoPlayer = ExoPlayer.Builder(this).build()
 * playerView.player = exoPlayer // 绑定到 PlayerView（XML 或代码创建的都行）
 * 2) xml绘制或代码创建
 * <com.google.android.exoplayer2.ui.PlayerView
 *     android:id="@+id/videoView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:use_controller="false"  <!-- 隐藏播放条 -->
 *     app:resize_mode="fill" />    <!-- 铺满屏幕 -->
 */
fun Context?.createPlayerView(exoPlayer: ExoPlayer?): PlayerView? {
    this ?: return null
    val playerView = PlayerView(this)
    playerView.bindExoPlayer(exoPlayer)
    return playerView
}

/**
 * 建立绑定关系
 */
@SuppressLint("UnsafeOptInUsageError")
fun PlayerView?.bindExoPlayer(exoPlayer: ExoPlayer?) {
    if (null == this || null == exoPlayer) return
    // 等价 app:use_controller="false"
    useController = false
    // 等价 app:resize_mode="fill"
    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
    // 布局参数（全屏）
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    // 绑定
    player = exoPlayer
}

/**
 * 创建 PlayerView + ExoPlayer，并插入容器
 * @return 返回 ExoPlayer 引用，供调用方监听和播放
 *
 * // 一行创建 + 插入容器，同时拿到两个引用
 * val (playerView, exoPlayer) = context.createAndAddPlayer(container)  ?: return
 *
 * // ExoPlayer 引用在手，播放、监听随意
 * exoPlayer.playExoPlayer(owner, "intro.mp4", autoReleaseOnEnd = false) {
 *     // 片头播完 → 切直播流 / 显示UI
 * }
 *
 * // 销毁时各司其职
 * playerView.releasePlayerView()   // View层：断开引用 + 移除
 * exoPlayer.releasePlayer()        // Player层：stop + release
 */
fun Context?.createAndAddPlayer(container: ViewGroup): Pair<PlayerView, ExoPlayer>? {
    this ?: return null
    val exoPlayer = ExoPlayer.Builder(this).build()
    val playerView = PlayerView(this).apply { bindExoPlayer(exoPlayer) }
    container.addView(playerView)
    return playerView to exoPlayer
}

/**
 * 销毁 PlayerView
 */
fun PlayerView?.releasePlayerView() {
    this ?: return
    // 断开播放器引用（最关键）
    player = null
    // 从父布局移除
    removeSelf()
}

/**
 * 播放视频（片头/广告等）
 * @param autoReleaseOnEnd 片头场景传 false，纯短视频场景传 true
 */
private var currentOwnerRef: WeakReference<LifecycleOwner>? = null
private var currentListener: Player.Listener? = null

fun ExoPlayer?.playExoPlayer(owner: LifecycleOwner, mp4Path: String, autoReleaseOnEnd: Boolean = true, onEnd: () -> Unit = {}) {
    this ?: return
    // 清理上一个页面的绑定和监听
    clearCurrentBinding(this)
    // 加载资源
    val uri = if (mp4Path.startsWith("http")) mp4Path.toUri() else "asset:///$mp4Path".toUri()
    setMediaItem(MediaItem.fromUri(uri))
    prepare()
    play()
    // 创建并绑定新监听
    val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                // 先解绑，防止后续状态变化误触发
                clearCurrentBinding(this@playExoPlayer)
                // 再回调业务层（此时绑定已清理）
                onEnd()
                // 最后按需释放播放器（通知业务层：片头播完了，该切直播流了）
                if (autoReleaseOnEnd) {
                    releasePlayer()
                }
            }
        }
    }
    addListener(listener)
    currentListener = listener
    currentOwnerRef = WeakReference(owner)
    // 绑定生命周期销毁监听（仅当本次是新 owner 时）
    owner.doOnDestroy {
        // 只有当前绑定的还是这个 owner 才清理
        if (currentOwnerRef?.get() === owner) {
            clearCurrentBinding(this)
            if (autoReleaseOnEnd) {
                releasePlayer()
            }
        }
    }
}

/**
 * 清理当前绑定（内部使用）
 */
private fun clearCurrentBinding(player: ExoPlayer) {
    currentListener?.let { player.removeListener(it) }
    currentListener = null
    currentOwnerRef = null
}

/**
 * 销毁播放器
 */
fun ExoPlayer?.releasePlayer() {
    this ?: return
    // 停止播放
    stop()
    // 清理绑定
    clearCurrentBinding(this)
    // 清空播放源
    setMediaItems(emptyList())
    // 销毁播放器
    release()
}