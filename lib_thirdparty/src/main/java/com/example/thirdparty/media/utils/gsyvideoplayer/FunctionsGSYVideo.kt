package com.example.thirdparty.media.utils.gsyvideoplayer

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import kotlin.math.max

/**
 * @description 播放器帮助类
 * @author yan
 *
 * https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md
 *
 * <activity
 *     android:name=".xxxxx"
 *     android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
 *     android:screenOrientation="portrait" />
 */

/**
 * 设置列表监听
 * //    /**
 * //     * 全屏时写，写在系统的onBackPressed之前
 * //     * @Override
 * //     * public void onBackPressed() {
 * //     * if (GSYVideoManager.backFromWindowFull(this)) {
 * //     * return;
 * //     * }
 * //     * super.onBackPressed();
 * //     * }
 * //     */
 * //    fun onBackPressed(): Boolean {
 * //        return GSYVideoManager.backFromWindowFull(mActivity)
 * //    }
 */
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
                if (GSYVideoManager.instance().playTag.equals(playTag) && (position < firstVisibleItem || position > lastVisibleItem)) {
                    if (GSYVideoManager.isFullState(activity)) {
                        return
                    }
                    // 如果滑出去了上面和下面就是否，和今日头条一样
                    GSYVideoManager.releaseAllVideos()
//                        adapter.notifyDataSetChanged()
                    listener.invoke(position)
                }
            }
        }
    })
}

/**
 * 列表内部每一个选项都会调取
 * 1.视频列表切记不能带有删除功能,如果有的情况下,部分方法需要重构
 * 2.适配器初始化传入helper，然后内部调取setUpLazy方法
 *
 * @playTag
 * 在 GSYVideoPlayer 的列表播放场景中，playTag 的核心作用是标识「当前播放所属的列表或播放组」，用于区分不同列表或不同播放场景的视频，避免播放状态混乱。
 * 一个列表（或一个独立的播放场景）使用唯一的 playTag，而不是每个 Item 单独设置不同的 Tag。
 */
@Synchronized
fun setUpLazy(player: StandardGSYVideoPlayer?, url: String, position: Int, playTag: String) {
    // 设置每个列表的url
    player?.setUpLazy(url, true, null, null, "这是title")
    // 增加title
    player?.titleTextView?.gone()
    // 设置返回键
    player?.backButton?.gone()
    // 设置全屏按键功能
    player?.fullscreenButton?.click {
        player.startWindowFullscreen(player.context, false, true)
    }
    // 防止错位设置(针对列表所带的页面)
    player?.playTag = playTag
    player?.playPosition = position
    // 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
    player?.isAutoFullWithSize = true
    // 音频焦点冲突时是否释放
    player?.isReleaseWhenLossAudio = false
    // 全屏动画
    player?.isShowFullAnimation = true
    // 小屏时不触摸滑动
    player?.setIsTouchWiget(false)
}