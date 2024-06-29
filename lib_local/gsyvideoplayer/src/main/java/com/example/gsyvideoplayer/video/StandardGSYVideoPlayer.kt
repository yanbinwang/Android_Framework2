package com.example.gsyvideoplayer.video

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.shuyu.gsyvideoplayer.R
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.utils.NetworkUtils
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import moe.codeest.enviews.ENDownloadView
import moe.codeest.enviews.ENPlayView
import java.io.File

/**
 * 标准播放器，继承之后实现一些ui显示效果，如显示／隐藏ui，播放按键等
 * Created by shuyu on 2016/11/11.
 */
@SuppressLint("SetTextI18n")
open class StandardGSYVideoPlayer : GSYVideoPlayer {
    //亮度dialog
    protected var mBrightnessDialog: Dialog? = null
    //音量dialog
    protected var mVolumeDialog: Dialog? = null
    //触摸进度dialog
    protected var mProgressDialog: Dialog? = null
    //触摸进度条的progress
    protected var mDialogProgressBar: ProgressBar? = null
    //音量进度条的progress
    protected var mDialogVolumeProgressBar: ProgressBar? = null
    //亮度文本
    protected var mBrightnessDialogTv: TextView? = null
    //触摸移动显示文本
    protected var mDialogSeekTime: TextView? = null
    //触摸移动显示全部时间
    protected var mDialogTotalTime: TextView? = null
    //触摸移动方向icon
    protected var mDialogIcon: ImageView? = null
    //控件图片/颜色资源设置
    protected var mBottomProgressDrawable: Drawable? = null
    protected var mBottomShowProgressDrawable: Drawable? = null
    protected var mBottomShowProgressThumbDrawable: Drawable? = null
    protected var mVolumeProgressDrawable: Drawable? = null
    protected var mDialogProgressBarDrawable: Drawable? = null
    protected var mDialogProgressHighLightColor = -11
    protected var mDialogProgressNormalColor = -11

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    override fun init(context: Context?) {
        super.init(context)
        //增加自定义ui
        if (mBottomProgressDrawable != null) {
            mBottomProgressBar.progressDrawable = mBottomProgressDrawable
        }
        if (mBottomShowProgressDrawable != null) {
            mProgressBar.progressDrawable = mBottomProgressDrawable
        }
        if (mBottomShowProgressThumbDrawable != null) {
            mProgressBar.thumb = mBottomShowProgressThumbDrawable
        }
    }

    /**
     * 继承后重写可替换为你需要的布局
     *
     * @return
     */
    override fun getLayoutId(): Int {
        return R.layout.video_layout_standard
    }

    /**
     * 开始播放
     */
    override fun startPlayLogic() {
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb")
            mVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, this@StandardGSYVideoPlayer)
        }
        prepareVideo()
        startDismissControlViewTimer()
    }

    /**
     * 显示wifi确定框，如需要自定义继承重写即可
     */
    override fun showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            //Toast.makeText(mContext, getResources().getString(R.string.no_net), Toast.LENGTH_LONG).show();
            startPlayLogic()
            return
        }
        val builder = AlertDialog.Builder(activityContext)
        builder.setMessage(resources.getString(R.string.tips_not_wifi))
        builder.setPositiveButton(resources.getString(R.string.tips_not_wifi_confirm)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            startPlayLogic()
        }
        builder.setNegativeButton(resources.getString(R.string.tips_not_wifi_cancel)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    /**
     * 触摸显示滑动进度dialog，如需要自定义继承重写即可，记得重写dismissProgressDialog
     */
    override fun showProgressDialog(deltaX: Float, seekTime: String?, seekTimePosition: Long, totalTime: String?, totalTimeDuration: Long) {
        if (mProgressDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(getProgressDialogLayoutId(), null)
            if (localView.findViewById<View>(getProgressDialogProgressId()) is ProgressBar) {
                mDialogProgressBar = localView.findViewById(getProgressDialogProgressId())
                if (mDialogProgressBarDrawable != null) {
                    mDialogProgressBar?.progressDrawable = mDialogProgressBarDrawable
                }
            }
            if (localView.findViewById<View>(getProgressDialogCurrentDurationTextId()) is TextView) {
                mDialogSeekTime = localView.findViewById(getProgressDialogCurrentDurationTextId())
            }
            if (localView.findViewById<View>(getProgressDialogAllDurationTextId()) is TextView) {
                mDialogTotalTime = localView.findViewById(getProgressDialogAllDurationTextId())
            }
            if (localView.findViewById<View>(getProgressDialogImageId()) is ImageView) {
                mDialogIcon = localView.findViewById(getProgressDialogImageId())
            }
            mProgressDialog = Dialog(activityContext, R.style.video_style_dialog_progress)
            mProgressDialog?.setContentView(localView)
            mProgressDialog?.window?.addFlags(Window.FEATURE_ACTION_BAR)
            mProgressDialog?.window?.addFlags(32)
            mProgressDialog?.window?.addFlags(16)
            mProgressDialog?.window?.setLayout(width, height)
            if (mDialogProgressNormalColor != -11 && mDialogTotalTime != null) {
                mDialogTotalTime?.setTextColor(mDialogProgressNormalColor)
            }
            if (mDialogProgressHighLightColor != -11 && mDialogSeekTime != null) {
                mDialogSeekTime?.setTextColor(mDialogProgressHighLightColor)
            }
            val localLayoutParams = mProgressDialog?.window?.attributes
            localLayoutParams?.gravity = Gravity.TOP
            localLayoutParams?.width = width
            localLayoutParams?.height = height
            val location = IntArray(2)
            getLocationOnScreen(location)
            localLayoutParams?.x = location[0]
            localLayoutParams?.y = location[1]
            mProgressDialog?.window?.attributes = localLayoutParams
        }
        if (!mProgressDialog?.isShowing.orFalse) {
            mProgressDialog?.show()
        }
        if (mDialogSeekTime != null) {
            mDialogSeekTime?.text = seekTime
        }
        if (mDialogTotalTime != null) {
            mDialogTotalTime?.text = " / $totalTime"
        }
        if (totalTimeDuration > 0) if (mDialogProgressBar != null) {
            mDialogProgressBar?.progress = (seekTimePosition * 100 / totalTimeDuration).toInt()
        }
        if (deltaX > 0) {
            if (mDialogIcon != null) {
                mDialogIcon?.setBackgroundResource(R.drawable.video_forward_icon)
            }
        } else {
            if (mDialogIcon != null) {
                mDialogIcon?.setBackgroundResource(R.drawable.video_backward_icon)
            }
        }
    }

    override fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }

    /**
     * 触摸音量dialog，如需要自定义继承重写即可，记得重写dismissVolumeDialog
     */
    override fun showVolumeDialog(deltaY: Float, volumePercent: Int) {
        if (mVolumeDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(getVolumeLayoutId(), null)
            if (localView.findViewById<View>(getVolumeProgressId()) is ProgressBar) {
                mDialogVolumeProgressBar = localView.findViewById(getVolumeProgressId())
                if (mVolumeProgressDrawable != null && mDialogVolumeProgressBar != null) {
                    mDialogVolumeProgressBar?.progressDrawable = mVolumeProgressDrawable
                }
            }
            mVolumeDialog = Dialog(activityContext, R.style.video_style_dialog_progress)
            mVolumeDialog?.setContentView(localView)
            mVolumeDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            mVolumeDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            mVolumeDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mVolumeDialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val localLayoutParams = mVolumeDialog?.window?.attributes
            localLayoutParams?.gravity = Gravity.TOP or Gravity.START
            localLayoutParams?.width = width
            localLayoutParams?.height = height
            val location = IntArray(2)
            getLocationOnScreen(location)
            localLayoutParams?.x = location[0]
            localLayoutParams?.y = location[1]
            mVolumeDialog?.window?.attributes = localLayoutParams
        }
        if (!mVolumeDialog?.isShowing.orFalse) {
            mVolumeDialog?.show()
        }
        if (mDialogVolumeProgressBar != null) {
            mDialogVolumeProgressBar?.progress = volumePercent
        }
    }

    override fun dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog?.dismiss()
            mVolumeDialog = null
        }
    }

    /**
     * 触摸亮度dialog，如需要自定义继承重写即可，记得重写dismissBrightnessDialog
     */
    override fun showBrightnessDialog(percent: Float) {
        if (mBrightnessDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(getBrightnessLayoutId(), null)
            if (localView.findViewById<View>(getBrightnessTextId()) is TextView) {
                mBrightnessDialogTv = localView.findViewById(getBrightnessTextId())
            }
            mBrightnessDialog = Dialog(activityContext, R.style.video_style_dialog_progress)
            mBrightnessDialog?.setContentView(localView)
            mBrightnessDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            mBrightnessDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            mBrightnessDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mBrightnessDialog?.window?.decorView?.systemUiVisibility = SYSTEM_UI_FLAG_HIDE_NAVIGATION
            mBrightnessDialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val localLayoutParams = mBrightnessDialog?.window?.attributes
            localLayoutParams?.gravity = Gravity.TOP or Gravity.END
            localLayoutParams?.width = width
            localLayoutParams?.height = height
            val location = IntArray(2)
            getLocationOnScreen(location)
            localLayoutParams?.x = location[0]
            localLayoutParams?.y = location[1]
            mBrightnessDialog?.window?.attributes = localLayoutParams
        }
        if (!mBrightnessDialog?.isShowing.orFalse) {
            mBrightnessDialog?.show()
        }
        if (mBrightnessDialogTv != null) mBrightnessDialogTv?.text = "${(percent * 100).toInt()}%"
    }

    override fun dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog?.dismiss()
            mBrightnessDialog = null
        }
    }

    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        val sf = from as? StandardGSYVideoPlayer
        val st = to as? StandardGSYVideoPlayer
        if (st?.mProgressBar != null && sf?.mProgressBar != null) {
            st.mProgressBar.progress = sf.mProgressBar.progress
            st.mProgressBar.secondaryProgress = sf.mProgressBar.secondaryProgress
        }
        if (st?.mTotalTimeTextView != null && sf?.mTotalTimeTextView != null) {
            st.mTotalTimeTextView.text = sf.mTotalTimeTextView.text
        }
        if (st?.mCurrentTimeTextView != null && sf?.mCurrentTimeTextView != null) {
            st.mCurrentTimeTextView.text = sf.mCurrentTimeTextView.text
        }
    }

    /**
     * 将自定义的效果也设置到全屏
     *
     * @param context
     * @param actionBar 是否有actionBar，有的话需要隐藏
     * @param statusBar 是否有状态bar，有的话需要隐藏
     * @return
     */
    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer? {
        val gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar)
        if (gsyBaseVideoPlayer != null) {
            val gsyVideoPlayer = gsyBaseVideoPlayer as? StandardGSYVideoPlayer
            gsyVideoPlayer?.setLockClickListener(mLockClickListener)
            gsyVideoPlayer?.isNeedLockFull = isNeedLockFull
            initFullUI(gsyVideoPlayer)
            //比如你自定义了返回案件，但是因为返回按键底层已经设置了返回事件，所以你需要在这里重新增加的逻辑
        }
        return gsyBaseVideoPlayer
    }

    /********************************各类UI的状态显示*********************************************/

    /**
     * 点击触摸显示和隐藏逻辑
     */
    override fun onClickUiToggle(e: MotionEvent?) {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }
        if (mIfCurrentIsFullscreen && !mSurfaceErrorPlay && mCurrentState == CURRENT_STATE_ERROR) {
            if (mBottomContainer != null) {
                if (mBottomContainer.visibility == VISIBLE) {
                    changeUiToPlayingClear()
                } else {
                    changeUiToPlayingShow()
                }
            }
        } else if (mCurrentState == CURRENT_STATE_PREPAREING) {
            if (mBottomContainer != null) {
                if (mBottomContainer.visibility == VISIBLE) {
                    changeUiToPrepareingClear()
                } else {
                    changeUiToPreparingShow()
                }
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING) {
            if (mBottomContainer != null) {
                if (mBottomContainer.visibility == VISIBLE) {
                    changeUiToPlayingClear()
                } else {
                    changeUiToPlayingShow()
                }
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mBottomContainer != null) {
                if (mBottomContainer.visibility == VISIBLE) {
                    changeUiToPauseClear()
                } else {
                    changeUiToPauseShow()
                }
            }
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (mBottomContainer != null) {
                if (mBottomContainer.visibility == VISIBLE) {
                    changeUiToCompleteClear()
                } else {
                    changeUiToCompleteShow()
                }
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (mBottomContainer != null) {
                if (mBottomContainer.visibility == VISIBLE) {
                    changeUiToPlayingBufferingClear()
                } else {
                    changeUiToPlayingBufferingShow()
                }
            }
        }
    }

    override fun hideAllWidget() {
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mTopContainer, INVISIBLE)
        setViewShowState(mBottomProgressBar, VISIBLE)
        setViewShowState(mStartButton, INVISIBLE)
    }

    override fun changeUiToNormal() {
        Debuger.printfLog("changeUiToNormal")
        setViewShowState(mTopContainer, VISIBLE)
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mStartButton, VISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, VISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, if ((mIfCurrentIsFullscreen && mNeedLockFull)) VISIBLE else GONE)
        updateStartImage()
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
    }

    override fun changeUiToPreparingShow() {
        Debuger.printfLog("changeUiToPreparingShow")
        setViewShowState(mTopContainer, VISIBLE)
        setViewShowState(mBottomContainer, VISIBLE)
        setViewShowState(mStartButton, INVISIBLE)
        setViewShowState(mLoadingProgressBar, VISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            val progressBar = mLoadingProgressBar as? ENDownloadView
            if (progressBar?.currentState == ENDownloadView.STATE_PRE) {
                progressBar.start()
            }
        }
    }

    override fun changeUiToPlayingShow() {
        Debuger.printfLog("changeUiToPlayingShow")
        if (mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }
        setViewShowState(mTopContainer, VISIBLE)
        setViewShowState(mBottomContainer, VISIBLE)
        setViewShowState(mStartButton, VISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, if ((mIfCurrentIsFullscreen && mNeedLockFull)) VISIBLE else GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
        updateStartImage()
    }

    override fun changeUiToPauseShow() {
        Debuger.printfLog("changeUiToPauseShow")
        if (mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }
        setViewShowState(mTopContainer, VISIBLE)
        setViewShowState(mBottomContainer, VISIBLE)
        setViewShowState(mStartButton, VISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, if ((mIfCurrentIsFullscreen && mNeedLockFull)) VISIBLE else GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
        updateStartImage()
        updatePauseCover()
    }

    override fun changeUiToPlayingBufferingShow() {
        Debuger.printfLog("changeUiToPlayingBufferingShow")
        setViewShowState(mTopContainer, VISIBLE)
        setViewShowState(mBottomContainer, VISIBLE)
        setViewShowState(mStartButton, INVISIBLE)
        setViewShowState(mLoadingProgressBar, VISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            val progressBar = mLoadingProgressBar as? ENDownloadView
            if (progressBar?.currentState == ENDownloadView.STATE_PRE) {
                progressBar.start()
            }
        }
    }

    override fun changeUiToCompleteShow() {
        Debuger.printfLog("changeUiToCompleteShow")
        setViewShowState(mTopContainer, VISIBLE)
        setViewShowState(mBottomContainer, VISIBLE)
        setViewShowState(mStartButton, VISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, VISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, if ((mIfCurrentIsFullscreen && mNeedLockFull)) VISIBLE else GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
        updateStartImage()
    }

    override fun changeUiToError() {
        Debuger.printfLog("changeUiToError")
        setViewShowState(mTopContainer, INVISIBLE)
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mStartButton, VISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, if ((mIfCurrentIsFullscreen && mNeedLockFull)) VISIBLE else GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
        updateStartImage()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dismissVolumeDialog()
        dismissBrightnessDialog()
    }

    /**
     * 触摸进度dialog的layoutId
     * 继承后重写可返回自定义
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected open fun getProgressDialogLayoutId(): Int {
        return R.layout.video_progress_dialog
    }

    /**
     * 触摸进度dialog的进度条id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected open fun getProgressDialogProgressId(): Int {
        return R.id.duration_progressbar
    }

    /**
     * 触摸进度dialog的当前时间文本
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected open fun getProgressDialogCurrentDurationTextId(): Int {
        return R.id.tv_current
    }

    /**
     * 触摸进度dialog全部时间文本
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected open fun getProgressDialogAllDurationTextId(): Int {
        return R.id.tv_duration
    }

    /**
     * 触摸进度dialog的图片id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected open fun getProgressDialogImageId(): Int {
        return R.id.duration_image_tip
    }

    /**
     * 音量dialog的layoutId
     * 继承后重写可返回自定义
     * 有自定义的实现逻辑可重载showVolumeDialog方法
     */
    protected open fun getVolumeLayoutId(): Int {
        return R.layout.video_volume_dialog
    }

    /**
     * 音量dialog的百分比进度条 id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showVolumeDialog方法
     */
    protected open fun getVolumeProgressId(): Int {
        return R.id.volume_progressbar
    }

    /**
     * 亮度dialog的layoutId
     * 继承后重写可返回自定义
     * 有自定义的实现逻辑可重载showBrightnessDialog方法
     */
    protected open fun getBrightnessLayoutId(): Int {
        return R.layout.video_brightness
    }

    /**
     * 亮度dialog的百分比text id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showBrightnessDialog方法
     */
    protected open fun getBrightnessTextId(): Int {
        return R.id.app_video_brightness
    }

    protected open fun changeUiToPrepareingClear() {
        Debuger.printfLog("changeUiToPrepareingClear")
        setViewShowState(mTopContainer, INVISIBLE)
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mStartButton, INVISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
    }

    protected open fun changeUiToPlayingClear() {
        Debuger.printfLog("changeUiToPlayingClear")
        changeUiToClear()
        setViewShowState(mBottomProgressBar, VISIBLE)
    }

    protected open fun changeUiToPauseClear() {
        Debuger.printfLog("changeUiToPauseClear")
        changeUiToClear()
        setViewShowState(mBottomProgressBar, VISIBLE)
        updatePauseCover()
    }

    protected open fun changeUiToPlayingBufferingClear() {
        Debuger.printfLog("changeUiToPlayingBufferingClear")
        setViewShowState(mTopContainer, INVISIBLE)
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mStartButton, INVISIBLE)
        setViewShowState(mLoadingProgressBar, VISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, VISIBLE)
        setViewShowState(mLockScreen, GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            val progressBar = mLoadingProgressBar as? ENDownloadView
            if (progressBar?.currentState == ENDownloadView.STATE_PRE) {
                progressBar.start()
            }
        }
        updateStartImage()
    }

    protected open fun changeUiToClear() {
        Debuger.printfLog("changeUiToClear")
        setViewShowState(mTopContainer, INVISIBLE)
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mStartButton, INVISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, INVISIBLE)
        setViewShowState(mBottomProgressBar, INVISIBLE)
        setViewShowState(mLockScreen, GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
    }

    protected open fun changeUiToCompleteClear() {
        Debuger.printfLog("changeUiToCompleteClear")
        setViewShowState(mTopContainer, INVISIBLE)
        setViewShowState(mBottomContainer, INVISIBLE)
        setViewShowState(mStartButton, VISIBLE)
        setViewShowState(mLoadingProgressBar, INVISIBLE)
        setViewShowState(mThumbImageViewLayout, VISIBLE)
        setViewShowState(mBottomProgressBar, VISIBLE)
        setViewShowState(mLockScreen, if ((mIfCurrentIsFullscreen && mNeedLockFull)) VISIBLE else GONE)
        if (mLoadingProgressBar is ENDownloadView) {
            (mLoadingProgressBar as? ENDownloadView)?.reset()
        }
        updateStartImage()
    }

    /**
     * 定义开始按键显示
     */
    protected open fun updateStartImage() {
        if (mStartButton is ENPlayView) {
            val button = mStartButton as? ENPlayView
            button?.setDuration(500)
            when (mCurrentState) {
                CURRENT_STATE_PLAYING -> button?.play()
                CURRENT_STATE_ERROR -> button?.pause()
                else -> button?.pause()
            }
        } else if (mStartButton is ImageView) {
            val image = mStartButton as? ImageView
            when (mCurrentState) {
                CURRENT_STATE_PLAYING -> image?.setImageResource(R.drawable.video_click_pause_selector)
                CURRENT_STATE_ERROR -> image?.setImageResource(R.drawable.video_click_error_selector)
                else -> image?.setImageResource(R.drawable.video_click_play_selector)
            }
        }
    }

    /**
     * 全屏的UI逻辑
     */
    private fun initFullUI(standardGSYVideoPlayer: StandardGSYVideoPlayer?) {
        if (mBottomProgressDrawable != null) {
            standardGSYVideoPlayer?.setBottomProgressBarDrawable(mBottomProgressDrawable)
        }
        if (mBottomShowProgressDrawable != null && mBottomShowProgressThumbDrawable != null) {
            standardGSYVideoPlayer?.setBottomShowProgressBarDrawable(mBottomShowProgressDrawable, mBottomShowProgressThumbDrawable)
        }
        if (mVolumeProgressDrawable != null) {
            standardGSYVideoPlayer?.setDialogVolumeProgressBar(mVolumeProgressDrawable)
        }
        if (mDialogProgressBarDrawable != null) {
            standardGSYVideoPlayer?.setDialogProgressBar(mDialogProgressBarDrawable)
        }
        if (mDialogProgressHighLightColor != -11 && mDialogProgressNormalColor != -11) {
            standardGSYVideoPlayer?.setDialogProgressColor(mDialogProgressHighLightColor, mDialogProgressNormalColor)
        }
    }

    /**
     * 底部进度条-弹出的
     */
    fun setBottomShowProgressBarDrawable(drawable: Drawable?, thumb: Drawable?) {
        mBottomShowProgressDrawable = drawable
        mBottomShowProgressThumbDrawable = thumb
        if (mProgressBar != null) {
            mProgressBar.progressDrawable = drawable
            mProgressBar.thumb = thumb
        }
    }

    /**
     * 底部进度条-非弹出
     */
    fun setBottomProgressBarDrawable(drawable: Drawable?) {
        mBottomProgressDrawable = drawable
        if (mBottomProgressBar != null) {
            mBottomProgressBar.progressDrawable = drawable
        }
    }

    /**
     * 声音进度条
     */
    fun setDialogVolumeProgressBar(drawable: Drawable?) {
        mVolumeProgressDrawable = drawable
    }

    /**
     * 中间进度条
     */
    fun setDialogProgressBar(drawable: Drawable?) {
        mDialogProgressBarDrawable = drawable
    }

    /**
     * 中间进度条字体颜色
     */
    fun setDialogProgressColor(highLightColor: Int?, normalColor: Int?) {
        mDialogProgressHighLightColor = highLightColor.orZero
        mDialogProgressNormalColor = normalColor.orZero
    }

    /************************************* 关于截图的 ****************************************/

    /**
     * 获取截图
     */
    fun taskShotPic(gsyVideoShotListener: GSYVideoShotListener?) {
        this.taskShotPic(gsyVideoShotListener, false)
    }

    /**
     * 获取截图
     *
     * @param high 是否需要高清的
     */
    fun taskShotPic(gsyVideoShotListener: GSYVideoShotListener?, high: Boolean) {
        if (currentPlayer.renderProxy != null) {
            currentPlayer.renderProxy.taskShotPic(gsyVideoShotListener, high)
        }
    }

    /**
     * 保存截图
     */
    fun saveFrame(file: File?, gsyVideoShotSaveListener: GSYVideoShotSaveListener?) {
        saveFrame(file, false, gsyVideoShotSaveListener)
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    fun saveFrame(file: File?, high: Boolean, gsyVideoShotSaveListener: GSYVideoShotSaveListener?) {
        if (currentPlayer.renderProxy != null) {
            currentPlayer.renderProxy.saveFrame(file, high, gsyVideoShotSaveListener)
        }
    }

    /**
     * 重新开启进度查询以及控制view消失的定时任务
     * 用于解决GSYVideoHelper中通过removeview方式做全屏切换导致的定时任务停止的问题
     * GSYVideoControlView   onDetachedFromWindow（）
     */
    fun restartTimerTask() {
        startProgressTimer()
        startDismissControlViewTimer()
    }

}