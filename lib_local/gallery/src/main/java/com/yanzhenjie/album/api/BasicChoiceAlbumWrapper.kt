package com.yanzhenjie.album.api

import android.content.Context
import androidx.annotation.IntRange

/**
 * Created by YanZhenjie on 2017/11/8.
 */
abstract class BasicChoiceAlbumWrapper<Returner : BasicChoiceAlbumWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicChoiceWrapper<Returner, Result, Cancel, Checked>(context) {
    var mQuality = 1
    var mLimitDuration = Integer.MAX_VALUE.toLong()
    var mLimitBytes = Integer.MAX_VALUE.toLong()

    /**
     * Set the quality when taking video, should be 0 or 1. Currently value 0 means low quality, and value 1 means high quality.
     *
     * @param quality should be 0 or 1.
     */
    fun cameraVideoQuality(@IntRange(from = 0, to = 1) quality: Int): Returner {
        this.mQuality = quality
        return this as Returner
    }

    /**
     * Set the maximum number of seconds to take video.
     *
     * @param duration seconds.
     */
    fun cameraVideoLimitDuration(@IntRange(from = 1) duration: Long): Returner {
        this.mLimitDuration = duration
        return this as Returner
    }

    /**
     * Set the maximum file size when taking video.
     *
     * @param bytes the size of the byte.
     */
    fun cameraVideoLimitBytes(@IntRange(from = 1) bytes: Long): Returner {
        this.mLimitBytes = bytes
        return this as Returner
    }

}