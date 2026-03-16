package com.example.mvvm.utils

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAParser.ParseCompletion
import com.opensource.svgaplayer.SVGAParser.PlayCallback
import com.opensource.svgaplayer.SVGAVideoEntity
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
 */
fun Context?.createSVGAView(videoItem: SVGAVideoEntity?): SVGAImageView? {
    this ?: return null
    // 创建 SVGA 视图
    val svgaView = SVGAImageView(this)
    // 立刻初始化全局解析器
    SVGAParser.shareParser().init(this)
    // 再加载动画
    svgaView.setVideoItem(videoItem)
    // 返回本体
    return svgaView
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
fun Context?.parserSource(source: String, callback: ParseCompletion? = null, playCallback: PlayCallback? = null) {
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
 * 播放 MP4
 * <com.google.android.exoplayer2.ui.PlayerView
 *     android:id="@+id/videoView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:use_controller="false"  <!-- 关键：隐藏播放条 -->
 *     app:resize_mode="fill" />    <!-- 铺满屏幕 -->
 */
fun Context?.createExoPlayer(mp4Path: String): ExoPlayer? {
    this ?: return null
    // 创建播放器
    val exoPlayer = ExoPlayer.Builder(this).build()
    // 设置视频（本地assets / 网络链接都支持）
    val uri = if (mp4Path.startsWith("http")) {
        // 网络链接 → 直接用
        mp4Path.toUri()
    } else {
        // 本地 assets → 加 asset:///
        "asset:///$mp4Path".toUri()
    }
    val mediaItem = MediaItem.fromUri(uri)
    exoPlayer?.setMediaItem(mediaItem)
//    // 3. 准备 + 播放
//    exoPlayer?.prepare()
//    exoPlayer?.play()
//    // 4. 播放完隐藏/回收
//    exoPlayer?.addListener(object : Player.Listener {
//        override fun onPlaybackStateChanged(state: Int) {
//            if (state == Player.STATE_ENDED) {
//                videoView.visibility = View.GONE
//                releasePlayer() // 必须释放
//            }
//        }
//    })
//    private fun releasePlayer() {
//        exoPlayer?.release()
//        exoPlayer = null
//    }
    return exoPlayer
}