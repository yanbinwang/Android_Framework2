package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

/**
 * 混合选择器专用
 * 继承自：BasicChoiceWrapper
 * 作用：
 * 这个类是给【既能选图片、又能选视频、还能拍照/录像】的功能用的
 * 因为是混合模式，所以需要额外支持【相机录像参数】
 */
public abstract class BasicChoiceAlbumWrapper<Returner extends BasicChoiceAlbumWrapper, Result, Cancel, Checked> extends BasicChoiceWrapper<Returner, Result, Cancel, Checked> {
    // 相机录制视频的质量：0低质量 / 1高质量（默认1）
    protected int mQuality = 1;
    // 相机录制视频最大时长（秒）
    protected long mLimitDuration = Integer.MAX_VALUE;
    // 相机录制视频最大文件大小（字节）
    protected long mLimitBytes = Integer.MAX_VALUE;

    public BasicChoiceAlbumWrapper(Context context) {
        super(context);
    }

    /**
     * 设置相机录制视频的质量
     * @param quality 0 低质量 / 1 高质量
     */
    public Returner cameraVideoQuality(@IntRange(from = 0, to = 1) int quality) {
        this.mQuality = quality;
        return (Returner) this;
    }

    /**
     * 设置相机录制视频最大时长（秒）
     */
    public Returner cameraVideoLimitDuration(@IntRange(from = 1) long duration) {
        this.mLimitDuration = duration;
        return (Returner) this;
    }

    /**
     * 设置相机录制视频最大文件大小（字节）
     */
    public Returner cameraVideoLimitBytes(@IntRange(from = 1) long bytes) {
        this.mLimitBytes = bytes;
        return (Returner) this;
    }

}