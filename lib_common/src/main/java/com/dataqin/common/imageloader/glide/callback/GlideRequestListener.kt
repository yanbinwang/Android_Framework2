package com.dataqin.common.imageloader.glide.callback

import android.os.Looper
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dataqin.common.utils.handler.WeakHandler

/**
 * Created by WangYanBin on 2020/7/31.
 * 图片下载监听
 */
abstract class GlideRequestListener<R> : RequestListener<R> {
    private val weakHandler = WeakHandler(Looper.getMainLooper())

    init {
        weakHandler.post { onStart() }
    }

    override fun onResourceReady(resource: R, model: Any?, target: Target<R>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        doResult(resource)
        return false
    }

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<R>?, isFirstResource: Boolean): Boolean {
        doResult(null)
        return false
    }

    private fun doResult(resource: R?) {
        weakHandler.post { onComplete(resource) }
    }

    protected abstract fun onStart()

    protected abstract fun onComplete(resource: R?)

}