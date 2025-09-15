package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

/**
 * Created by YanZhenjie on 2017/11/8.
 */
public abstract class BasicChoiceAlbumWrapper<Returner extends BasicChoiceAlbumWrapper, Result, Cancel, Checked> extends BasicChoiceWrapper<Returner, Result, Cancel, Checked> {
    int mQuality = 1;
    long mLimitDuration = Integer.MAX_VALUE;
    long mLimitBytes = Integer.MAX_VALUE;

    BasicChoiceAlbumWrapper(Context context) {
        super(context);
    }

    /**
     * Set the quality when taking video, should be 0 or 1. Currently value 0 means low quality, and value 1 means high quality.
     *
     * @param quality should be 0 or 1.
     */
    public Returner cameraVideoQuality(@IntRange(from = 0, to = 1) int quality) {
        this.mQuality = quality;
        return (Returner) this;
    }

    /**
     * Set the maximum number of seconds to take video.
     *
     * @param duration seconds.
     */
    public Returner cameraVideoLimitDuration(@IntRange(from = 1) long duration) {
        this.mLimitDuration = duration;
        return (Returner) this;
    }

    /**
     * Set the maximum file size when taking video.
     *
     * @param bytes the size of the byte.
     */
    public Returner cameraVideoLimitBytes(@IntRange(from = 1) long bytes) {
        this.mLimitBytes = bytes;
        return (Returner) this;
    }

}