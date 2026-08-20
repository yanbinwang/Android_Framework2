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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.RequiresApi
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.safeAs
import com.example.common.network.repository.withHandling
import com.example.common.utils.ScreenUtil
import com.example.common.utils.function.getBroadcastPendingIntent
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.framework.utils.function.doOnReceiver
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityScreenBinding
import com.example.thirdparty.media.utils.getPipAspectRatio
import com.example.thirdparty.media.utils.gsyvideoplayer.GSYVideoHelper
import com.example.thirdparty.media.utils.gsyvideoplayer.OnGSYVideoPlayerListener
import com.example.thirdparty.media.utils.suspendingCalculateHeight
import com.example.thirdparty.utils.NotificationUtil.NOTIFY_ID_PIP_PLAY
import com.example.thirdparty.utils.NotificationUtil.NOTIFY_ID_PIP_STOP
import com.therouter.router.Route
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * 画中画 (仅8.0+支持)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Route(path = RouterPath.ScreenActivity)
class ScreenActivity : BaseActivity<ActivityScreenBinding>() {
    // 是否是小窗
    private var isPipMode = false
    // 是否点击小窗关闭
    private var isPipClose = false
    // 小窗按钮
    private val playPending by lazy { getBroadcastPendingIntent(NOTIFY_ID_PIP_PLAY, Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT) }
    private val pausePending by lazy { getBroadcastPendingIntent(NOTIFY_ID_PIP_STOP, Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT) }
    // 播放器帮助类
    private val gsyHelper by lazy { GSYVideoHelper(this, false, false) }
    // 播放地址
    private val videoUrl = "https://stream7.iqilu.com/10339/upload_transcode/202002/09/20200209105011F0zPoYzHry.mp4"
    private val statusBarHeight = getStatusBarHeight()
    private var videoHeight = 280.pt
    // 接收按钮点击事件
    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                // 播放
                ACTION_PLAY -> {
                    gsyHelper.resumeOrRestart()
                    mBinding?.gsyPlayer?.changeUiToPip()
                    updatePipActions(true)
                }
                // 暂停
                ACTION_PAUSE -> {
                    gsyHelper.onVideoPause()
                    mBinding?.gsyPlayer?.changeUiToPip()
                    updatePipActions(false)
                }
            }
        }
    }

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 注册画中画按钮点击事件广播
        doOnReceiver(this, pipReceiver, IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
        })
        gsyHelper.bind(mBinding?.gsyPlayer, showFullScreen = true)
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

    override fun initData() {
        super.initData()
        launch {
            flow {
                emit(requestAffair { suspendingCalculateHeight(this@ScreenActivity, videoUrl) })
            }.withHandling({
                mBinding?.gsyPlayer.size(height = videoHeight)
            }, {
                gsyHelper.setUrl(videoUrl)
            }).collect {
                videoHeight = it
                val ratio = getPipAspectRatio(ScreenUtil.screenWidth, it)
                "高度:${it}\n宽度:${ScreenUtil.screenWidth}\n比率:${ratio.first}:${ratio.second}".logWTF("wyb")
                mBinding?.gsyPlayer.size(height = it)
            }
        }
    }

    /**
     * 监听进入/退出画中画状态
     */
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
        mBinding?.gsyPlayer?.isInPipMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            // 进入小窗：隐藏播放控制器、标题栏、冗余UI，只留画面
            mBinding?.tvStart.gone()
            mBinding?.gsyPlayer?.margin(top = 0)
            mBinding?.gsyPlayer?.size(height = MATCH_PARENT)
            mBinding?.gsyPlayer?.changeUiToPip()
            // 当前播放器回到全屏时,如果处于实际播放状态
            if (gsyHelper.isActuallyPlaying()) {
                gsyHelper.onVideoResume()
                updatePipActions(true)
            }
        } else {
            if (isPipClose) {
                finish()
            } else {
                mBinding?.tvStart.visible()
                mBinding?.gsyPlayer?.margin(top = statusBarHeight)
                mBinding?.gsyPlayer?.size(height = videoHeight)
                mBinding?.gsyPlayer?.changeUiToPip()
                // 如果已产生有效播放进度
                if (gsyHelper.hasValidPlaybackProgress()) {
                    gsyHelper.seekTo()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        // 进页面直接小窗
//        window.decorView.post {
//            enterPipMode()
//        }
        if (!isPipMode) {
            gsyHelper.onVideoResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isPipMode) {
            gsyHelper.onVideoPause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isPipMode) {
            isPipClose = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gsyHelper.onVideoDestroy()
    }

    /**
     * 进入画中画模式 核心方法
     * 1) 点击小窗本体 → 回到全屏
     * 触发 onPictureInPictureModeChanged(isInPip = false, ...)
     * Activity 生命周期 : onPictureInPictureModeChanged -> onResume()
     *
     * 2) 点击关闭按钮（X）
     * 触发 onPictureInPictureModeChanged(isInPip = false, ...)
     * Activity 生命周期 : 不会走 onResume()，直接走 onStop() -> onPictureInPictureModeChanged 并且停在此处
     */
    private fun enterPipMode() {
        // 设置画中画窗口
        val params = PictureInPictureParams.Builder()
            // 宽高比 比如 16:9 / 4:3
            .setAspectRatio(Rational(16, 9))
            // 底部按钮
            .setActions(listOf(getPipAction(false)))
            .build()
        // 进入画中画
        enterPictureInPictureMode(params)
        isPipMode = true
        isPipClose = false
    }

    /**
     * 画中画模式中，每次状态变化时，重新构建 action 并更新 PiP 参数
     */
    private fun updatePipActions(isPause: Boolean) {
        if (!isPipMode) return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(listOf(getPipAction(isPause)))
            .build()
        // 用这个API动态更新，不需要重新进入PiP
        setPictureInPictureParams(params)
    }

    /**
     * 获取画中画按钮
     */
    private fun getPipAction(isPause: Boolean): RemoteAction {
        return if (isPause) {
            // 暂停按钮
            RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_pause), "暂停", "暂停播放", pausePending)
        } else {
            // 播放按钮
            RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_play), "播放", "继续播放", playPending)
        }
    }

}