package com.example.gallery.album.api

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.album.Album
import com.example.gallery.album.app.camera.CameraActivity

class VideoCameraWrapper(context: Context) : BasicCameraWrapper<VideoCameraWrapper>(context) {
    // 视频质量：0低质量 1高质量（默认1）
    private var mQuality = 1
    // 最大录制时长（秒）
    private var mLimitDuration = Long.MAX_VALUE
    // 最大文件大小（字节）
    private var mLimitBytes = Long.MAX_VALUE

    /**
     * 设置视频录制质量
     * @param quality 0 / 1
     */
    fun quality(@IntRange(from = 0, to = 1) quality: Int): VideoCameraWrapper {
        this.mQuality = quality
        return this
    }

    /**
     * 设置最大录制时长（秒）
     */
    fun limitDuration(@IntRange(from = 1) duration: Long): VideoCameraWrapper {
        this.mLimitDuration = duration
        return this
    }

    /**
     * 设置视频最大文件大小
     */
    fun limitBytes(@IntRange(from = 1) bytes: Long): VideoCameraWrapper {
        this.mLimitBytes = bytes
        return this
    }

    /**
     * 启动相机录像页面
     */
    override fun start() {
        // 静态注入回调
        CameraActivity.sResult = mResult
        CameraActivity.sCancel = mCancel
        val intent = Intent(mContext, CameraActivity::class.java)
        // 功能 = 录像
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_VIDEO)
        // 保存路径
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath)
        // 视频质量、时长、大小
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality)
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration)
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes)
        mContext.startActivity(intent)
    }

}