package com.example.gsyvideoplayer.video

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.click
import com.shuyu.gsyvideoplayer.R
import com.shuyu.gsyvideoplayer.model.GSYVideoModel
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import java.io.File
import kotlin.math.abs

/**
 * 只支持每个片头广告播放的类
 * 其实就是根据实体，判断播放列表中哪个是广告，哪个不是，从而处理不同的UI显示效果
 * Created by guoshuyu on 2018/1/26.
 */
@SuppressLint("SetTextI18n")
class GSYSampleADVideoPlayer : ListGSYVideoPlayer {
    protected var mJumpAd: View? = null
    protected var mWidgetContainer: ViewGroup? = null
    protected var mADTime: TextView? = null
    protected var isAdModel: Boolean = false
    protected var isFirstPrepared: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    override fun init(context: Context?) {
        super.init(context)
        mJumpAd = findViewById(R.id.jump_ad)
        mADTime = findViewById(R.id.ad_time)
        mWidgetContainer = findViewById(R.id.widget_container)
        if (mJumpAd != null) {
            mJumpAd.click { playNext() }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.video_layout_sample_ad
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @return
     */
    override fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int): Boolean {
        return setUp(url, cacheWithPlay, position, null)
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @return
     */
    override fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int, cachePath: File?): Boolean {
        return setUp(url, cacheWithPlay, position, cachePath, HashMap())
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @return
     */
    override fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int, cachePath: File?, mapHeadData: Map<String, String>): Boolean {
        return setUp(url, cacheWithPlay, position, cachePath, mapHeadData, true)
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
     * @return
     */
    override fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int, cachePath: File?, mapHeadData: Map<String, String>, changeState: Boolean): Boolean {
        val gsyVideoModel = url[position]
        if (gsyVideoModel is GSYADVideoModel) {
            if (gsyVideoModel.isSkip && position < (url.size - 1)) {
                return setUp(url, cacheWithPlay, position + 1, cachePath, mapHeadData, changeState)
            }
            isAdModel = (gsyVideoModel.mType == GSYADVideoModel.TYPE_AD)
        }
        changeAdUIState()
        return super.setUp(url, cacheWithPlay, position, cachePath, mapHeadData, changeState)
    }

    override fun onPrepared() {
        super.onPrepared()
        isFirstPrepared = true
        changeAdUIState()
    }

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            (mStartButton as? ImageView)?.setImageResource(
                when (mCurrentState) {
                    CURRENT_STATE_PLAYING -> R.drawable.video_click_pause_selector
                    CURRENT_STATE_ERROR -> R.drawable.video_click_play_selector
                    else -> R.drawable.video_click_play_selector
                }
            )
        }
    }

    /**
     * 广告期间不需要双击
     */
    override fun touchDoubleUp(e: MotionEvent?) {
        if (isAdModel) {
            return
        }
        super.touchDoubleUp(e)
    }

    /**
     * 广告期间不需要触摸
     */
    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (mChangePosition && isAdModel) {
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
            if (isAdModel && absDeltaX >= mThreshold && abs((screenWidth - mDownX).toDouble()) > mSeekEndOffset) {
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
        if (mChangePosition && isAdModel) {
            return
        }
        super.touchSurfaceUp()
    }

    override fun hideAllWidget() {
        if (isFirstPrepared && isAdModel) {
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
        val sf = from as? GSYSampleADVideoPlayer
        val st = to as? GSYSampleADVideoPlayer
        st?.isAdModel = sf?.isAdModel.orFalse
        st?.isFirstPrepared = sf?.isFirstPrepared.orFalse
        st?.changeAdUIState()
    }

    /**
     * 根据是否广告url修改ui显示状态
     */
    protected fun changeAdUIState() {
        if (mJumpAd != null) {
            mJumpAd?.visibility = if ((isFirstPrepared && isAdModel)) VISIBLE else GONE
        }
        if (mADTime != null) {
            mADTime?.visibility = if ((isFirstPrepared && isAdModel)) VISIBLE else GONE
        }
        if (mWidgetContainer != null) {
            mWidgetContainer?.visibility = if ((isFirstPrepared && isAdModel)) GONE else VISIBLE
        }
        if (mBottomContainer != null) {
            val color = if ((isFirstPrepared && isAdModel)) Color.TRANSPARENT else context.resources.getColor(R.color.bottom_container_bg)
            mBottomContainer.setBackgroundColor(color)
        }
        if (mCurrentTimeTextView != null) {
            mCurrentTimeTextView.visibility = if ((isFirstPrepared && isAdModel)) INVISIBLE else VISIBLE
        }
        if (mTotalTimeTextView != null) {
            mTotalTimeTextView.visibility = if ((isFirstPrepared && isAdModel)) INVISIBLE else VISIBLE
        }
        if (mProgressBar != null) {
            mProgressBar.visibility = if ((isFirstPrepared && isAdModel)) INVISIBLE else VISIBLE
            mProgressBar.isEnabled = !(isFirstPrepared && isAdModel)
        }
    }

    /******************对外接口*******************/
    /******************对外接口 */
    /**
     * 带片头广告的，setAdUp
     *
     * @param url
     * @param cacheWithPlay
     * @param position
     * @return
     */
    fun setAdUp(url: ArrayList<GSYADVideoModel?>, cacheWithPlay: Boolean, position: Int): Boolean {
        return setUp((url.clone() as? ArrayList<GSYVideoModel>).orEmpty(), cacheWithPlay, position)
    }

    /**
     * 带片头广告的，setAdUp
     *
     * @param url
     * @param cacheWithPlay
     * @param position
     * @param cachePath
     * @return
     */
    fun setAdUp(url: ArrayList<GSYADVideoModel?>, cacheWithPlay: Boolean, position: Int, cachePath: File?): Boolean {
        return setUp((url.clone() as? ArrayList<GSYVideoModel>).orEmpty(), cacheWithPlay, position, cachePath)
    }

    /**
     * 带片头广告的，setAdUp
     *
     * @param url
     * @param cacheWithPlay
     * @param position
     * @param cachePath
     * @param mapHeadData
     * @return
     */
    fun setAdUp(url: ArrayList<GSYADVideoModel?>, cacheWithPlay: Boolean, position: Int, cachePath: File?, mapHeadData: Map<String, String>): Boolean {
        return setUp((url.clone() as? ArrayList<GSYVideoModel>).orEmpty(), cacheWithPlay, position, cachePath, mapHeadData)
    }

    class GSYADVideoModel : GSYVideoModel {
        /**
         * 类型
         */
        var mType = TYPE_NORMAL

        /**
         * 是否跳过
         */
        var isSkip = false

        companion object {
            /**
             * 正常
             */
            @JvmStatic
            var TYPE_NORMAL = 0

            /**
             * 广告
             */
            @JvmStatic
            var TYPE_AD = 1
        }

        /**
         * @param url   播放url
         * @param title 标题
         * @param type  类型 广告还是正常类型
         */
        constructor(url: String, title: String, type: Int) : super(url, title) {
            this.mType = type
            this.isSkip = false
        }

        /**
         * @param url    播放url
         * @param title  标题
         * @param type   类型 广告还是正常类型
         * @param isSkip 是否跳过
         */
        constructor(url: String, title: String, type: Int, isSkip: Boolean) : super(url, title) {
            this.mType = type
            this.isSkip = isSkip
        }
    }

}