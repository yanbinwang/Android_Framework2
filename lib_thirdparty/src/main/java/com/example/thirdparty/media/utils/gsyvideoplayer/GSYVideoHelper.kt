package com.example.thirdparty.media.utils.gsyvideoplayer

import android.content.res.Configuration
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
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
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setBitmap
import com.example.framework.utils.function.view.size
import com.example.glide.ImageLoader
import com.example.glide.ImageLoader.Companion.DEFAULT_MASK_RESOURCE
import com.example.glide.ImageLoader.Companion.DEFAULT_RESOURCE
import com.example.thirdparty.R
import com.example.thirdparty.databinding.ViewGsyvideoThumbBinding
import com.example.thirdparty.media.utils.suspendingThumbnail
import com.gyf.immersionbar.ImmersionBar
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * @description 播放器帮助类
 * @author yan
 * https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md
 * 1) 使用FragmentManager来管理fragment的时候，其如果继承BaseLazyFragment，此时页面判断lifecycle的生命周期是没有意义的
 * 2) 因为重写了onHiddenChanged，并且其添加了FragmentOwner注解，碰到此类需要有视频播放的情况，不传activity，手动在对应生命周期内写播放器生命周期管控
 * <activity
 *     android:name=".xxxxx"
 *     android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
 *     android:screenOrientation="portrait" />
 */
class GSYVideoHelper(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    // 播放/UI状态
    private var isPause = false
    private var isPrepared = false
    private var retryWithPlay = false
    private var lastVisible = true
    // 内部事务
    private var thumbJob: Job? = null
    private var toggleJob: Job? = null
    private var restartJob: Job? = null
    // UI/操作监听
    private var player: StandardGSYVideoPlayer? = null
    private var topContainer: LinearLayout? = null
    private var orientationUtils: OrientationUtils? = null
    private var onGSYVideoPlayerListener: OnGSYVideoPlayerListener? = null
    private var onPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null
    // 辅助工具类
    private val immersionBar by lazy { ImmersionBar.with(mActivity) }
    private val binding by lazy { ViewGsyvideoThumbBinding.bind(mActivity.inflate(R.layout.view_gsyvideo_thumb)) }
    private val gsySampleCallBack by lazy {
        object : GSYSampleCallBack() {
            override fun onStartPrepared(url: String?, vararg objects: Any?) {
                super.onStartPrepared(url, *objects)
                player?.fullscreenButton?.disable()
                onGSYVideoPlayerListener?.onStartPrepared(url, objects)
            }

            override fun onPrepared(url: String?, vararg objects: Any?) {
                super.onPrepared(url, *objects)
                // 开始播放了才能旋转和全屏
                isPrepared = true
                orientationUtils?.isEnable = true
                player?.fullscreenButton?.enable()
                onGSYVideoPlayerListener?.onPrepared(url, objects)
            }

            override fun onClickStartIcon(url: String?, vararg objects: Any?) {
                super.onClickStartIcon(url, *objects)
                onGSYVideoPlayerListener?.onClickStartIcon(url, objects)
            }

            override fun onClickStartError(url: String?, vararg objects: Any?) {
                super.onClickStartError(url, *objects)
                onGSYVideoPlayerListener?.onClickStartError(url, objects)
            }

            override fun onClickStop(url: String?, vararg objects: Any?) {
                super.onClickStop(url, *objects)
                onGSYVideoPlayerListener?.onClickStop(url, objects)
            }

            override fun onClickStopFullscreen(url: String?, vararg objects: Any?) {
                super.onClickStopFullscreen(url, *objects)
                onGSYVideoPlayerListener?.onClickSeekbarFullscreen(url, objects)
            }

            override fun onClickResume(url: String?, vararg objects: Any?) {
                super.onClickResume(url, *objects)
                onGSYVideoPlayerListener?.onClickResume(url, objects)
            }

            override fun onClickResumeFullscreen(url: String?, vararg objects: Any?) {
                super.onClickResumeFullscreen(url, *objects)
                onGSYVideoPlayerListener?.onClickResumeFullscreen(url, objects)
            }

            override fun onClickSeekbar(url: String?, vararg objects: Any?) {
                super.onClickSeekbar(url, *objects)
                onGSYVideoPlayerListener?.onClickSeekbar(url, objects)
            }

            override fun onClickSeekbarFullscreen(url: String?, vararg objects: Any?) {
                super.onClickSeekbarFullscreen(url, *objects)
                onGSYVideoPlayerListener?.onClickSeekbarFullscreen(url, objects)
            }

            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                super.onAutoComplete(url, *objects)
                onGSYVideoPlayerListener?.onAutoComplete(url, objects)
            }

            override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                super.onEnterFullscreen(url, *objects)
                // 进入全屏,拿取此时的播放器
                val gsy = objects[1] as? GSYBaseVideoPlayer
//                // 当前播放器的父容器
//                val parentView = gsy?.parent as? FrameLayout
                // 通过播放器自带的工具类获取到当前的window对象
                val window = CommonUtil.getActivityNestWrapper(gsy?.context).window
                // 拿取到准确的状态栏高度(横竖屏高度是不一致的)
                val insets = ViewCompat.getRootWindowInsets(window.decorView)
                val statusBarHeight = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top.orZero
                // 默认全屏窗体的状态
                controllerToggle(window, true)
                // 可能部分机型会有问题,不过基本是兼容的
                window.setStatusBarLightMode(false)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    immersionBar?.apply {
                        reset()
                        statusBarDarkFont(false, 0.2f)
                        init()
                    }
                }
                // 拿取播放器的顶部菜单,空出状态栏的高度距离
                topContainer = gsy.getTopContainer() as? LinearLayout
                topContainer.doOnceAfterLayout {
                    it.setBackgroundColor(color(R.color.bottom_container_bg))
                    it.size(height = it.measuredHeight + statusBarHeight)
                    it.padding(top = statusBarHeight)
                    // 监听子View的布局变化
                    if (null == onPreDrawListener) {
                        onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
                            checkVisibilityChange(window, it)
                            true
                        }
                    }
                    onPreDrawListener?.let { block ->
                        it.viewTreeObserver.removeOnPreDrawListener(block)
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
                onGSYVideoPlayerListener?.onEnterFullscreen(url, objects)
            }

            override fun onQuitFullscreen(url: String, vararg objects: Any) {
                super.onQuitFullscreen(url, *objects)
                orientationUtils?.backToProtVideo()
                onGSYVideoPlayerListener?.onQuitFullscreen(url, objects)
            }

            override fun onQuitSmallWidget(url: String?, vararg objects: Any?) {
                super.onQuitSmallWidget(url, *objects)
                onGSYVideoPlayerListener?.onQuitSmallWidget(url, objects)
            }

            override fun onEnterSmallWidget(url: String?, vararg objects: Any?) {
                super.onEnterSmallWidget(url, *objects)
                onGSYVideoPlayerListener?.onEnterSmallWidget(url, objects)
            }

            override fun onTouchScreenSeekVolume(url: String?, vararg objects: Any?) {
                super.onTouchScreenSeekVolume(url, *objects)
                onGSYVideoPlayerListener?.onTouchScreenSeekVolume(url, objects)
            }

            override fun onTouchScreenSeekPosition(url: String?, vararg objects: Any?) {
                super.onTouchScreenSeekPosition(url, *objects)
                onGSYVideoPlayerListener?.onTouchScreenSeekPosition(url, objects)
            }

            override fun onTouchScreenSeekLight(url: String?, vararg objects: Any?) {
                super.onTouchScreenSeekLight(url, *objects)
                onGSYVideoPlayerListener?.onTouchScreenSeekLight(url, objects)
            }

            override fun onPlayError(url: String?, vararg objects: Any?) {
                super.onPlayError(url, *objects)
                if (!retryWithPlay) {
                    retryWithPlay = true
                    player.disable()
                    restartJob?.cancel()
                    restartJob = mActivity.lifecycleScope.launch {
                        // 允许硬件解码，装载IJK播放器内核
//                        GSYVideoType.enableMediaCodec()
                        GSYVideoType.enableMediaCodecTexture()
                        PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
                        CacheFactory.setCacheManager(ProxyCacheManager::class.java)
                        delay(1000L)
                        player.enable()
                        player?.startPlayLogic()
                    }
                }
                onGSYVideoPlayerListener?.onPlayError(url, objects)
            }

            override fun onClickStartThumb(url: String?, vararg objects: Any?) {
                super.onClickStartThumb(url, *objects)
                onGSYVideoPlayerListener?.onClickStartThumb(url, objects)
            }

            override fun onClickBlank(url: String?, vararg objects: Any?) {
                super.onClickBlank(url, *objects)
                onGSYVideoPlayerListener?.onClickBlank(url, objects)
            }

            override fun onClickBlankFullscreen(url: String?, vararg objects: Any?) {
                super.onClickBlankFullscreen(url, *objects)
                onGSYVideoPlayerListener?.onClickBlankFullscreen(url, objects)
            }

            override fun onComplete(url: String?, vararg objects: Any?) {
                super.onComplete(url, *objects)
                onGSYVideoPlayerListener?.onComplete(url, objects)
            }

            private fun checkVisibilityChange(window: Window, childView: View?) {
                childView ?: return
                val isVisible = childView.isVisible
                if (isVisible != lastVisible) {
                    lastVisible = isVisible
                    controllerToggle(window, isVisible)
                }
            }

            private fun controllerToggle(window: Window, isShow: Boolean) {
                toggleJob?.cancel()
                toggleJob = mActivity.lifecycleScope.launch {
                    delay(300L)
                    window.controllerToggle(isShow)
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
    fun <T : StandardGSYVideoPlayer> bind(standardGSYVideoPlayer: T?, showTitle: Boolean = false, showBack: Boolean = false, showFullScreen: Boolean = false) {
        player = standardGSYVideoPlayer
        player.initialize(binding.root, showTitle, showBack, showFullScreen)
        // 返回处理
        if (showBack) {
            player?.backButton.click {
                mActivity.finish()
            }
        }
        // 全屏处理
        if (showFullScreen) {
            // 外部辅助的旋转，帮助全屏
            orientationUtils = OrientationUtils(mActivity, player)
            // 初始化不打开外部的旋转
            orientationUtils?.isEnable = false
            // 直接横屏
            player?.fullscreenButton?.click {
                orientationUtils?.resolveByClick()
                // 第一个true是否需要隐藏actionbar，第二个true是否需要隐藏 StatusBar
                player?.startWindowFullscreen(player?.context, true, true)
            }
        }
    }

    /**
     * 设置自定义标题
     */
    fun setCustomTitle(layoutRes: Int): View? {
        // 获取播放器标题
        val targetTv = player?.titleTextView
        // 获取标题父容器
        val parent = targetTv?.parent
        if (parent !is ViewGroup) {
            return null
        }
        // 移除原来的标题
        parent.removeView(targetTv)
        val customView = LayoutInflater.from(targetTv.context).inflate(layoutRes, parent, false)
//        // 继承原 TextView 的宽高、margin、权重等所有参数
//        customView.layoutParams = targetTv.layoutParams
        // 添加 View（自动放在原位置）
        parent.addView(customView)
        // 返回自定义整体容器
        return customView
    }

    /**
     * 设置返回
     */
    fun setCustomBack(block: () -> Unit = {}) {
        player?.backButton.click {
            block.invoke()
        }
    }

    /**
     * 设置全屏
     */
    fun setCustomFullScreen(block: () -> Unit = {}) {
        player?.fullscreenButton.click {
            block.invoke()
        }
    }

    /**
     * 设置播放路径，缩略图，是否自动开始播放
     */
    fun setUrl(url: String, thumbUrl: String? = null, setUpLazy: Boolean = false) {
        // 加载图片(非自动播放的情况下加载中不可点击)
        val onLoadStartAction = {
            if (!setUpLazy) {
                player?.startButton.disable()
                thumbJob?.cancel()
                thumbJob = mActivity.lifecycleScope.launch {
                    delay(3000L)
                    player?.startButton.enable()
                }
            }
        }
        val onLoadCompleteAction = {
            if (!setUpLazy) {
                player?.startButton.enable()
            }
        }
        if (thumbUrl.isNullOrEmpty()) {
            ImageLoader.instance.loadVideoFrameFromUrl(binding.ivThumb, url, onLoadStart = {
                onLoadStartAction()
            }, onLoadComplete = {
                if (it == null) {
                    // 如果Glide加载失败,采用视频工具类的suspendingThumbnail方法再次尝试进行加载
                    binding.ivThumb.background(DEFAULT_RESOURCE)
                    thumbJob?.cancel()
                    thumbJob = mActivity.lifecycleScope.launch {
                        val bitmap = withTimeoutOrNull(3000L) { suspendingThumbnail(mActivity, url) }
                        if (null != bitmap) {
                            binding.ivThumb.setBitmap(mActivity, bitmap)
                        } else {
                            binding.ivThumb.background(DEFAULT_MASK_RESOURCE)
                        }
                        onLoadCompleteAction()
                    }
                } else {
                    onLoadCompleteAction()
                }
            })
        } else {
            ImageLoader.instance.loadImageFromUrl(binding.ivThumb, thumbUrl, onLoadStart = {
                onLoadStartAction()
            }, onLoadComplete = {
                onLoadCompleteAction()
            })
        }
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
            .setVideoAllCallBack(gsySampleCallBack)
            // 开启构建,绑定配置
            .build(player)
        // 如果需要自动播放,此时开启
        if (setUpLazy) startPlayLogic()
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
        if (isPrepared && !isPause) player?.onConfigurationChanged(mActivity, newConfig, orientationUtils, true, true)
    }

    fun setOnGSYVideoPlayerListener(onGSYVideoPlayerListener: OnGSYVideoPlayerListener) {
        this.onGSYVideoPlayerListener = onGSYVideoPlayerListener
    }

    fun clearOnGSYVideoPlayerListener() {
        onGSYVideoPlayerListener = null
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onVideoResume()
            Lifecycle.Event.ON_PAUSE -> onVideoPause()
            Lifecycle.Event.ON_DESTROY -> onVideoDestroy()
            else -> {}
        }
    }

    /**
     * 播放-默认一次切内核的重试机会
     */
    fun startPlayLogic() {
        isPrepared = false
        retryWithPlay = false
        player?.startPlayLogic()
    }

    /**
     * 暂停
     */
    fun onVideoPause() {
        isPause = true
        player?.currentPlayer?.onVideoPause()
    }

    /**
     * 加载
     */
    fun onVideoResume(seek: Boolean = false) {
        isPause = false
        player?.currentPlayer?.onVideoResume(seek)
    }

    /**
     * 销毁
     */
    fun onVideoDestroy() {
        clearOnGSYVideoPlayerListener()
        onPreDrawListener?.let {
            topContainer?.viewTreeObserver?.removeOnPreDrawListener(it)
        }
        topContainer = null
        orientationUtils?.releaseListener()
        player?.currentPlayer?.release()
        player?.release()
        player = null
        thumbJob?.cancel()
        toggleJob?.cancel()
        restartJob?.cancel()
        mActivity.lifecycle.removeObserver(this)
    }

}