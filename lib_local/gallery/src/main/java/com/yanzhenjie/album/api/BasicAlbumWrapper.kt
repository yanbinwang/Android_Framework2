package com.yanzhenjie.album.api

import android.content.Context
import com.yanzhenjie.album.Action
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.album.api.widget.Widget.Companion.getDefaultWidget

/**
 * <p>Album basic wrapper.</p>
 * Created by yanzhenjie on 17-3-29.
 */
@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BasicAlbumWrapper<Returner : BasicAlbumWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) {
    var mResult: Action<Result>? = null
    var mCancel: Action<Cancel>? = null
    var mChecked: Checked? = null
    var mWidget: Widget? = null
    var mContext: Context? = null

    init {
        this.mContext = context
        this.mWidget = getDefaultWidget()
    }

    /**
     * Set the action when result.
     *
     * @param result action when producing result.
     */
    fun onResult(result: Action<Result>): Returner {
        this.mResult = result
        return this as Returner
    }

    /**
     * Set the action when canceling.
     *
     * @param cancel action when canceled.
     */
    fun onCancel(cancel: Action<Cancel>): Returner {
        this.mCancel = cancel
        return this as Returner
    }

    /**
     * Set the widget property.
     *
     * @param widget the widget.
     */
    fun widget(widget: Widget): Returner {
        this.mWidget = widget
        return this as Returner
    }

    /**
     * Start up.
     */
    abstract fun start()

}