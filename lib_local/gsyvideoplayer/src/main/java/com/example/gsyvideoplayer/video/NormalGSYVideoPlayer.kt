package com.example.gsyvideoplayer.video

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.shuyu.gsyvideoplayer.R

/**
 * Created by guoshuyu on 2017/4/1.
 * 使用正常播放按键和loading的播放器
 */
class NormalGSYVideoPlayer : StandardGSYVideoPlayer {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    override fun getLayoutId(): Int {
        return R.layout.video_layout_normal
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

}