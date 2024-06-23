package com.example.gsyvideoplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.example.common.utils.function.string
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.fade
import com.example.gsyvideoplayer.R
import com.example.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.utils.NetworkUtils

/**
 * 使用正常播放按键和loading的播放器
 * 适用于viewpager2上下滑动的列表播放器
 * 1.项目主要修改的是gsy播放器的容器
 * 2.部分改动资源可以本地覆写
 * 3.和依赖项目中自定义的播放器有个区分（为便于修改，故而依赖项目的播放器文件放置于video目录下）
 */
class PageGSYVideoPlayer : StandardGSYVideoPlayer {
    private val dialog by lazy { AppDialog(mContext) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    /**
     * 重绘为定制的layout，id命名不要改
     */
    override fun getLayoutId() = R.layout.view_gsyvideo_page

    /**
     * 更新播放按钮
     */
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
     * 使底部菜单在加载视频的时候处于隐藏状态
     */
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        setViewShowState(mBottomContainer, INVISIBLE)
    }

    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        setViewShowState(mBottomContainer, INVISIBLE)
    }

    /**
     * wifi弹框自定义
     */
    override fun showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            startPlayLogic()
            return
        }
        dialog.setParams(message = string(R.string.gsyNotWifiTips), positiveText = string(R.string.gsyNotWifiConfirm), negativeText = string(R.string.gsyNotWifiCancel))
            .setDialogListener({ startPlayLogic() })
            .show()
    }

    /**
     * 给遮罩页面加动画，使加载平滑
     */
    override fun setViewShowState(view: View?, visibility: Int) {
        if (view == mThumbImageViewLayout) {
            if (visibility == VISIBLE) {
                mThumbImageViewLayout.appear()
            } else {
                mThumbImageViewLayout.fade()
            }
        } else {
            super.setViewShowState(view, visibility)
        }
    }

}