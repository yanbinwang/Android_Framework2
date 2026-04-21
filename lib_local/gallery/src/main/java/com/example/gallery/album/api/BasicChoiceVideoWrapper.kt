package com.example.gallery.album.api

import android.content.Context
import androidx.annotation.IntRange

/**
 * 视频选择/拍摄专用
 * 继承自：BasicChoiceWrapper
 * 功能：专门封装 视频选择、视频拍摄 的公共配置（画质、时长、大小限制）
 */
abstract class BasicChoiceVideoWrapper<Returner : BasicChoiceVideoWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicChoiceWrapper<Returner, Result, Cancel, Checked>(context) {
    // 视频录制质量：0=低质量，1=高质量（默认1）
    protected var mQuality = 1
    // 视频最大录制时长（默认无限制）
    protected var mLimitDuration = Long.MAX_VALUE
    // 视频最大文件大小（默认无限制）
    protected var mLimitBytes = Long.MAX_VALUE

    /**
     * 设置视频录制质量
     * @param quality 0 低质量 / 1 高质量
     */
    fun quality(@IntRange(from = 0, to = 1) quality: Int): Returner {
        this.mQuality = quality
        return this as Returner
    }

    /**
     * 设置视频最大录制时长（秒）
     */
    fun limitDuration(@IntRange(from = 1) duration: Long): Returner {
        this.mLimitDuration = duration
        return this as Returner
    }

    /**
     * 设置视频最大允许的文件大小（字节）
     */
    fun limitBytes(@IntRange(from = 1) bytes: Long): Returner {
        this.mLimitBytes = bytes
        return this as Returner
    }

}