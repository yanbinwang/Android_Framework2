package com.example.gsyvideoplayer.utils

import android.content.res.Configuration
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
import com.example.gsyvideoplayer.R
import com.example.gsyvideoplayer.databinding.ViewGsyvideoThumbBinding
import com.example.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager
import kotlin.coroutines.CoroutineContext

/**
 * @description 播放器帮助类
 * @author yan
 *
 * https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md
 *
 * 使用FragmentManager来管理fragment的时候，其如果继承BaseLazyFragment，此时页面判断lifecycle的生命周期是没有意义的
 * 因为重写了onHiddenChanged，并且其添加了FragmentOwner注解，碰到此类需要有视频播放的情况，不传activity
 *
 * <activity
 *     android:name=".xxxxx"
 *     android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
 *     android:screenOrientation="portrait" />
 */
class GSYVideoHelper(private val mActivity: FragmentActivity? = null) : CoroutineScope, LifecycleEventObserver {
    private var isPause = false
    private var isPlay = false
    private var retryWithPlay = false
    private var restartJob: Job? = null
    private var player: StandardGSYVideoPlayer? = null
    private var orientationUtils: OrientationUtils? = null
    private val mBinding by lazy { mActivity?.inflate(R.layout.view_gsyvideo_thumb)?.let { ViewGsyvideoThumbBinding.bind(it) } }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        mActivity?.lifecycle?.addObserver(this)
    }

    /**
     * 绑定页面-设置基础配资
     */
    fun bind(player: StandardGSYVideoPlayer?, fullScreen: Boolean = false) {
        this.player = player
        //屏幕展示效果->采用基础配资
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        //设置底层渲染,关闭硬件解码
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
//        GSYVideoType.disableMediaCodec()
        GSYVideoType.disableMediaCodecTexture()
        //默认采用exo内核，播放报错则切ijk内核
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        //配置适配遮罩，隐藏默认的顶部菜单的返回/标题
        player?.thumbImageView = mBinding?.root
        player?.titleTextView?.gone()
        player?.backButton?.gone()
        if (!fullScreen) {
            player?.fullscreenButton?.gone()
        } else {
            //外部辅助的旋转，帮助全屏
            orientationUtils = OrientationUtils(mActivity, player)
            //初始化不打开外部的旋转
            orientationUtils?.isEnable = false
            //直接横屏
            player?.fullscreenButton?.click {
                orientationUtils?.resolveByClick()
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                player.startWindowFullscreen(player.context, true, true)
            }
        }
    }

    /**
     * 设置播放路径，缩略图，是否自动开始播放
     */
    fun setUrl(url: String, thumbUrl: String? = null, setUpLazy: Boolean = false) {
        //加载图片
        if (thumbUrl.isNullOrEmpty()) {
            ImageLoader.instance.displayFrame(mBinding?.ivThumb, url)
        } else {
            ImageLoader.instance.display(mBinding?.ivThumb, thumbUrl)
        }
        GSYVideoOptionBuilder()
            .setIsTouchWiget(false)
            .setRotateViewAuto(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(false)
//            .setSetUpLazy(setUpLazy)
            .setUrl(url)
            .setCacheWithPlay(false)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onPrepared(url: String?, vararg objects: Any?) {
                    super.onPrepared(url, *objects)
                    //开始播放了才能旋转和全屏
                    isPlay = true
                    orientationUtils?.setEnable(true)
                }

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
                        restartJob?.cancel()
                        restartJob = launch {
                            delay(1000)
                            player.enable()
                            player?.startPlayLogic()
                        }
                    }
                }
            })
//            .setLockClickListener { _, lock ->
//                //配合下方的onConfigurationChanged
//                orientationUtils?.setEnable(!lock)
//            }
            .build(player)
        if (setUpLazy) start()
    }

    /**
     * 全屏时写，写在系统的onBackPressed之前
     *  @Override
     *  public void onBackPressed() {
     *     if (GSYVideoManager.backFromWindowFull(this)) {
     *         return;
     *     }
     *     super.onBackPressed();
     *  }
     */
    fun onBackPressed(): Boolean {
        orientationUtils?.backToProtVideo()
        return GSYVideoManager.backFromWindowFull(mActivity)
    }

    /**
     * 如果旋转了就全屏
     * @Override
     * public void onConfigurationChanged(Configuration newConfig) {
     *     super.onConfigurationChanged(newConfig);
     *     if (isPlay && !isPause) {
     *         detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
     *     }
     * }
     */
    fun onConfigurationChanged(newConfig: Configuration) {
        if (isPlay && !isPause) player?.onConfigurationChanged(mActivity, newConfig, orientationUtils, true, true)
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> resume()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> destroy()
            else -> {}
        }
    }

    /**
     * 播放-默认一次切内核的重试机会
     */
    fun start() {
        isPlay = false
        retryWithPlay = false
        player?.startPlayLogic()
    }

    /**
     * 暂停
     */
    fun pause() {
        isPause = true
        player?.currentPlayer?.onVideoPause()
    }

    /**
     * 加载
     */
    fun resume() {
        isPause = false
        player?.currentPlayer?.onVideoResume(false)
    }

    /**
     * 销毁
     */
    fun destroy() {
        restartJob?.cancel()
        job.cancel()
        orientationUtils?.releaseListener()
        player?.currentPlayer?.release()
        player?.release()
        player = null
        mActivity?.lifecycle?.removeObserver(this)
    }

}