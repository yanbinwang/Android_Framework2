package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import com.example.common.base.BaseTitleActivity
import com.example.common.config.RouterPath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.permission.registerRequestPermissionWrapper
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.view.clicks
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMediaBinding
import com.example.mvvm.service.MusicService
import com.example.thirdparty.media.utils.MediaHelper
import com.example.thirdparty.utils.NotificationPermissionHelper
import com.therouter.router.Route
import java.util.concurrent.atomic.AtomicBoolean

@Route(path = RouterPath.MediaActivity)
class MediaActivity : BaseTitleActivity<ActivityMediaBinding>(), View.OnClickListener {
    private var isPrepared = AtomicBoolean(false)
    private val media by lazy { MediaHelper(this, false, false) }
    private val notificationPermissionHelper = NotificationPermissionHelper(this, registerRequestPermissionWrapper())

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("音频详情")
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvStart, mBinding?.tvStop)
        // 设置媒体监听
        media.setOnPreparedListener {
            isPrepared.set(true)
        }
        media.setOnErrorListener { _, _, _ ->
            isPrepared.set(false)
        }
        media.setOnCompletionListener {
            isPrepared.set(false)
        }
        notificationPermissionHelper.setOnNotificationListener {
            if (it) {
                if (isPrepared.get()) {
                    MusicService.media = media
                    startService(MusicService::class.java)
                } else {
                    "还在加载".shortToast()
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        // 设置资源
        val fileUri = "https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/music/audio.mp3"
        media.setDataSource(fileUri, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_start -> {
                notificationPermissionHelper.pullUpNotification()
            }
            R.id.tv_stop -> {
                stopService(MusicService::class.java)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(MusicService::class.java)
    }

}