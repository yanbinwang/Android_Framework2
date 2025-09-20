package com.yanzhenjie.album.api

import android.content.Context
import com.yanzhenjie.album.Action

/**
 * Created by YanZhenjie on 2017/8/18.
 */
@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BasicCameraWrapper<Returner : BasicCameraWrapper<Returner>>(context: Context) {
    var mFilePath: String? = null
    var mContext: Context? = null
    var mResult: Action<String>? = null
    var mCancel: Action<String>? = null

    init {
        this.mContext = context
    }

    /**
     * Set the action when result.
     *
     * @param result action when producing result.
     */
    fun onResult(result: Action<String>): Returner {
        this.mResult = result
        return this as Returner
    }

    /**
     * Set the action when canceling.
     *
     * @param cancel action when canceled.
     */
    fun onCancel(cancel: Action<String>): Returner {
        this.mCancel = cancel
        return this as Returner
    }

    /**
     * Set the image storage path.
     *
     * @param filePath storage path.
     */
    fun filePath(filePath: String): Returner {
        this.mFilePath = filePath
        return this as Returner
    }

    /**
     * Start up.
     */
    abstract fun start()

}