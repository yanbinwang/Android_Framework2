package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import com.example.common.base.BaseTitleActivity
import com.example.common.config.RouterPath
import com.example.common.utils.permission.registerRequestPermissionWrapper
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.view.clicks
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMediaBinding
import com.example.mvvm.service.MusicService
import com.example.thirdparty.utils.NotificationPermissionHelper
import com.therouter.router.Route

@Route(path = RouterPath.MediaActivity)
class MediaActivity : BaseTitleActivity<ActivityMediaBinding>(), View.OnClickListener {
    private val notificationPermissionHelper = NotificationPermissionHelper(this, registerRequestPermissionWrapper())

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("音频详情")
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvStart, mBinding?.tvStop)
        notificationPermissionHelper.setOnNotificationListener {
            if (it) {
                startService(MusicService::class.java)
            }
        }
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