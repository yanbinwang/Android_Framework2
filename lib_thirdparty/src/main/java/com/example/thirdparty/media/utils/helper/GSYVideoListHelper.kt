package com.example.thirdparty.media.utils.helper

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.value.orZero
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
class GSYVideoListHelper(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private var TAG: String = ""
    private var rvList: RecyclerView? = null

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 绑定列表
     */
    fun bind(rvList: RecyclerView?, TAG: String) {
        this.rvList = rvList
        this.TAG = TAG
    }

    /**
     * 列表内部调取
     */
    @Synchronized
    fun setUpLazy(player: StandardGSYVideoPlayer?, url: String, position: Int) {
        player?.setUpLazy(url, true, null, null, "这是title")
        //增加title
        player?.titleTextView?.visibility = View.GONE
        //设置返回键
        player?.backButton?.setVisibility(View.GONE)
        //设置全屏按键功能
        player?.fullscreenButton?.setOnClickListener {
            player.startWindowFullscreen(player.context, false, true)
        }
        //防止错位设置
        player?.playTag = TAG
        player?.playPosition = position
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
        player?.isAutoFullWithSize = true
        //音频焦点冲突时是否释放
        player?.isReleaseWhenLossAudio = false
        //全屏动画
        player?.isShowFullAnimation = true
        //小屏时不触摸滑动
        player?.setIsTouchWiget(false)
    }

    /**
     * 设置列表监听
     */
    fun setOnScrollListener(listener: (() -> Unit)) {
        rvList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? GridLayoutManager
                // 获取首个可见的item的位置
                val firstVisibleItem = layoutManager?.findFirstVisibleItemPosition().orZero
                // 获取当前屏幕可见的item数量
                val visibleItemCount = max(0, layoutManager?.childCount.orZero)
                // 获取最后一个可见的item的索引
                val lastVisibleItem = firstVisibleItem + visibleItemCount - 1
                //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) {
                    //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag.equals(TAG) && (position < firstVisibleItem || position > lastVisibleItem)) {
                        if (GSYVideoManager.isFullState(mActivity)) {
                            return
                        }
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        GSYVideoManager.releaseAllVideos()
//                        adapter.notifyDataSetChanged()
                        listener.invoke()
                    }
                }
            }
        })
    }

    /**
     * 全屏时写，写在系统的onBackPressed之前
     * @Override
     * public void onBackPressed() {
     * if (GSYVideoManager.backFromWindowFull(this)) {
     * return;
     * }
     * super.onBackPressed();
     * }
     */
    fun onBackPressed(): Boolean {
        return GSYVideoManager.backFromWindowFull(mActivity)
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> GSYVideoManager.onResume()
            Lifecycle.Event.ON_PAUSE -> GSYVideoManager.onPause()
            Lifecycle.Event.ON_DESTROY -> {
                GSYVideoManager.releaseAllVideos()
                rvList?.clearOnScrollListeners()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}