package com.example.thirdparty.media.utils

import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.function.color
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
    private var lastVisible = true
    private var retryWithPlay = false
    private var toggleJob: Job? = null
    private var restartJob: Job? = null
    private var player: StandardGSYVideoPlayer? = null
    private var orientationUtils: OrientationUtils? = null
    private var onQuitFullscreenListener: (() -> Unit)? = null
    private var onPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null
    private val mBinding by lazy { ViewGsyvideoThumbBinding.bind(mActivity.inflate(R.layout.view_gsyvideo_thumb)) }
    private val mGSYSampleCallBack by lazy { object : GSYSampleCallBack() {
        override fun onStartPrepared(url: String?, vararg objects: Any?) {
            super.onStartPrepared(url, *objects)
            player?.fullscreenButton?.disable()
        }

        override fun onPrepared(url: String?, vararg objects: Any?) {
            super.onPrepared(url, *objects)
            // 开始播放了才能旋转和全屏
            isPlay = true
            orientationUtils?.isEnable = true
            player?.fullscreenButton?.enable()
        }

        override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
            super.onEnterFullscreen(url, *objects)
            // 进入全屏,拿取此时的播放器
            val gsy = objects[1] as? GSYBaseVideoPlayer
//            // 当前播放器的父容器
//            val parentView = gsy?.parent as? FrameLayout
            // 通过播放器自带的工具类获取到当前的window对象
            val window = CommonUtil.getActivityNestWrapper(gsy?.context).window
            // 拿取到准确的状态栏高度(横竖屏高度是不一致的)
            val insets = ViewCompat.getRootWindowInsets(window.decorView)
            val statusBarHeight = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top.orZero
            // 默认全屏窗体的状态
            controllerToggle(window, true)
            window.setStatusBarLightMode(false) // 可能部分机型会有问题,不过基本是兼容的
            // 拿取播放器的顶部菜单,空出状态栏的高度距离
            val topContainer = getTopContainer(gsy as? GSYVideoControlView) as? LinearLayout
            topContainer.doOnceAfterLayout {
                it.setBackgroundColor(color(R.color.bottom_container_bg))
                it.size(height = it.measuredHeight + statusBarHeight)
                it.padding(top = statusBarHeight)
                // 监听子View的布局变化
                onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
                    checkVisibilityChange(window, it)
                    true
                }
                it.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
                // 监听子View的可见性变化（通过OnAttachStateChangeListener）
                it.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        checkVisibilityChange(window, it)
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        checkVisibilityChange(window, it)
                    }
                })
            }
        }

        override fun onQuitFullscreen(url: String, vararg objects: Any) {
            super.onQuitFullscreen(url, *objects)
            orientationUtils?.backToProtVideo()
            onQuitFullscreenListener?.invoke()
        }

        override fun onPlayError(url: String?, vararg objects: Any?) {
            super.onPlayError(url, *objects)
            if (!retryWithPlay) {
                retryWithPlay = true
                player.disable()
                restartJob?.cancel()
                restartJob = mActivity.lifecycleScope.launch {
                    // 允许硬件解码，装载IJK播放器内核
//                    GSYVideoType.enableMediaCodec()
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

    private fun checkVisibilityChange(window: Window, childView: View?) {
        childView ?: return
        val isVisible = childView.isVisible
        if (isVisible != lastVisible) {
            lastVisible = isVisible
            if (!isVisible) {
                controllerToggle(window, false)
            } else {
                controllerToggle(window, true)
            }
        }
    }

    private fun controllerToggle(window: Window, isShow: Boolean) {
        toggleJob?.cancel()
        toggleJob = mActivity.lifecycleScope.launch {
            delay(500)
            window.apply {
                // 清除全屏标志（让状态栏显示），保留导航栏隐藏状态
                if (isShow) {
                    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                } else {
                    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
                // 控制系统UI可见性，只显示状态栏，隐藏导航栏
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+（API 30+）使用WindowInsetsController
                    insetsController?.apply {
                        val statusBars = WindowInsets.Type.statusBars()
                        val navigationBars = WindowInsets.Type.navigationBars()
                        if (isShow) {
                            // 显示状态栏/导航栏
                            show(statusBars)
                            hide(navigationBars)
                        } else {
                            // 隐藏状态栏/导航栏
                            hide(statusBars)
                            hide(navigationBars)
                        }
                        systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    // Android 6.0-10（API 23-29）使用systemUiVisibility
                    var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    if (isShow) {
                        // 显示状态栏，清除全屏标志
                        flags = flags and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
                    } else {
                        // 隐藏状态栏，添加全屏标志
                        flags = flags or View.SYSTEM_UI_FLAG_FULLSCREEN
                        // 清除状态栏文字样式标志
                        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                    decorView.systemUiVisibility = flags
                }
            }
        }
    }

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
            }
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
//        // 读取原视频方向角
//        val videoInfo = getVideoOrientationAndRotation(url)
//        val videoOrientation = videoInfo[0] // 视频方向
////            val needRotation = videoInfo[1] // 播放器需旋转的角度
//        if (videoOrientation == ORIENTATION_LANDSCAPE) {
//            lockLand = true
//        }
        // 构建配置
        GSYVideoOptionBuilder()
            // 是否可以滑动界面改变进度，声音等 默认true
            .setIsTouchWiget(false)
            // 是否开启自动旋转
            .setRotateViewAuto(false)
//            .setLockClickListener { _, lock ->
//                //配合下方的onConfigurationChanged
//                orientationUtils?.setEnable(!lock)
//            }
//            // 一全屏就锁屏横屏，默认false竖屏，可配合setRotateViewAuto使用
//            .setLockLand(false)
            // 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效**
            .setAutoFullWithSize(true)
            // 是否使用全屏动画效果
            .setShowFullAnimation(false)
            // 是否需要显示流量提示,默认true
            .setNeedShowWifiTip(false)
            // 是否需要全屏锁定屏幕功能 如果单独使用请设置setIfCurrentIsFullscreen为true
            .setNeedLockFull(false)
            // 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
            .setAutoFullWithSize(true)
            // 播放url
//            .setSetUpLazy(setUpLazy)
            .setUrl(url)
            // 是否边缓存，m3u8等无效
            .setCacheWithPlay(false)
            // 设置播放过程中的回调
            .setVideoAllCallBack(mGSYSampleCallBack)
            // 开启构建,绑定配置
            .build(player)
        // 如果需要自动播放,此时开启
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
        clearOnQuitFullscreenListener()
        orientationUtils?.releaseListener()
        player?.currentPlayer?.release()
        player?.release()
        player = null
        toggleJob?.cancel()
        restartJob?.cancel()
        mActivity.lifecycle.removeObserver(this)
    }

    fun setOnQuitFullscreenListener(onQuitFullscreenListener: () -> Unit) {
        this.onQuitFullscreenListener = onQuitFullscreenListener
    }

    fun clearOnQuitFullscreenListener() {
        onQuitFullscreenListener = null
    }

}