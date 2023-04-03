package com.example.multimedia.utils.helper

import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.TimerUtil
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.glide.ImageLoader
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager

/**
 * @description 播放器帮助类
 * @author yan
 */
class GSYVideoHelper(private val activity: FragmentActivity, layout: FrameLayout, fullScreen: Boolean = false, private val videoType: VideoType = VideoType.MOBILE) : LifecycleEventObserver {
    private var retryWithPlay = false
    private var player: StandardGSYVideoPlayer? = null
    private var orientationUtils: OrientationUtils? = null
    private val imgCover by lazy { ImageView(activity) }
    private val gSYSampleCallBack by lazy { object : GSYSampleCallBack() {
        override fun onQuitFullscreen(url: String, vararg objects: Any) {
            super.onQuitFullscreen(url, *objects)
            if (videoType == VideoType.PC) orientationUtils?.backToProtVideo()
        }

        override fun onPlayError(url: String?, vararg objects: Any?) {
            super.onPlayError(url, *objects)
            if (!retryWithPlay) {
                retryWithPlay = true
                player.disable()
                //允许硬件解码，装载IJK播放器内核
//                GSYVideoType.enableMediaCodec()
                GSYVideoType.enableMediaCodecTexture()
                PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
                CacheFactory.setCacheManager(ProxyCacheManager::class.java)
                TimerUtil.schedule({
                    player?.enable()
                    player?.startPlayLogic()
                })
            }
        }
    }}

    init {
        activity.lifecycle.addObserver(this)
        //将生成的播放器放入对应的容器中
        player = StandardGSYVideoPlayer(activity)
        layout.addView(player)
        //屏幕展示效果
        GSYVideoType.setShowType(if (videoType == VideoType.MOBILE && !fullScreen) GSYVideoType.SCREEN_MATCH_FULL else GSYVideoType.SCREEN_TYPE_DEFAULT)
        //设置底层渲染,关闭硬件解码
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
//        GSYVideoType.disableMediaCodec()
        GSYVideoType.disableMediaCodecTexture()
        //默认采用exo内核，播放报错则切ijk内核
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        imgCover.scaleType = if (videoType == VideoType.MOBILE && !fullScreen) ImageView.ScaleType.FIT_XY else ImageView.ScaleType.CENTER_CROP
        player?.titleTextView?.gone()
        player?.backButton?.gone()
        player?.thumbImageView = imgCover
        if (!fullScreen) {
            player?.fullscreenButton?.gone()
        } else {
            if (videoType == VideoType.PC) {
                //外部辅助的旋转，帮助全屏
                orientationUtils = OrientationUtils(activity, player)
                //初始化不打开外部的旋转
                orientationUtils?.isEnable = false
            }
            //直接横屏
            player?.fullscreenButton?.click {
                orientationUtils?.resolveByClick()
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                player?.startWindowFullscreen(activity, true, true)
            }
        }
    }

    /**
     * 设置播放路径
     */
    fun setUrl(url: String, autoPlay: Boolean = false) {
        retryWithPlay = false
        //加载图片
        ImageLoader.instance.displayCover(imgCover, url)
        if (videoType == VideoType.MOBILE) {
            GSYVideoOptionBuilder()
                .setIsTouchWiget(false)
                .setRotateViewAuto(false)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(false)
                .setUrl(url)
                .setCacheWithPlay(false)//禁用缓存，vivo手机出错
                .setVideoAllCallBack(gSYSampleCallBack).build(player)
        } else {
            GSYVideoOptionBuilder()
                .setIsTouchWiget(false)
                .setRotateViewAuto(false)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(false)
                .setUrl(url)
                .setCacheWithPlay(false)
                .setVideoAllCallBack(gSYSampleCallBack).build(player)
        }
        if (autoPlay) player?.startPlayLogic()
    }

    /**
     * 全屏时写，写在系统的onBackPressed之前
     */
    fun onBackPressed(): Boolean {
        orientationUtils?.backToProtVideo()
        return GSYVideoManager.backFromWindowFull(activity)
    }

    /**
     * 写在系统的onPause之前
     */
    private fun onPause() = player?.currentPlayer?.onVideoPause()

    /**
     * 写在系统的onResume之前
     */
    private fun onResume() = player?.currentPlayer?.onVideoResume(false)

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_DESTROY -> {
                onPause()
                player?.currentPlayer?.release()
                player?.release()
                orientationUtils?.releaseListener()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}

enum class VideoType {
    MOBILE, PC
}