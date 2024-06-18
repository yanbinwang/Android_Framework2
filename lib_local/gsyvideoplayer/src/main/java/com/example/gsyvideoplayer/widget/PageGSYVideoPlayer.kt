package com.example.gsyvideoplayer.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.fade
import com.example.gsyvideoplayer.R
import com.example.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.utils.NetworkUtils

/**
 * 自定义列表播放器
 * 1.项目主要修改的是gsy播放器的容器
 * 2.video文件夹下的播放器不要做改动
 */
class PageGSYVideoPlayer : StandardGSYVideoPlayer {

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
     * wifi弹框
     */
    override fun showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            startPlayLogic()
            return
        }
        val builder = AlertDialog.Builder(activityContext)
        builder.setMessage(resources.getString(R.string.tips_not_wifi))
        builder.setPositiveButton(resources.getString(R.string.tips_not_wifi_confirm)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            startPlayLogic()
        }
        builder.setNegativeButton(resources.getString(R.string.tips_not_wifi_cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.create().show()
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