package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.Nullable;

import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.api.widget.Widget;

/**
 * <p>Album basic wrapper.</p>
 * Created by yanzhenjie on 17-3-29.
 */
public abstract class BasicAlbumWrapper<Returner extends BasicAlbumWrapper, Result, Cancel, Checked> {
    Action<Result> mResult;
    Action<Cancel> mCancel;
    Widget mWidget;
    Checked mChecked;
    final Context mContext;

    BasicAlbumWrapper(Context context) {
        this.mContext = context;
        mWidget = Widget.getDefaultWidget(context);
    }

    /**
     * Set the action when result.
     *
     * @param result action when producing result.
     */
    public final Returner onResult(Action<Result> result) {
        this.mResult = result;
        return (Returner) this;
    }

    /**
     * Set the action when canceling.
     *
     * @param cancel action when canceled.
     */
    public final Returner onCancel(Action<Cancel> cancel) {
        this.mCancel = cancel;
        return (Returner) this;
    }

    /**
     * Set the widget property.
     *
     * @param widget the widget.
     */
    public final Returner widget(@Nullable Widget widget) {
        this.mWidget = widget;
        return (Returner) this;
    }

    /**
     * Start up.
     */
    public abstract void start();

}