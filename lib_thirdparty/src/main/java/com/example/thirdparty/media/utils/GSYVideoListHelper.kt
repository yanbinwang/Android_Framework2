package com.example.thirdparty.media.utils

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
import java.util.concurrent.ConcurrentHashMap
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
class GSYVideoListHelper(private val mActivity: FragmentActivity, private val recycler: RecyclerView?) : LifecycleEventObserver {
    private val data by lazy { ConcurrentHashMap<Int, String>() }

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 列表内部调取
     * 适配器初始化传入helper，然后内部调取setUpLazy方法
     */
    @Synchronized
    fun setUpLazy(player: StandardGSYVideoPlayer?, url: String, position: Int) {
        //每一项都会调取dataManager
        data[position] = url
        //设置每个列表的url
        player?.setUpLazy(url, true, null, null, "这是title")
        //增加title
        player?.titleTextView?.gone()
        //设置返回键
        player?.backButton?.gone()
        //设置全屏按键功能
        player?.fullscreenButton?.click {
            player.startWindowFullscreen(player.context, false, true)
        }
        //防止错位设置(下标+url)
        player?.playTag = "${position}::${url}"
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
    fun setOnScrollListener(listener: ((position: Int) -> Unit)) {
        recycler?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? GridLayoutManager
                // 获取首个可见的item的位置
                val firstVisibleItem = layoutManager?.findFirstVisibleItemPosition().orZero
                // 获取当前屏幕可见的item数量
                val visibleItemCount = max(0, layoutManager?.childCount.orZero)
                // 获取最后一个可见的item的索引
                val lastVisibleItem = firstVisibleItem + visibleItemCount - 1
                //当前播放的位置
                val position = GSYVideoManager.instance().playPosition
                //大于0说明有播放
                if (position >= 0) {
                    //当前播放tag
                    val playTag = data[position]
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag.equals(playTag) && (position < firstVisibleItem || position > lastVisibleItem)) {
                        if (GSYVideoManager.isFullState(mActivity)) {
                            return
                        }
                        //如果滑出去了上面和下面就是否，和今日头条一样
//                        GSYVideoManager.releaseAllVideos()
//                        adapter.notifyDataSetChanged()
                        listener.invoke(position)
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
     * 页面全局刷新时清空本地data后再覆盖赋值
     */
    fun onRefresh() {
        data.clear()
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
                recycler?.clearOnScrollListeners()
                data.clear()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}