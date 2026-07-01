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
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.doOnReceiver
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.mvvm.databinding.ActivityScreenBinding
import com.example.thirdparty.utils.NotificationUtil.getBroadcastPendingIntent
import com.therouter.router.Route

/**
 * 画中画 (仅8.0+支持)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Route(path = RouterPath.ScreenActivity)
class ScreenActivity : BaseActivity<ActivityScreenBinding>() {

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    // 接收按钮点击事件
    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> {
                    "播放".shortToast()
                }
                ACTION_PAUSE -> {
                    "暂停".shortToast()
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 注册点击事件接收
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
        }
        doOnReceiver(this, pipReceiver, filter)
    }

    override fun initEvent() {
        super.initEvent()
        mBinding?.tvStart.click {
            enterPipMode()
        }
    }

    /**
     * 进入画中画模式 核心方法
     */
    private fun enterPipMode() {
        // 播放按钮
//        val playPending = PendingIntent.getBroadcast(this, 1, Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val playPending = getBroadcastPendingIntent(Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT)
        val playAction = RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_play), "播放", "播放", playPending)
        // 暂停按钮
//        val pausePending = PendingIntent.getBroadcast(this, 2, Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pausePending = getBroadcastPendingIntent(Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT)
        val pauseAction = RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_pause), "暂停", "暂停", pausePending)
        // 设置画中画窗口
        val params = PictureInPictureParams.Builder()
            // 宽高比 比如 16:9 / 4:3
            .setAspectRatio(Rational(16, 9))
            // 底部两个按钮
            .setActions(listOf(playAction, pauseAction))
            .build()
        // 进入画中画
        enterPictureInPictureMode(params)
    }

    // 监听进入/退出画中画状态
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // 进入小窗：隐藏播放控制器、标题栏、冗余UI，只留画面
            mBinding?.tvStart.gone()
        } else {
            // 退出小窗：恢复全屏UI、恢复控制器
            mBinding?.tvStart.visible()
        }
    }

}