package com.example.gsyvideoplayer.video

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.click
import com.shuyu.gsyvideoplayer.GSYVideoADManager
import com.shuyu.gsyvideoplayer.R
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge
import kotlin.math.abs

/**
 * Created by guoshuyu on 2018/2/1.
 */
@SuppressLint("SetTextI18n")
class GSYADVideoPlayer : StandardGSYVideoPlayer {
    protected var mJumpAd: View? = null
    protected var mADTime: TextView? = null
    protected var isFirstPrepared: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    override fun init(context: Context?) {
        super.init(context)
        mJumpAd = findViewById(R.id.jump_ad)
        mADTime = findViewById(R.id.ad_time)
        if (mJumpAd != null) {
            mJumpAd?.click {
                if (gsyVideoManager.listener() != null) {
                    gsyVideoManager.listener().onAutoCompletion()
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.video_layout_ad
    }

    override fun getGSYVideoManager(): GSYVideoViewBridge {
        GSYVideoADManager.instance().initContext(context.applicationContext)
        return GSYVideoADManager.instance()
    }

    override fun backFromFull(context: Context?): Boolean {
        return GSYVideoADManager.backFromWindowFull(context)
    }

    override fun releaseVideos() {
        GSYVideoADManager.releaseAllVideos()
    }

    override fun getFullId(): Int {
        return GSYVideoADManager.FULLSCREEN_ID
    }

    override fun getSmallId(): Int {
        return GSYVideoADManager.SMALL_ID
    }

    override fun onPrepared() {
        super.onPrepared()
        isFirstPrepared = true
        changeAdUIState()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.start) {
            if (mCurrentState == CURRENT_STATE_ERROR) {
                clickStartIcon()
            }
        } else {
            super.onClick(v)
        }
    }

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            (mStartButton as? ImageView)?.setImageResource(
                when (mCurrentState) {
                    CURRENT_STATE_PLAYING -> R.drawable.empty_drawable
                    CURRENT_STATE_ERROR -> R.drawable.video_click_error_selector
                    else -> R.drawable.empty_drawable
                }
            )
        }
    }

    /**
     * 广告期间不需要双击
     */
    override fun touchDoubleUp(e: MotionEvent?) {
    }

    /**
     * 广告期间不需要触摸
     */
    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (mChangePosition) {
        } else {
            super.touchSurfaceMove(deltaX, deltaY, y)
        }
    }

    /**
     * 广告期间不需要触摸
     */
    override fun touchSurfaceMoveFullLogic(absDeltaX: Float, absDeltaY: Float) {
        if ((absDeltaX > mThreshold || absDeltaY > mThreshold)) {
            val screenWidth = CommonUtil.getScreenWidth(context)
            if (absDeltaX >= mThreshold && abs((screenWidth - mDownX).toDouble()) > mSeekEndOffset) {
                //防止全屏虚拟按键
                mChangePosition = true
                mDownPosition = currentPositionWhenPlaying
            } else {
                super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY)
            }
        }
    }

    /**
     * 广告期间不需要触摸
     */
    override fun touchSurfaceUp() {
        if (mChangePosition) {
            return
        }
        super.touchSurfaceUp()
    }

    override fun hideAllWidget() {
        if (isFirstPrepared) {
            return
        }
        super.hideAllWidget()
    }

    override fun setProgressAndTime(progress: Long, secProgress: Long, currentTime: Long, totalTime: Long, forceChange: Boolean) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
        if (mADTime != null && currentTime > 0) {
            val totalSeconds = totalTime / 1000
            val currentSeconds = currentTime / 1000
            mADTime?.text = "${(totalSeconds - currentSeconds)}"
        }
    }

    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        val sf = from as? GSYADVideoPlayer
        val st = to as? GSYADVideoPlayer
        st?.isFirstPrepared = sf?.isFirstPrepared.orFalse
        st?.changeAdUIState()
    }

    override fun release() {
        super.release()
        if (mADTime != null) {
            mADTime?.visibility = GONE
        }
    }

    /**
     * 根据是否广告url修改ui显示状态
     */
    protected fun changeAdUIState() {
        if (mJumpAd != null) {
            mJumpAd?.visibility = if ((isFirstPrepared)) VISIBLE else GONE
        }
        if (mADTime != null) {
            mADTime?.visibility = if ((isFirstPrepared)) VISIBLE else GONE
        }
        if (mBottomContainer != null) {
            val color = if ((isFirstPrepared)) Color.TRANSPARENT else context.resources.getColor(R.color.bottom_container_bg)
            mBottomContainer.setBackgroundColor(color)
        }
        if (mCurrentTimeTextView != null) {
            mCurrentTimeTextView.visibility = if ((isFirstPrepared)) INVISIBLE else VISIBLE
        }
        if (mTotalTimeTextView != null) {
            mTotalTimeTextView.visibility = if ((isFirstPrepared)) INVISIBLE else VISIBLE
        }
        if (mProgressBar != null) {
            mProgressBar.visibility = if ((isFirstPrepared)) INVISIBLE else VISIBLE
            mProgressBar.isEnabled = !(isFirstPrepared)
        }
    }

    /**
     * 移除广告播放的全屏
     */
    fun removeFullWindowViewOnly() {
        val vp = CommonUtil.scanForActivity(context).findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val old = vp.findViewById<View>(fullId)
        if (old != null) {
            if (old.parent != null) {
                val viewGroup = old.parent as? ViewGroup
                vp.removeView(viewGroup)
            }
        }
        mIfCurrentIsFullscreen = false
    }

}