package com.example.thirdparty.media.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.function.color
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.setStatusBarLightMode
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.framework.utils.logWTF
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
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoControlView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager
import java.lang.reflect.Constructor

/**
 * @description 播放器帮助类
 * @author yan
 *
 * https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md
 *
 * 使用FragmentManager来管理fragment的时候，其如果继承BaseLazyFragment，此时页面判断lifecycle的生命周期是没有意义的
 * 因为重写了onHiddenChanged，并且其添加了FragmentOwner注解，碰到此类需要有视频播放的情况，不传activity，手动在对应生命周期内写播放器生命周期管控
 *
 * <activity
 *     android:name=".xxxxx"
 *     android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
 *     android:screenOrientation="portrait" />
 */
class GSYVideoHelper(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private var isPause = false
    private var isPlay = false
    private var retryWithPlay = false
    private var player: StandardGSYVideoPlayer? = null
    private var orientationUtils: OrientationUtils? = null
    private var restartJob: Job? = null
    private val mBinding by lazy { ViewGsyvideoThumbBinding.bind(mActivity.inflate(R.layout.view_gsyvideo_thumb)) }
    private val mGSYSampleCallBack by lazy { object : GSYSampleCallBack() {
        override fun onPrepared(url: String?, vararg objects: Any?) {
            super.onPrepared(url, *objects)
            // 开始播放了才能旋转和全屏
            isPlay = true
            orientationUtils?.isEnable = true
        }

        override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
            super.onEnterFullscreen(url, *objects)
            // 进入全屏,拿取此时的播放器
            val gsy = objects[1] as? GSYBaseVideoPlayer
//            // 如果是垂直全屏
//            val statusBarHeight = getStatusBarHeight()
//            val topContainer = getTopContainer(gsy as? GSYVideoControlView)
//            topContainer.doOnceAfterLayout {
//                if (orientationUtils?.screenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//                    it.setBackgroundColor(color(R.color.bottom_container_bg))
//                    it.size(height = it.measuredHeight + statusBarHeight)
//                    it.padding(top = statusBarHeight)
//                } else {
//                    it.padding(top = 0)
//                }
//            }
            // 当视图被添加到窗口时回调
            val parentView = gsy?.parent as? FrameLayout
            // 如果是垂直全屏
            if (orientationUtils?.screenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                parentView.padding(top = getStatusBarHeight())
                // 获取Window对象
                CommonUtil.getActivityNestWrapper(parentView?.context).window?.apply {
                    // 清除全屏标志（让状态栏显示），保留导航栏隐藏状态
                    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    // 控制系统UI可见性，只显示状态栏，隐藏导航栏
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Android 11+（API 30+）使用WindowInsetsController
                        insetsController?.apply {
                            // 显示状态栏
                            show(WindowInsets.Type.statusBars())
                            // 隐藏导航栏
                            hide(WindowInsets.Type.navigationBars())
                            // 设置状态栏文字样式（0表示默认浅色文字，适用于深色背景）
                            setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                        }
                    } else {
                        // Android 6.0-10（API 23-29）使用systemUiVisibility
                        var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // 隐藏导航栏,沉浸式（下拉时临时显示）
                        // 清除状态栏隐藏标志（确保状态栏显示）
                        flags = flags and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
                        // 设置状态栏文字样式
                        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() // 浅色文字
                        // flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 深色文字
                        decorView.systemUiVisibility = flags
                    }
                }
            } else {
                parentView.padding(top = 0)
            }
        }

        override fun onQuitFullscreen(url: String, vararg objects: Any) {
            super.onQuitFullscreen(url, *objects)
            orientationUtils?.backToProtVideo()
            mActivity.window?.setStatusBarLightMode(true)
        }

        override fun onPlayError(url: String?, vararg objects: Any?) {
            super.onPlayError(url, *objects)
            if (!retryWithPlay) {
                retryWithPlay = true
                player.disable()
                restartJob?.cancel()
                restartJob = mActivity.lifecycleScope.launch {
                    // 允许硬件解码，装载IJK播放器内核
//                GSYVideoType.enableMediaCodec()
                    GSYVideoType.enableMediaCodecTexture()
                    PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
                    CacheFactory.setCacheManager(ProxyCacheManager::class.java)
                    delay(1000)
                    player.enable()
                    player?.startPlayLogic()
                }
            }
        }
    }}

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 绑定页面-设置基础配资
     */
    fun bind(standardGSYVideoPlayer: StandardGSYVideoPlayer?, fullScreen: Boolean = false) {
        player = standardGSYVideoPlayer
        // 屏幕展示效果 -> 采用基础配资
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        // 设置底层渲染,关闭硬件解码
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
//        GSYVideoType.disableMediaCodec()
        GSYVideoType.disableMediaCodecTexture()
        // 默认采用exo内核，播放报错则切ijk内核
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        // 配置适配遮罩，隐藏默认的顶部菜单的返回/标题
        player?.thumbImageView = mBinding.root
        player?.titleTextView?.gone()
        player?.backButton?.gone()
        if (!fullScreen) {
            player?.fullscreenButton?.gone()
        } else {
            // 外部辅助的旋转，帮助全屏
            orientationUtils = OrientationUtils(mActivity, player)
            // 初始化不打开外部的旋转
            orientationUtils?.isEnable = false
            // 直接横屏
            player?.fullscreenButton?.click {
                orientationUtils?.resolveByClick()
                // 第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                player?.startWindowFullscreen(player?.context, true, true)
//                val gsy = player?.startWindowFullscreen(player?.context, true, true)
//                // 当视图被添加到窗口时回调
//                val parentView = gsy?.parent as? FrameLayout
//                // 如果是垂直全屏
//                if (orientationUtils?.screenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//                    parentView.padding(top = getStatusBarHeight())
//                } else {
//                    parentView.padding(top = 0)
//                }
//                val statusBarHeight = getStatusBarHeight()
//                val topContainer = getTopContainer(gsy as? GSYVideoControlView)
//                topContainer.doOnceAfterLayout {
//                    val measureHeight = it.measuredHeight
//                    if (orientationUtils?.screenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//                        it.size(height = measureHeight + statusBarHeight)
//                        it.padding(top = statusBarHeight)
//                    } else {
//                        it.size(height = measureHeight)
//                        it.padding(top = 0)
//                    }
//                }
            }
        }
    }

    private fun getTopContainer(controlView: GSYVideoControlView?): ViewGroup? {
        return try {
            // 获取GSYVideoControlView类的Class对象
            val clazz = GSYVideoControlView::class.java
            // 获取名为"mTopContainer"的字段
            val field = clazz.getDeclaredField("mTopContainer")
            // 设置可访问，允许访问受保护的字段
            field.isAccessible = true
            // 获取字段值并转换为ViewGroup类型
            field.get(controlView) as? ViewGroup
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 设置播放路径，缩略图，是否自动开始播放
     */
    fun setUrl(url: String, thumbUrl: String? = null, setUpLazy: Boolean = false) {
        // 加载图片
        if (thumbUrl.isNullOrEmpty()) {
            ImageLoader.instance.loadVideoFrameFromUrl(mBinding.ivThumb, url)
        } else {
            ImageLoader.instance.loadImageFromUrl(mBinding.ivThumb, thumbUrl)
        }
        GSYVideoOptionBuilder()
            // 是否可以滑动界面改变进度，声音等 默认true
            .setIsTouchWiget(false)
            // 是否开启自动旋转
            .setRotateViewAuto(false)
            // 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
            .setAutoFullWithSize(true)
            // 是否使用全屏动画效果
            .setShowFullAnimation(false)
            // 是否需要显示流量提示,默认true
            .setNeedShowWifiTip(false)
            // 是否需要全屏锁定屏幕功能 如果单独使用请设置setIfCurrentIsFullscreen为true
            .setNeedLockFull(false)
//            .setSetUpLazy(setUpLazy)
            // 播放url
            .setUrl(url)
            // 是否边缓存，m3u8等无效
            .setCacheWithPlay(false)
            // 设置播放过程中的回调
            .setVideoAllCallBack(mGSYSampleCallBack)
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
        orientationUtils?.releaseListener()
        player?.currentPlayer?.release()
        player?.release()
        player = null
        mActivity.lifecycle.removeObserver(this)
    }

}