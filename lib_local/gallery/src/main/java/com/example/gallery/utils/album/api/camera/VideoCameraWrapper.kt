package com.example.gallery.utils.album.api.camera

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.activity.CameraActivity
import com.example.gallery.utils.album.Album

class VideoCameraWrapper(context: Context?) : BasicCameraWrapper<VideoCameraWrapper>(context) {
    private var mQuality = 1
    private var mLimitDuration = Int.MAX_VALUE.toLong()
    private var mLimitBytes = Int.MAX_VALUE.toLong()

    fun quality(@IntRange(from = 0, to = 1) quality: Int): VideoCameraWrapper {
        this.mQuality = quality
        return this
    }

    /**
     * Specify the maximum allowed recording duration in seconds.
     *
     * @param duration the maximum number of seconds.
     */
    fun limitDuration(@IntRange(from = 1) duration: Long): VideoCameraWrapper {
        this.mLimitDuration = duration
        return this
    }

    /**
     * Specify the maximum allowed size.
     *
     * @param bytes the size of the byte.
     */
    fun limitBytes(@IntRange(from = 1) bytes: Long): VideoCameraWrapper {
        this.mLimitBytes = bytes
        return this
    }

    override fun start() {
        CameraActivity.sResult = mResult
        CameraActivity.sCancel = mCancel
        val intent = Intent(mContext, CameraActivity::class.java)

        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_VIDEO)
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath)
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality)
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration)
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes)
        mContext.startActivity(intent)
    }

}