package com.example.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

class SmartVideoPlayer : StandardGSYVideoPlayer {
    // 当前是否处于画中画模式
    var isInPipMode = false

//    init {
//        //加载中配置
//        (mLoadingProgressBar as? ProgressBar)?.indeterminateDrawable = context.drawable(R.drawable.layer_list_loading)
//    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

//    override fun getLayoutId(): Int {
//        return R.layout.view_gsyvideo_normal
//    }
//
//    /**
//     * 自定义自己的开始/暂停按钮
//     */
//    override fun updateStartImage() {
//        if (mStartButton is ImageView) {
//            val imageView = mStartButton as? ImageView
//            when (mCurrentState) {
//                CURRENT_STATE_PLAYING -> imageView?.setImageResource(R.drawable.video_click_pause_selector)
//                CURRENT_STATE_ERROR -> imageView?.setImageResource(R.drawable.video_click_play_selector)
//                else -> imageView?.setImageResource(R.drawable.video_click_play_selector)
//            }
//        }
//    }

    /**
     * 准备阶段
     */
    override fun changeUiToPrepareingClear() {
        if (isInPipMode) {
            changeUiToClear()
        } else {
            super.changeUiToPrepareingClear()
        }
    }

    /**
     * 播放阶段
     */
    override fun changeUiToPlayingClear() {
        if (isInPipMode) {
            changeUiToClear()
        } else {
            super.changeUiToPlayingClear()
        }
    }

    /**
     * 缓冲阶段
     */
    override fun changeUiToPlayingBufferingClear() {
        if (isInPipMode) {
            changeUiToClear()
        } else {
            super.changeUiToPlayingBufferingClear()
        }
    }

    /**
     * 暂停阶段
     */
    override fun changeUiToPauseClear() {
        if (isInPipMode) {
            changeUiToClear()
        } else {
            super.changeUiToPauseClear()
        }
    }

    /**
     * 完成阶段
     */
    override fun changeUiToCompleteClear() {
        if (isInPipMode) {
            changeUiToClear()
        } else {
            super.changeUiToCompleteClear()
        }
    }

    /**
     * 完全隐藏
     */
    override fun changeUiToClear() {
        super.changeUiToClear()
    }

}