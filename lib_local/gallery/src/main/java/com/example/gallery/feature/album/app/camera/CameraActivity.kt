package com.example.gallery.feature.album.app.camera

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentLong
import com.example.framework.utils.function.intentString
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.callback.Action
import com.example.gallery.feature.album.utils.AlbumUtil
import java.io.File

/**
 * 相机跳转页
 * 功能：调用系统相机拍照 / 录制视频
 * 拍完后把文件路径回调给外部
 */
internal class CameraActivity : BaseActivity() {
    // 相机功能类型：拍照 / 录像
    private val mFunction by lazy { intentInt(Album.KEY_INPUT_FUNCTION) }
    // 视频质量
    private val mQuality by lazy { intentInt(Album.KEY_INPUT_CAMERA_QUALITY) }
    // 视频最大时长
    private val mLimitDuration by lazy { intentLong(Album.KEY_INPUT_CAMERA_DURATION) }
    // 视频最大大小
    private val mLimitBytes by lazy { intentLong(Album.KEY_INPUT_CAMERA_BYTES) }
    // 拍照/录像保存的文件路径
    private lateinit var mCameraFilePath: String

    companion object {
        // 相机请求码
        private const val CODE_ACTIVITY_TAKE_IMAGE = 1 // 拍照
        private const val CODE_ACTIVITY_TAKE_VIDEO = 2 // 录像

        // 外部回调监听
        var sResult: Action<String>? = null // 成功
        var sCancel: Action<String>? = null // 取消
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 校验参数
        if (!hasExtras()) return finish()
        // 禁用过渡动画
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        // 强制竖屏（统一适配，避免横屏回调异常）
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
        // 拿取默认传递的路径
        mCameraFilePath = intentString(Album.KEY_INPUT_FILE_PATH)
        // 根据功能类型：打开系统相机
        when (mFunction) {
            Album.FUNCTION_CAMERA_IMAGE -> {
                if (mCameraFilePath.isEmpty()) {
                    // 没有指定路径，自动生成一个
                    mCameraFilePath = AlbumUtil.randomJPGPath(this)
                }
                // 调用系统拍照
                AlbumUtil.takeImage(this, CODE_ACTIVITY_TAKE_IMAGE, File(mCameraFilePath))
            }
            Album.FUNCTION_CAMERA_VIDEO -> {
                if (mCameraFilePath.isEmpty()) {
                    // 自动生成视频路径
                    mCameraFilePath = AlbumUtil.randomMP4Path(this)
                }
                // 调用系统录像
                AlbumUtil.takeVideo(
                    this,
                    CODE_ACTIVITY_TAKE_VIDEO,
                    File(mCameraFilePath),
                    mQuality,
                    mLimitDuration,
                    mLimitBytes
                )
            }
            else -> throw AssertionError("This should not be the case.")
        }
    }

    /**
     * 相机拍摄完成后接收结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CODE_ACTIVITY_TAKE_IMAGE, CODE_ACTIVITY_TAKE_VIDEO -> {
                if (resultCode == RESULT_OK) {
                    // 拍摄成功
                    callbackResult()
                } else {
                    // 用户取消
                    callbackCancel()
                }
            }
            else -> throw AssertionError("This should not be the case.")
        }
    }

    /**
     * 成功回调：返回文件路径
     */
    private fun callbackResult() {
        sResult?.onAction(mCameraFilePath)
        // 清空监听，防止内存泄漏
        sResult = null
        sCancel = null
        finish()
    }

    /**
     * 取消回调
     */
    private fun callbackCancel() {
        sCancel?.onAction("User canceled.")
        // 清空监听
        sResult = null
        sCancel = null
        finish()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        finish()
        return true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
    }

}