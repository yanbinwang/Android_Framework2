package com.example.gallery.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.example.gallery.utils.album.Album
import com.yanzhenjie.album.Action
import com.yanzhenjie.album.util.AlbumUtils
import com.yanzhenjie.album.util.SystemBar
import java.io.File

/**
 * 相机页
 */
class CameraActivity : BaseActivity() {
    private var mFunction = 0
    private var mCameraFilePath: String? = null
    private var mQuality = 0
    private var mLimitDuration = 0L
    private var mLimitBytes = 0L

    companion object {
        private const val INSTANCE_CAMERA_FUNCTION = "INSTANCE_CAMERA_FUNCTION"
        private const val INSTANCE_CAMERA_FILE_PATH = "INSTANCE_CAMERA_FILE_PATH"
        private const val INSTANCE_CAMERA_QUALITY = "INSTANCE_CAMERA_QUALITY"
        private const val INSTANCE_CAMERA_DURATION = "INSTANCE_CAMERA_DURATION"
        private const val INSTANCE_CAMERA_BYTES = "INSTANCE_CAMERA_BYTES"

        private const val CODE_ACTIVITY_TAKE_IMAGE = 1
        private const val CODE_ACTIVITY_TAKE_VIDEO = 2

        var sResult: Action<String>? = null
        var sCancel: Action<String>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBar.setStatusBarColor(this, Color.TRANSPARENT)
        SystemBar.setNavigationBarColor(this, Color.TRANSPARENT)
        SystemBar.invasionNavigationBar(this)
        SystemBar.invasionNavigationBar(this)
        if (savedInstanceState != null) {
            mFunction = savedInstanceState.getInt(INSTANCE_CAMERA_FUNCTION)
            mCameraFilePath = savedInstanceState.getString(INSTANCE_CAMERA_FILE_PATH)
            mQuality = savedInstanceState.getInt(INSTANCE_CAMERA_QUALITY)
            mLimitDuration = savedInstanceState.getLong(INSTANCE_CAMERA_DURATION)
            mLimitBytes = savedInstanceState.getLong(INSTANCE_CAMERA_BYTES)
        } else {
            val bundle = intent.extras
            checkNotNull(bundle)
            mFunction = bundle.getInt(Album.KEY_INPUT_FUNCTION)
            mCameraFilePath = bundle.getString(Album.KEY_INPUT_FILE_PATH)
            mQuality = bundle.getInt(Album.KEY_INPUT_CAMERA_QUALITY)
            mLimitDuration = bundle.getLong(Album.KEY_INPUT_CAMERA_DURATION)
            mLimitBytes = bundle.getLong(Album.KEY_INPUT_CAMERA_BYTES)
            when (mFunction) {
                Album.FUNCTION_CAMERA_IMAGE -> {
                    if (mCameraFilePath.isNullOrEmpty()) mCameraFilePath = AlbumUtils.randomJPGPath(this)
                    AlbumUtils.takeImage(this, CODE_ACTIVITY_TAKE_IMAGE, File(mCameraFilePath.orEmpty()))
                }
                Album.FUNCTION_CAMERA_VIDEO -> {
                    if (mCameraFilePath.isNullOrEmpty()) mCameraFilePath = AlbumUtils.randomMP4Path(this)
                    AlbumUtils.takeVideo(this, CODE_ACTIVITY_TAKE_VIDEO, File(mCameraFilePath.orEmpty()), mQuality, mLimitDuration, mLimitBytes)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CODE_ACTIVITY_TAKE_IMAGE, CODE_ACTIVITY_TAKE_VIDEO -> {
                if (resultCode == RESULT_OK) {
                    callbackResult()
                } else {
                    callbackCancel()
                }
            }
        }
    }

    private fun callbackResult() {
        if (sResult != null) sResult?.onAction(mCameraFilePath.orEmpty())
        sResult = null
        sCancel = null
        finish()
    }

    private fun callbackCancel() {
        if (sCancel != null) sCancel?.onAction("User canceled.")
        sResult = null
        sCancel = null
        finish()
    }

}