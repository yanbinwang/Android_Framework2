package com.example.glide.callback

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by WangYanBin on 2020/7/31.
 * 图片下载监听
 */
abstract class GlideRequestListener<R> : RequestListener<R> {

    init {
        GlobalScope.launch(Dispatchers.Main) { onStart() }
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
        GlobalScope.launch(Dispatchers.Main) { onComplete(resource) }
    }

    protected abstract fun onStart()

    protected abstract fun onComplete(resource: R?)

}