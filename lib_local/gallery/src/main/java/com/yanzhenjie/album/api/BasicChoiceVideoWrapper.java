package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

/**
 * Created by YanZhenjie on 2017/11/8.
 */
public abstract class BasicChoiceVideoWrapper<Returner extends BasicChoiceVideoWrapper, Result, Cancel, Checked> extends BasicChoiceWrapper<Returner, Result, Cancel, Checked> {
    int mQuality = 1;
    long mLimitDuration = Integer.MAX_VALUE;
    long mLimitBytes = Integer.MAX_VALUE;

    BasicChoiceVideoWrapper(Context context) {
        super(context);
    }

    /**
     * Set the quality when taking video, should be 0 or 1. Currently value 0 means low quality, and value 1 means high quality.
     *
     * @param quality should be 0 or 1.
     */
    public Returner quality(@IntRange(from = 0, to = 1) int quality) {
        this.mQuality = quality;
        return (Returner) this;
    }

    /**
     * Specify the maximum allowed recording duration in seconds.
     *
     * @param duration the maximum number of seconds.
     */
    public Returner limitDuration(@IntRange(from = 1) long duration) {
        this.mLimitDuration = duration;
        return (Returner) this;
    }

    /**
     * Specify the maximum allowed size.
     *
     * @param bytes the size of the byte.
     */
    public Returner limitBytes(@IntRange(from = 1) long bytes) {
        this.mLimitBytes = bytes;
        return (Returner) this;
    }

}