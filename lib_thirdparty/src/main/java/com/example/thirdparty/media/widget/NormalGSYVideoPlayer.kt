package com.example.thirdparty.media.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.framework.utils.function.drawable
import com.example.thirdparty.R
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

class NormalGSYVideoPlayer : StandardGSYVideoPlayer {

    init {
        //加载中配置
        (mLoadingProgressBar as? ProgressBar)?.indeterminateDrawable = context.drawable(R.drawable.layer_list_loading)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    override fun getLayoutId(): Int {
        return R.layout.view_gsyvideo_normal
    }

    /**
     * 自定义自己的开始/暂停按钮
     */
    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            val imageView = mStartButton as? ImageView
            when (mCurrentState) {
                CURRENT_STATE_PLAYING -> imageView?.setImageResource(R.drawable.video_click_pause_selector)
                CURRENT_STATE_ERROR -> imageView?.setImageResource(R.drawable.video_click_play_selector)
                else -> imageView?.setImageResource(R.drawable.video_click_play_selector)
            }
        }
    }

//    /**
//     * 准备阶段
//     */
//    override fun changeUiToPrepareingClear() {
//        super.changeUiToPrepareingClear()
//        changeUiToClear()
//    }
//
//    /**
//     * 播放阶段
//     */
//    override fun changeUiToPlayingClear() {
//        super.changeUiToPlayingClear()
//        changeUiToClear()
//    }
//
//    /**
//     * 缓冲阶段
//     */
//    override fun changeUiToPlayingBufferingClear() {
//        super.changeUiToPlayingBufferingClear()
//        changeUiToClear()
//    }
//
//    /**
//     * 暂停阶段
//     */
//    override fun changeUiToPauseClear() {
//        super.changeUiToPauseClear()
//        changeUiToClear()
//    }
//
//    /**
//     * 完成阶段
//     */
//    override fun changeUiToCompleteClear() {
//        super.changeUiToCompleteClear()
//        changeUiToClear()
//    }
//
//    /**
//     * 完全隐藏
//     */
//    override fun changeUiToClear() {
//        super.changeUiToClear()
//    }

}