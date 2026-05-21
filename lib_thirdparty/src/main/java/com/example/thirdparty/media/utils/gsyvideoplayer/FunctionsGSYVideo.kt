package com.example.thirdparty.media.utils.gsyvideoplayer

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoControlView
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager
import kotlin.math.max

//------------------------------------播放器扩展函数类------------------------------------
// ==============================
//  https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md
//
//  <activity
//      android:name=".xxxxx"
//      android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
//      android:screenOrientation="portrait" />
// ==============================

/**
 * 设置列表监听
 * /**
 *  * 全屏时写，写在系统的onBackPressed之前
 *  * @Override
 *  * public void onBackPressed() {
 *  * if (GSYVideoManager.backFromWindowFull(this)) {
 *  * return;
 *  * }
 *  * super.onBackPressed();
 *  * }
 *  */
 * fun onBackPressed(): Boolean {
 *     return GSYVideoManager.backFromWindowFull(mActivity)
 * }
 */
fun XRecyclerView?.setOnScrollListener(activity: FragmentActivity, playTag: String, listener: ((position: Int) -> Unit)) {
    this ?: return
    recycler.setOnScrollListener(activity, playTag, listener)
}

fun RecyclerView?.setOnScrollListener(activity: FragmentActivity, playTag: String, listener: ((position: Int) -> Unit)) {
    this ?: return
    /**
     * 如果不同页面的播放器设置了不同的 playTag：releaseAllVideos() 只会释放「当前 playTag 关联的播放器」，不会影响其他页面的视频。
     * 如果所有页面共用同一个 playTag（或未设置）：releaseAllVideos() 会释放全局所有播放器，包括其他页面的视频。
     */
    activity.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    GSYVideoManager.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    GSYVideoManager.onPause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    GSYVideoManager.releaseAllVideos()
                    clearOnScrollListeners()
                    source.lifecycle.removeObserver(this)
                }
                else -> {}
            }
        }
    })
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as? GridLayoutManager
            // 获取首个可见的item的位置
            val firstVisibleItem = layoutManager?.findFirstVisibleItemPosition().orZero
            // 获取当前屏幕可见的item数量
            val visibleItemCount = max(0, layoutManager?.childCount.orZero)
            // 获取最后一个可见的item的索引
            val lastVisibleItem = firstVisibleItem + visibleItemCount - 1
            // 当前播放的位置
            val position = GSYVideoManager.instance().playPosition
            // 大于0说明有播放
            if (position >= 0) {
                // 对应的播放列表TAG
                if (GSYVideoManager.instance().playTag.equals(playTag) && (position !in firstVisibleItem..lastVisibleItem)) {
                    if (GSYVideoManager.isFullState(activity)) {
                        return
                    }
                    // 如果滑出去了上面和下面就是否，和今日头条一样
                    GSYVideoManager.releaseAllVideos()
//                    adapter.notifyDataSetChanged()
                    listener.invoke(position)
                }
            }
        }
    })
}

/**
 * 列表内部每一个选项都会调取
 * 1) 视频列表切记不能带有删除功能,如果有的情况下,部分方法需要重构
 * 2) 适配器初始化传入helper，然后内部调取setUpLazy方法 (可不写,方法内初始化)
 *
 * @tag
 * 1) 在 GSYVideoPlayer 的列表播放场景中，playTag 的核心作用是标识「当前播放所属的列表或播放组」，用于区分不同列表或不同播放场景的视频，避免播放状态混乱。
 * 2) 一个列表（或一个独立的播放场景）使用唯一的 playTag，而不是每个 Item 单独设置不同的 Tag。
 */
@Synchronized
fun StandardGSYVideoPlayer?.setUpLazy(url: String, position: Int, tag: String) {
    this ?: return
    // 设置每个列表的url
    setUpLazy(url, true, null, null, null)
//    // 增加title
//    titleTextView?.gone()
//    // 设置返回键
//    backButton?.gone()
//    // 设置全屏按键功能
//    fullscreenButton?.click {
//        startWindowFullscreen(context, false, true)
//    }
    initialize(showFullScreen = true)
    // 设置全屏按键功能
    fullscreenButton?.click {
        startWindowFullscreen(context, false, true)
    }
    // 防止错位设置(针对列表所带的页面)
    playTag = tag
    playPosition = position
    // 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
    isAutoFullWithSize = true
    // 音频焦点冲突时是否释放
    isReleaseWhenLossAudio = false
    // 全屏动画
    isShowFullAnimation = true
    // 小屏时不触摸滑动
    setIsTouchWiget(false)
}

/**
 * 初始化GSY视频播放器基础配置
 * @param mThumbView 封面图View
 * @param showTitle 是否显示标题
 * @param showBack 是否显示返回按钮
 * @param showFullScreen 是否显示全屏按钮
 */
fun StandardGSYVideoPlayer?.initialize(mThumbView: View? = null, showTitle: Boolean = false, showBack: Boolean = false, showFullScreen: Boolean = false) {
    this ?: return
    // 屏幕展示效果 -> 采用基础配资
    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
    // 设置底层渲染,关闭硬件解码
    GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
//    GSYVideoType.disableMediaCodec()
    GSYVideoType.disableMediaCodecTexture()
    // 默认采用exo内核，播放报错则切ijk内核
    PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
    CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
    // 配置适配遮罩，隐藏默认的顶部菜单的返回/标题
    if (null != mThumbView) thumbImageView = mThumbView
    if (showTitle) titleTextView.visible() else titleTextView?.gone()
    if (showBack) backButton.visible() else backButton?.gone()
    if (showFullScreen) fullscreenButton.visible() else fullscreenButton.gone()
}

/**
 * 获取播放器顶部/底部菜单 (可转线性布局)
 */
fun GSYVideoControlView?.getTopContainer(): ViewGroup? {
    this ?: return null
    return getContainer(this, "mTopContainer")
}

fun GSYVideoControlView?.getBottomContainer(): ViewGroup? {
    this ?: return null
    return getContainer(this, "mBottomContainer")
}

private fun getContainer(controlView: GSYVideoControlView?, fieldName: String): ViewGroup? {
    return try {
        // 获取GSYVideoControlView类的Class对象
        val clazz = GSYVideoControlView::class.java
        // 获取名为"mTopContainer"的字段
        val field = clazz.getDeclaredField(fieldName)
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
 * 菜单栏状态
 */
fun Window?.controllerToggle(isShow: Boolean) {
    this ?: return
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

/**
 * 自定义播放器回调
 */
interface OnGSYVideoPlayerListener {
    /**
     * 开始加载，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onStartPrepared(url: String?, vararg objects: Any?) {}

    /**
     * 加载成功，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onPrepared(url: String?, vararg objects: Any?) {}

    /**
     * 点击了开始按键播放，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickStartIcon(url: String?, vararg objects: Any?) {}

    /**
     * 点击了错误状态下的开始按键，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickStartError(url: String?, vararg objects: Any?) {}

    /**
     * 点击了播放状态下的开始按键--->停止，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickStop(url: String?, vararg objects: Any?) {}

    /**
     * 点击了全屏播放状态下的开始按键--->停止，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickStopFullscreen(url: String?, vararg objects: Any?) {}

    /**
     * 点击了暂停状态下的开始按键--->播放，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickResume(url: String?, vararg objects: Any?) {}

    /**
     * 点击了全屏暂停状态下的开始按键--->播放，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickResumeFullscreen(url: String?, vararg objects: Any?) {}

    /**
     * 点击了空白弹出seekbar，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickSeekbar(url: String?, vararg objects: Any?) {}

    /**
     * 点击了全屏的seekbar，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickSeekbarFullscreen(url: String?, vararg objects: Any?) {}

    /**
     * 播放完了，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onAutoComplete(url: String?, vararg objects: Any?) {}

    /**
     * 进去全屏，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onEnterFullscreen(url: String?, vararg objects: Any?) {}

    /**
     * 退出全屏，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onQuitFullscreen(url: String?, vararg objects: Any?) {}

    /**
     * 进入小窗口，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onQuitSmallWidget(url: String?, vararg objects: Any?) {}

    /**
     * 退出小窗口，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onEnterSmallWidget(url: String?, vararg objects: Any?) {}

    /**
     * 触摸调整声音，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onTouchScreenSeekVolume(url: String?, vararg objects: Any?) {}

    /**
     * 触摸调整进度，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onTouchScreenSeekPosition(url: String?, vararg objects: Any?) {}

    /**
     * 触摸调整亮度，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onTouchScreenSeekLight(url: String?, vararg objects: Any?) {}

    /**
     * 播放错误，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onPlayError(url: String?, vararg objects: Any?) {}

    /**
     * 点击了空白区域开始播放，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickStartThumb(url: String?, vararg objects: Any?) {}

    /**
     * 点击了播放中的空白区域，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickBlank(url: String?, vararg objects: Any?) {}

    /**
     * 点击了全屏播放中的空白区域，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onClickBlankFullscreen(url: String?, vararg objects: Any?) {}

    /**
     * 非正常播放完了,比如新的播放旧的释放，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
     */
    fun onComplete(url: String?, vararg objects: Any?) {}
}