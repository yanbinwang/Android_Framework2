package com.example.mvvm.activity

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.common.utils.builder.toast
import com.example.common.utils.function.getBroadcastPendingIntent
import com.example.framework.utils.function.doOnReceiver
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityScreenBinding
import com.example.thirdparty.media.utils.gsyvideoplayer.GSYVideoHelper
import com.example.thirdparty.media.utils.gsyvideoplayer.OnGSYVideoPlayerListener
import com.example.thirdparty.utils.NotificationUtil.NOTIFY_ID_PIP_PLAY
import com.example.thirdparty.utils.NotificationUtil.NOTIFY_ID_PIP_STOP
import com.example.thirdparty.utils.NotificationUtil.requestCode
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START
import com.therouter.router.Route

/**
 * 画中画 (仅8.0+支持)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Route(path = RouterPath.ScreenActivity)
class ScreenActivity : BaseActivity<ActivityScreenBinding>() {
    // 是否是小窗
    private var isInPip = false
    private val playPending by lazy { getBroadcastPendingIntent(NOTIFY_ID_PIP_PLAY, Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT) }
    private val pausePending by lazy { getBroadcastPendingIntent(NOTIFY_ID_PIP_STOP, Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT) }
    private val gsyHelper by lazy { GSYVideoHelper(this) }

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    // 接收按钮点击事件
    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> {
                    "播放".toast()
                    if (mBinding?.gsyPlayer?.isInPlayingState.orFalse) {
                        mBinding?.gsyPlayer?.onVideoResume(true)
                    } else {
                        gsyHelper.startPlayLogic()
                    }
                    forceHideAllWidget()
                    updatePipActions(true)
                }
                ACTION_PAUSE -> {
                    "暂停".toast()
                    gsyHelper.onVideoPause()
                    forceHideAllWidget()
                    updatePipActions(false)
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 注册画中画按钮点击事件广播
        doOnReceiver(this, pipReceiver, IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
        })
        gsyHelper.bind(mBinding?.gsyPlayer, showFullScreen = true)
        gsyHelper.setUrl("https://stream7.iqilu.com/10339/upload_transcode/202002/09/20200209105011F0zPoYzHry.mp4")
    }

    override fun initEvent() {
        super.initEvent()
        mBinding?.tvStart.click {
            enterPipMode()
        }
        gsyHelper.setOnGSYVideoPlayerListener(object : OnGSYVideoPlayerListener {
            override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                super.onQuitFullscreen(url, *objects)
                initImmersionBar()
            }

            override fun onPlayError(url: String?, vararg objects: Any?) {
                super.onPlayError(url, *objects)
                updatePipActions(false)
            }

            override fun onComplete(url: String?, vararg objects: Any?) {
                super.onComplete(url, *objects)
                updatePipActions(false)
            }

            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                super.onAutoComplete(url, *objects)
                updatePipActions(false)
            }
        })
    }

    /**
     * 进入画中画模式 核心方法
     * 1. 点击小窗本体 → 回到全屏
     * 触发 onPictureInPictureModeChanged(isInPip = false, ...)
     * 同时 Activity 会走 onPictureInPictureModeChanged 然后走 onResume()
     * 这是关键区分点
     * 2. 点击关闭按钮（X）
     * 触发 onPictureInPictureModeChanged(isInPip = false, ...)
     * 不会走 onResume()，而是直接走 onStop(),然后走 onPictureInPictureModeChanged
     * 说明用户不想回来了
     */
    private fun enterPipMode() {
        // 设置画中画窗口
        val params = PictureInPictureParams.Builder()
            // 宽高比 比如 16:9 / 4:3
            .setAspectRatio(Rational(16, 9))
            // 底部两个按钮
//            .setActions(listOf(playAction, pauseAction))
            .setActions(listOf(getPipAction(false)))
            .build()
        // 进入画中画
        enterPictureInPictureMode(params)
    }

    // 每次状态变化时，重新构建 action 并更新 PiP 参数
    private fun updatePipActions(isPause: Boolean) {
        if (!isInPip) return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(listOf(getPipAction(isPause)))
            .build()
        // 用这个API动态更新，不需要重新进入PiP
        setPictureInPictureParams(params)
    }

    private fun getPipAction(isPause: Boolean):RemoteAction {
        return if (isPause) {
            // 暂停按钮
            RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_pause), "暂停", "暂停播放", pausePending)
        } else {
            // 播放按钮
            RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_play), "播放", "继续播放", playPending)
        }
    }

    override fun onResume() {
        super.onResume()
//        // 进页面直接小窗
//        window.decorView.post {
//            enterPipMode()
//        }
        "onResume".logWTF("wyb")
    }

    override fun onPause() {
        super.onPause()
        "onPause".logWTF("wyb")
    }

    override fun onStop() {
        super.onStop()
        "onStop".logWTF("wyb")
        if (isInPip) {
            finish()
        }
    }

    // 监听进入/退出画中画状态
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        "onPipChanged: isInPip=$isInPictureInPictureMode".logWTF("wyb")
        isInPip = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            // 进入小窗：隐藏播放控制器、标题栏、冗余UI，只留画面
            mBinding?.tvStart.gone()
            forceHideAllWidget()
            resetPlaying()
        } else {
            mBinding?.tvStart.visible()
            forceChangeUiToNormal()
            resetPlaying()
        }
    }

    private fun forceHideAllWidget() {
        mBinding?.gsyPlayer?.dismissControlTime = 0
        try {
            val method = StandardGSYVideoPlayer::class.java.getDeclaredMethod("hideAllWidget")
            method.isAccessible = true
            method.invoke(mBinding?.gsyPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun forceChangeUiToNormal() {
        mBinding?.gsyPlayer?.dismissControlTime = 2500
        try {
            val method = StandardGSYVideoPlayer::class.java.getDeclaredMethod("changeUiToNormal")
            method.isAccessible = true
            method.invoke(mBinding?.gsyPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetPlaying() {
        val currentPosition = mBinding?.gsyPlayer?.currentPositionWhenPlaying.orZero
        if (mBinding?.gsyPlayer?.isInPlayingState.orFalse) {
//                mBinding?.gsyPlayer?.seekOnStart = currentPosition
//                mBinding?.gsyPlayer?.startPlayLogic()
            mBinding?.gsyPlayer?.seekTo(currentPosition)
        }
    }

}