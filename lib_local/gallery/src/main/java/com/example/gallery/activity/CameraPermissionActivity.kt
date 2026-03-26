package com.example.gallery.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.common.base.page.ResultCode.RESULT_IMAGE
import com.example.common.base.page.ResultCode.RESULT_VIDEO
import com.example.common.utils.function.isPathExists
import com.example.common.utils.function.pullUpImage
import com.example.common.utils.function.pullUpVideo
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentLong
import com.example.framework.utils.function.value.hour
import com.example.gallery.R

/**
 * 外部直接通过工具类拉起相机时调取
 */
class CameraPermissionActivity : AppCompatActivity() {
    private var mFilePath: String? = null
    private val mFunction by lazy { intentInt(CAMERA_FUNCTION, 0) }
    private val mQuality by lazy { intentInt(CAMERA_QUALITY, 0) }
    private val mLimitDuration by lazy { intentLong(CAMERA_DURATION, 1.hour) }
    private val mLimitBytes by lazy { intentLong(CAMERA_BYTES, 10L) }

    companion object {
        // 相机功能类型：0 -> 拍照 / 1 -> 录像
        const val CAMERA_FUNCTION = "CAMERA_FUNCTION"
        // 视频质量
        const val CAMERA_QUALITY = "CAMERA_QUALITY"
        // 视频最大时长
        const val CAMERA_DURATION = "CAMERA_DURATION"
        // 视频最大大小
        const val CAMERA_BYTES = "CAMERA_BYTES"
        // 相机回调
        var onResult: ((String) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 禁用过渡动画
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        // 强制竖屏（统一适配，避免横屏回调异常）
        requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        initData()
    }

    private fun initData() {
        // 根据功能类型：打开系统相机
        mFilePath = when (mFunction) {
            // 拍照
            0 -> pullUpImage()
            // 录像
            else -> pullUpVideo(mLimitDuration, mLimitBytes, mQuality)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_IMAGE || requestCode == RESULT_VIDEO) {
            finish()
        } else {
            schedule(this, {
                finish()
            }, 500)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭时返回值
        if (mFilePath.isPathExists()) {
            onResult?.invoke(mFilePath.orEmpty())
        }
        // 用完清空，防止泄漏
        onResult = null
    }

    override fun finish() {
        super.finish()
        // 关闭时也禁用动画，避免闪屏
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
    }

}