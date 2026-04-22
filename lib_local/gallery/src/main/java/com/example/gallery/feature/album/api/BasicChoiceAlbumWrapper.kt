package com.example.gallery.feature.album.api

import android.content.Context
import androidx.annotation.IntRange

/**
 * 混合选择器专用
 * 继承自：BasicChoiceWrapper
 * 作用：
 * 这个类是给【既能选图片、又能选视频、还能拍照/录像】的功能用的 , 因为是混合模式，所以需要额外支持【相机录像参数】
 */
abstract class BasicChoiceAlbumWrapper<Returner : BasicChoiceAlbumWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicChoiceWrapper<Returner, Result, Cancel, Checked>(context) {
    // 相机录制视频的质量：0低质量 / 1高质量（默认1）
    protected var mQuality = 1
    // 相机录制视频最大时长（秒）
    protected var mLimitDuration = Long.MAX_VALUE
    // 相机录制视频最大文件大小（字节）
    protected var mLimitBytes = Long.MAX_VALUE

    /**
     * 设置相机录制视频的质量
     * @param quality 0 低质量 / 1 高质量
     */
    fun cameraVideoQuality(@IntRange(from = 0, to = 1) quality: Int): Returner {
        this.mQuality = quality
        return this as Returner
    }

    /**
     * 设置相机录制视频最大时长（秒）
     */
    fun cameraVideoLimitDuration(@IntRange(from = 1) duration: Long): Returner {
        this.mLimitDuration = duration
        return this as Returner
    }

    /**
     * 设置相机录制视频最大文件大小（字节）
     */
    fun cameraVideoLimitBytes(@IntRange(from = 1) bytes: Long): Returner {
        this.mLimitBytes = bytes
        return this as Returner
    }

}