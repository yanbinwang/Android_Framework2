package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.Nullable;

import com.yanzhenjie.album.Action;

/**
 * Created by YanZhenjie on 2017/8/18.
 */
public abstract class BasicCameraWrapper<Returner extends BasicCameraWrapper> {
    String mFilePath;
    Context mContext;
    Action<String> mResult;
    Action<String> mCancel;

    public BasicCameraWrapper(Context context) {
        this.mContext = context;
    }

    /**
     * Set the action when result.
     *
     * @param result action when producing result.
     */
    public final Returner onResult(Action<String> result) {
        this.mResult = result;
        return (Returner) this;
    }

    /**
     * Set the action when canceling.
     *
     * @param cancel action when canceled.
     */
    public final Returner onCancel(Action<String> cancel) {
        this.mCancel = cancel;
        return (Returner) this;
    }

    /**
     * Set the image storage path.
     *
     * @param filePath storage path.
     */
    public Returner filePath(@Nullable String filePath) {
        this.mFilePath = filePath;
        return (Returner) this;
    }

    /**
     * Start up.
     */
    public abstract void start();

}