package com.example.multimedia.utils.helper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.common.base.page.Extras
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orFalse
import com.example.multimedia.service.ScreenService
import java.util.Timer
import java.util.TimerTask

/**
 * @description 录屏工具类
 * @author yan
 */
class ScreenHelper(private val activity: FragmentActivity) {
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    /**
     * 处理录屏的回调
     */
    private val activityResultValue =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            stopWaitingTime()
            if (it.resultCode == RESULT_OK) {
                "开始录屏".shortToast()
                activity.startService(ScreenService::class.java, Extras.RESULT_CODE to it.resultCode, Extras.BUNDLE_BEAN to it.data)
//                activity.moveTaskToBack(true)
            } else {
                "取消录屏".shortToast()
            }
        }

    companion object {
        var waitingTime = 0
        var previewWidth = 0
        var previewHeight = 0
    }

    init {
        //获取录屏屏幕宽高，高版本进行修正
        previewWidth = screenWidth
        previewHeight = screenHeight
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var destroy = false
            if (activity.isFinishing.orFalse) destroy = true
            if (activity.isDestroyed.orFalse) destroy = true
            if (activity.windowManager == null) destroy = true
            if (activity.window?.decorView == null) destroy = true
            if (activity.window?.decorView?.parent == null) destroy = true
            if (!destroy) {
                val decorView = activity.window.decorView
                decorView.post {
                    val displayCutout = decorView.rootWindowInsets.displayCutout
                    val rectLists = displayCutout?.boundingRects
                    if (null != rectLists && rectLists.size > 0) {
                        previewWidth = screenWidth - displayCutout.safeInsetLeft - displayCutout.safeInsetRight
                        previewHeight = screenHeight - displayCutout.safeInsetTop - displayCutout.safeInsetBottom
                    }
                }
            }
        }
    }

    /**
     * 开始录屏
     * 尝试唤起手机录屏弹窗，会在onActivityResult中回调结果
     */
    fun startScreen() = activity.execute {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            "请授权上层显示".shortToast()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${packageName}")
            startActivity(intent)
        } else {
            startWaitingTime()
            val mediaProjectionManager = getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            val permissionIntent = mediaProjectionManager?.createScreenCaptureIntent()
            activityResultValue.launch(permissionIntent)
        }
    }

    private fun startWaitingTime() {
        waitingTime = 0
        if (timer == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    waitingTime++
                }
            }
            timer?.schedule(timerTask, 1000)
        }
    }

    private fun stopWaitingTime() {
        timer?.cancel()
        timerTask?.cancel()
        timer = null
        timerTask = null
    }

    /**
     * 结束录屏
     */
    fun stopScreen() = activity.execute { stopService(ScreenService::class.java) }

}