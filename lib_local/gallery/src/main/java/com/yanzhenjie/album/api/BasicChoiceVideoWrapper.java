package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

/**
 * 视频选择/拍摄专用
 * 继承自：BasicChoiceWrapper
 * 功能：专门封装 视频选择、视频拍摄 的公共配置（画质、时长、大小限制）
 */
public abstract class BasicChoiceVideoWrapper<Returner extends BasicChoiceVideoWrapper, Result, Cancel, Checked> extends BasicChoiceWrapper<Returner, Result, Cancel, Checked> {
    // 视频录制质量：0=低质量，1=高质量（默认1）
    protected int mQuality = 1;
    // 视频最大录制时长（默认无限制）
    protected long mLimitDuration = Integer.MAX_VALUE;
    // 视频最大文件大小（默认无限制）
    protected long mLimitBytes = Integer.MAX_VALUE;

    public BasicChoiceVideoWrapper(Context context) {
        super(context);
    }

    /**
     * 设置视频录制质量
     * @param quality 0 低质量 / 1 高质量
     */
    public Returner quality(@IntRange(from = 0, to = 1) int quality) {
        this.mQuality = quality;
        return (Returner) this;
    }

    /**
     * 设置视频最大录制时长（秒）
     */
    public Returner limitDuration(@IntRange(from = 1) long duration) {
        this.mLimitDuration = duration;
        return (Returner) this;
    }

    /**
     * 设置视频最大允许的文件大小（字节）
     */
    public Returner limitBytes(@IntRange(from = 1) long bytes) {
        this.mLimitBytes = bytes;
        return (Returner) this;
    }

}