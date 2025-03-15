package com.example.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Looper
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.drawable
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.glide.callback.GlideImpl
import com.example.glide.callback.GlideModule
import com.example.glide.callback.GlideRequestListener
import com.example.glide.callback.progress.ProgressInterceptor
import com.example.glide.transform.CornerTransform
import com.example.glide.transform.ZoomTransform
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 * 图片加载库使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
 */
class ImageLoader private constructor() : GlideModule(), GlideImpl {
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    companion object {
        @JvmStatic
        val instance by lazy { ImageLoader() }
    }

    override fun displayZoom(view: ImageView?, string: String?, onStart: () -> Unit, onComplete: (bitmap: Bitmap?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .asBitmap()
            .load(string)
            .placeholder(R.drawable.shape_glide_mask_bg)
            .dontAnimate()
            .listener(object : GlideRequestListener<Bitmap?>() {
                override fun onStart() {
                    onStart()
                }

                override fun onComplete(resource: Bitmap?) {
                    onComplete.invoke(resource)
                }
            })
            .into(ZoomTransform(view))
    }

    override fun displayFrame(view: ImageView?, string: String?) {
        view ?: return
        try {
            Glide.with(view.context)
                .setDefaultRequestOptions(RequestOptions().frame(1000000).centerCrop())
                .load(string)
                .dontAnimate()
                .into(view)
        } catch (_: Exception) {
            view.setBackgroundResource(R.drawable.shape_glide_mask_bg)
        }
    }

    override fun displayGif(view: ImageView?, string: String?) {
        view ?: return
        Glide.with(view.context).asGif().load(string).into(view)
    }

    override fun displayGif(view: ImageView?, resourceId: Int?) {
        view ?: return
        Glide.with(view.context).asGif().load(resourceId).into(view)
    }

    override fun displayProgress(view: ImageView?, string: String, onStart: () -> Unit, onProgress: (progress: Int?) -> Unit, onComplete: (result: Boolean) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
            .addListener(object : GlideRequestListener<Drawable?>() {
                override fun onStart() {
                    ProgressInterceptor.addListener(string) { weakHandler.post { onProgress(it) } }
                    onStart()
                }

                override fun onComplete(resource: Drawable?) {
                    ProgressInterceptor.removeListener(string)
                    onComplete(resource != null)
                }
            })
            .into(view)
    }

    override fun display(view: ImageView?, string: String?, errorId: Int?, onStart: () -> Unit, onComplete: (drawable: Drawable?) -> Unit) {
        displayDefType(view, string, view?.context?.drawable(errorId.orZero), onStart, onComplete)
    }

    override fun display(view: ImageView?, resourceId: Int?, errorId: Int?, onStart: () -> Unit, onComplete: (drawable: Drawable?) -> Unit) {
        displayDefType(view, view?.context?.drawable(resourceId.orZero), view?.context?.drawable(errorId.orZero), onStart, onComplete)
    }

    override fun displayDefType(view: ImageView?, string: String?, errorDrawable: Drawable?, onStart: () -> Unit, onComplete: (drawable: Drawable?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(string)
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .listener(object : GlideRequestListener<Drawable?>() {
                override fun onStart() {
                    onStart()
                }

                override fun onComplete(resource: Drawable?) {
                    onComplete.invoke(resource)
                }
            })
            .into(view)
    }

    override fun displayDefType(view: ImageView?, resourceDrawable: Drawable?, errorDrawable: Drawable?, onStart: () -> Unit, onComplete: (drawable: Drawable?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(resourceDrawable)
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .listener(object : GlideRequestListener<Drawable?>() {
                override fun onStart() {
                    onStart()
                }

                override fun onComplete(resource: Drawable?) {
                    onComplete.invoke(resource)
                }
            })
            .into(view)
    }

    override fun displayRound(view: ImageView?, string: String?, errorId: Int?, radius: Int, overRide: BooleanArray) {
        displayRoundDefType(view, string, view?.context?.drawable(errorId.orZero), radius, overRide)
    }

    override fun displayRound(view: ImageView?, resourceId: Int?, errorId: Int?, radius: Int, overRide: BooleanArray) {
        displayRoundDefType(view, view?.context?.drawable(resourceId.orZero), view?.context?.drawable(errorId.orZero), radius, overRide)
    }

    override fun displayRoundDefType(view: ImageView?, string: String?, errorDrawable: Drawable?, radius: Int, overRide: BooleanArray) {
        view ?: return
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions.bitmapTransform(CornerTransform(view.context, radius.toSafeFloat()).apply { setExceptCorner(overRide) }))
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun displayRoundDefType(view: ImageView?, resourceDrawable: Drawable?, errorDrawable: Drawable?, radius: Int, overRide: BooleanArray) {
        view ?: return
        Glide.with(view.context)
            .load(resourceDrawable)
            .apply(RequestOptions.bitmapTransform(CornerTransform(view.context, radius.toSafeFloat()).apply { setExceptCorner(overRide) }))
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun displayCircle(view: ImageView?, string: String?, errorId: Int?) {
        displayCircleDefType(view, string, view?.context?.drawable(errorId.orZero))
    }

    override fun displayCircle(view: ImageView?, resourceId: Int?, errorId: Int?) {
        displayCircleDefType(view, view?.context?.drawable(resourceId.orZero), view?.context?.drawable(errorId.orZero))
    }

    override fun displayCircleDefType(view: ImageView?, string: String?, errorDrawable: Drawable?) {
        view ?: return
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_glide_oval_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun displayCircleDefType(view: ImageView?, resourceDrawable: Drawable?, errorDrawable: Drawable?) {
        view ?: return
        Glide.with(view.context)
            .load(resourceDrawable)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_glide_oval_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun download(context: Context, string: String?, onStart: () -> Unit, onComplete: (file: File?) -> Unit) {
        Glide.with(context)
            .downloadOnly()
            .load(string)
            .listener(object : GlideRequestListener<File?>() {
                override fun onStart() {
                    onStart()
                }

                override fun onComplete(resource: File?) {
                    onComplete.invoke(resource)
                }
            })
            .preload()
    }

    /**
     * 清除内存缓存是在主线程中
     */
    override fun clearMemoryCache(context: Context, owner: LifecycleOwner) {
        val clearDiskCacheAction = {
            try {
                Glide.get(context).clearMemory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!isMainThread) {
            owner.lifecycleScope.launch {
                withContext(Main) {
                    clearDiskCacheAction()
                }
            }
        } else {
            clearDiskCacheAction()
        }
    }

    /**
     * 清除磁盘缓存是在子线程中进行
     */
    override fun clearDiskCache(context: Context, owner: LifecycleOwner) {
        val clearDiskCacheAction = {
            try {
                Glide.get(context).clearDiskCache()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (isMainThread) {
            owner.lifecycleScope.launch {
                withContext(IO) { clearDiskCacheAction() }
            }
        } else {
            clearDiskCacheAction()
        }
    }

    /**
     * 获取用于缓存图片的路劲
     */
    override fun cacheDir(context: Context): File? {
        return Glide.getPhotoCacheDir(context)
    }

}