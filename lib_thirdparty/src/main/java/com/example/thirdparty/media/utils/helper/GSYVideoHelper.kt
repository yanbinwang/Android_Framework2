package com.example.thirdparty.media.utils.helper

import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.glide.ImageLoader
import com.example.thirdparty.R
import com.example.thirdparty.databinding.ViewGsyvideoThumbBinding
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager

/**
 * @description 播放器帮助类
 * @author yan
 */
class GSYVideoHelper(private val observer: LifecycleOwner) : LifecycleEventObserver {
    private var retryWithPlay = false
    private var job: Job? = null
    private var context: Context? = null
    private var binding: ViewGsyvideoThumbBinding? = null
    private var player: StandardGSYVideoPlayer? = null
    private var orientationUtils: OrientationUtils? = null

    init {
        observer.lifecycle.addObserver(this)
    }

    /**
     * 绑定页面
     */
    fun bind(activity: FragmentActivity, player: StandardGSYVideoPlayer, fullScreen: Boolean = false) {
        this.context = activity
        this.player = player
        this.binding = ViewGsyvideoThumbBinding.bind(activity.inflate(R.layout.view_gsyvideo_thumb))
        //屏幕展示效果
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        //设置底层渲染,关闭硬件解码
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
//        GSYVideoType.disableMediaCodec()
        GSYVideoType.disableMediaCodecTexture()
        //默认采用exo内核，播放报错则切ijk内核
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        player.titleTextView?.gone()
        player.backButton?.gone()
        player.thumbImageView = binding?.root
        if (!fullScreen) {
            player.fullscreenButton?.gone()
        } else {
            //外部辅助的旋转，帮助全屏
            orientationUtils = OrientationUtils(activity, player)
            //初始化不打开外部的旋转
            orientationUtils?.isEnable = false
            //直接横屏
            player.fullscreenButton?.click {
                orientationUtils?.resolveByClick()
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                player.startWindowFullscreen(activity, true, true)
            }
        }
    }

    /**
     * 设置播放路径
     */
    fun setUrl(url: String, autoPlay: Boolean = false) {
        retryWithPlay = false
        //加载图片
        ImageLoader.instance.displayFrame(binding?.ivThumb ?: ImageView(context), url)
        GSYVideoOptionBuilder()
            .setIsTouchWiget(false)
            .setRotateViewAuto(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(false)
            .setUrl(url)
            .setCacheWithPlay(false)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onQuitFullscreen(url: String, vararg objects: Any) {
                    super.onQuitFullscreen(url, *objects)
                    orientationUtils?.backToProtVideo()
                }

                override fun onPlayError(url: String?, vararg objects: Any?) {
                    super.onPlayError(url, *objects)
                    if (!retryWithPlay) {
                        retryWithPlay = true
                        player.disable()
                        //允许硬件解码，装载IJK播放器内核
//                        GSYVideoType.enableMediaCodec()
                        GSYVideoType.enableMediaCodecTexture()
                        PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
                        CacheFactory.setCacheManager(ProxyCacheManager::class.java)
                        job?.cancel()
                        job = GlobalScope.launch {
                            delay(1000)
                            player.enable()
                            player?.startPlayLogic()
                        }
                    }
                }
            }).build(player)
        if (autoPlay) player?.startPlayLogic()
    }

    /**
     * 全屏时写，写在系统的onBackPressed之前
     */
    fun onBackPressed(): Boolean {
        orientationUtils?.backToProtVideo()
        return GSYVideoManager.backFromWindowFull(context)
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> resume()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> {
                pause()
                job?.cancel()
                orientationUtils?.releaseListener()
                player?.currentPlayer?.release()
                player?.release()
                player = null
                observer.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    /**
     * 写在系统的onPause之前
     */
    private fun pause() = player?.currentPlayer?.onVideoPause()

    /**
     * 写在系统的onResume之前
     */
    private fun resume() = player?.currentPlayer?.onVideoResume(false)

}