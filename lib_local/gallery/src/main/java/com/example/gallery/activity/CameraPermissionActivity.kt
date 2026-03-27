package com.example.gallery.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.common.base.page.ResultCode.RESULT_ALBUM
import com.example.common.base.page.ResultCode.RESULT_IMAGE
import com.example.common.base.page.ResultCode.RESULT_VIDEO
import com.example.common.utils.function.getFileFromUri
import com.example.common.utils.function.isPathExists
import com.example.common.utils.function.pullUpAlbum
import com.example.common.utils.function.pullUpImage
import com.example.common.utils.function.pullUpVideo
import com.example.common.utils.manager.AppManager
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentLong
import com.example.framework.utils.function.value.hour
import com.example.gallery.R

/**
 * 相机跳转页
 * 功能：调用系统相机拍照 / 录制视频 / 相册
 * 1) 相册模块单独独立,内部调取相册不会有问题,该页面解决的是外部调取
 */
class CameraPermissionActivity : AppCompatActivity() {
    private var mFilePath: String? = null
    private val mFunction by lazy { intentInt(CAMERA_FUNCTION, CAMERA_FUNCTION_IMAGE) }
    private val mQuality by lazy { intentInt(CAMERA_QUALITY, 0) }
    private val mLimitDuration by lazy { intentLong(CAMERA_DURATION, 1.hour) }
    private val mLimitBytes by lazy { intentLong(CAMERA_BYTES, 10L) }

    companion object {
        // 相机功能类型
        const val CAMERA_FUNCTION = "CAMERA_FUNCTION"
        // 拍照
        const val CAMERA_FUNCTION_IMAGE = 0
        // 录像
        const val CAMERA_FUNCTION_VIDEO = 1
        // 相册
        const val CAMERA_FUNCTION_ALBUM = 2
        // 视频质量
        const val CAMERA_QUALITY = "CAMERA_QUALITY"
        // 视频最大时长
        const val CAMERA_DURATION = "CAMERA_DURATION"
        // 视频最大大小
        const val CAMERA_BYTES = "CAMERA_BYTES"
        // 相机回调
        var onResult: ((String) -> Unit)? = null

        /**
         * 拍照
         */
        fun Context?.takePicture(listener: (albumPath: String) -> Unit = {}) {
            this ?: return
            onResult = {
                listener.invoke(it)
            }
            val intent = Intent(this, CameraPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(CAMERA_FUNCTION, CAMERA_FUNCTION_IMAGE)
            startActivity(intent)
        }

        /**
         * 录像
         */
        fun Context?.recordVideo(maxDurationMs: Long = 1.hour, maxSizeMb: Long = 10L, quality: Int = 0, listener: (albumPath: String) -> Unit = {}) {
            this ?: return
            onResult = {
                listener.invoke(it)
            }
            val intent = Intent(this, CameraPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(CAMERA_FUNCTION, CAMERA_FUNCTION_VIDEO)
            intent.putExtra(CAMERA_QUALITY, quality)
            intent.putExtra(CAMERA_DURATION, maxDurationMs)
            intent.putExtra(CAMERA_BYTES, maxSizeMb)
            startActivity(intent)
        }

        /**
         * 相册
         */
        fun Context?.pickImage(listener: (albumPath: String) -> Unit = {}) {
            this ?: return
            onResult = {
                listener.invoke(it)
            }
            val intent = Intent(this, CameraPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(CAMERA_FUNCTION, CAMERA_FUNCTION_ALBUM)
            startActivity(intent)
        }

        /**
         * 调取当前页面的前一个页面的OnResume中调取 , 避免用户跳转后去别的app然后直接切回我们的app
         */
        fun clearCameraPage() {
            val cameraClass = CameraPermissionActivity::class.java
            if (AppManager.isActivityAlive(cameraClass)) {
                AppManager.finishActivitiesOfClass(cameraClass)
            }
        }
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
        // 加入管理类
        AppManager.addActivity(this)
        initData()
    }

    private fun initData() {
        // 根据功能类型：打开系统相机
        when (mFunction) {
            // 拍照
            CAMERA_FUNCTION_IMAGE -> mFilePath = pullUpImage()
            // 录像
            CAMERA_FUNCTION_VIDEO -> mFilePath = pullUpVideo(mLimitDuration, mLimitBytes, mQuality)
            // 相册
            else -> pullUpAlbum()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_IMAGE, RESULT_VIDEO -> finish()
            RESULT_ALBUM -> {
                val uri = data?.data
                val oriFile = uri.getFileFromUri(this)
                mFilePath = oriFile?.absolutePath
                finish()
            }

            else -> {
                schedule(this, {
                    finish()
                }, 500)
            }
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
        // 清除管理类
        AppManager.removeActivity(this)
    }

    override fun finish() {
        super.finish()
        // 关闭时也禁用动画，避免闪屏
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
    }

}