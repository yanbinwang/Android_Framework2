package com.example.glide.callback

import android.os.Looper
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.framework.utils.WeakHandler

/**
 * Created by WangYanBin on 2020/7/31.
 * 图片下载监听
 */
abstract class GlideRequestListener<R> : RequestListener<R> {
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    init {
        weakHandler.post { onStart() }
    }

    /**
     * 加载成功
     */
    override fun onResourceReady(resource: R & Any, model: Any, target: Target<R>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
        doResult(resource)
        /**
         * true->表示已经处理好资源，不让 Glide 继续默认流程
         * false->让 Glide 继续默认的显示流程
         */
        return false
    }

    /**
     * 加载失败
     */
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<R>, isFirstResource: Boolean): Boolean {
        doResult(null)
        return false
    }

    private fun doResult(resource: R?) {
        weakHandler.post { onComplete(resource) }
    }

    protected abstract fun onStart()

    protected abstract fun onComplete(resource: R?)

}