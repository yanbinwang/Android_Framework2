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
 * 1.如果图片加载库使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
 * 2.GlideModule在高版本已经不需要继承，写好打上注解全局就会应用（glide的依赖需要都引入）
 */
//class ImageLoader private constructor() : GlideModule(), GlideImpl {
class ImageLoader private constructor() : GlideImpl {
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    companion object {
        @JvmStatic
        val instance by lazy { ImageLoader() }
    }

    override fun loadScaledImage(view: ImageView?, imageUrl: String?, onLoadStart: () -> Unit, onLoadComplete: (bitmap: Bitmap?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.shape_glide_mask_bg)
            .dontAnimate()
            .listener(object : GlideRequestListener<Bitmap>() {
                override fun onLoadStart() {
                    onLoadStart()
                }

                override fun onLoadFinished(resource: Bitmap?) {
                    onLoadComplete(resource)
                }
            })
            .into(ZoomTransform(view))
    }

    override fun loadVideoFrame(view: ImageView?, videoUrl: String?, frameTimeMicros: Long) {
        view ?: return
        try {
            Glide.with(view.context)
                .setDefaultRequestOptions(RequestOptions().frame(frameTimeMicros).centerCrop())
                .load(videoUrl)
                .dontAnimate()
                .into(view)
        } catch (e: Exception) {
            e.printStackTrace()
            view.setBackgroundResource(R.drawable.shape_glide_mask_bg)
        }
    }

    override fun loadGifFromUrl(view: ImageView?, gifUrl: String?) {
        view ?: return
        Glide.with(view.context)
            .asGif()
            .load(gifUrl)
            .into(view)
    }

    override fun loadGifFromResource(view: ImageView?, gifResource: Int?) {
        view ?: return
        Glide.with(view.context)
            .asGif()
            .load(gifResource)
            .into(view)
    }

    override fun loadImageWithProgress(view: ImageView?, imageUrl: String, onLoadStart: () -> Unit, onLoadProgress: (progress: Int?) -> Unit, onLoadResult: (result: Boolean) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
            .addListener(object : GlideRequestListener<Drawable>() {
                override fun onLoadStart() {
                    ProgressInterceptor.addListener(imageUrl) {
                        weakHandler.post {
                            onLoadProgress(it)
                        }
                    }
                    onLoadStart()
                }

                override fun onLoadFinished(resource: Drawable?) {
                    ProgressInterceptor.removeListener(imageUrl)
                    onLoadResult(resource != null)
                }
            })
            .into(view)
    }

    override fun loadImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int?, onLoadStart: () -> Unit, onLoadComplete: (drawable: Drawable?) -> Unit) {
        loadImageDrawableFromUrl(view, imageUrl, view?.context?.drawable(errorResource.orZero), onLoadStart, onLoadComplete)
    }

    override fun loadImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int?, onLoadStart: () -> Unit, onLoadComplete: (drawable: Drawable?) -> Unit) {
        loadImageDrawableFromResource(view, view?.context?.drawable(imageResource.orZero), view?.context?.drawable(errorResource.orZero), onLoadStart, onLoadComplete)
    }

    override fun loadImageDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable?, onLoadStart: () -> Unit, onLoadComplete: (drawable: Drawable?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .listener(object : GlideRequestListener<Drawable>() {
                override fun onLoadStart() {
                    onLoadStart()
                }

                override fun onLoadFinished(resource: Drawable?) {
                    onLoadComplete(resource)
                }
            })
            .into(view)
    }

    override fun loadImageDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable?, onLoadStart: () -> Unit, onLoadComplete: (drawable: Drawable?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(imageDrawable)
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .listener(object : GlideRequestListener<Drawable>() {
                override fun onLoadStart() {
                    onLoadStart()
                }

                override fun onLoadFinished(resource: Drawable?) {
                    onLoadComplete(resource)
                }
            })
            .into(view)
    }

    override fun loadRoundedImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int?, cornerRadius: Int, overrideCorners: BooleanArray) {
        loadRoundedDrawableFromUrl(view, imageUrl, view?.context?.drawable(errorResource.orZero), cornerRadius, overrideCorners)
    }

    override fun loadRoundedImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int?, cornerRadius: Int, overrideCorners: BooleanArray) {
        loadRoundedDrawableFromResource(view, view?.context?.drawable(imageResource.orZero), view?.context?.drawable(errorResource.orZero), cornerRadius, overrideCorners)
    }

    override fun loadRoundedDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable?, cornerRadius: Int, overrideCorners: BooleanArray) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions.bitmapTransform(CornerTransform(view.context, cornerRadius.toSafeFloat()).apply { setExceptCorner(overrideCorners) }))
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun loadRoundedDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable?, cornerRadius: Int, overrideCorners: BooleanArray) {
        view ?: return
        Glide.with(view.context)
            .load(imageDrawable)
            .apply(RequestOptions.bitmapTransform(CornerTransform(view.context, cornerRadius.toSafeFloat()).apply { setExceptCorner(overrideCorners) }))
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun loadCircularImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int?) {
        loadCircularDrawableFromUrl(view, imageUrl, view?.context?.drawable(errorResource.orZero))
    }

    override fun loadCircularImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int?) {
        loadCircularDrawableFromResource(view, view?.context?.drawable(imageResource.orZero), view?.context?.drawable(errorResource.orZero))
    }

    override fun loadCircularDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable?) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_glide_oval_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    override fun loadCircularDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable?) {
        view ?: return
        Glide.with(view.context)
            .load(imageDrawable)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_glide_oval_bg)
            .error(errorDrawable)
            .dontAnimate()
            .into(view)
    }

    /**
     * 获取用于缓存图片的路劲
     */
    override fun getImageCacheDir(context: Context): File? {
        return Glide.getPhotoCacheDir(context)
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
                withContext(Main) { clearDiskCacheAction() }
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

    override fun downloadImage(context: Context, imageUrl: String?, onDownloadStart: () -> Unit, onDownloadComplete: (file: File?) -> Unit) {
        Glide.with(context)
            .downloadOnly()
            .load(imageUrl)
            .listener(object : GlideRequestListener<File>() {
                override fun onLoadStart() {
                    onDownloadStart()
                }

                override fun onLoadFinished(resource: File?) {
                    onDownloadComplete(resource)
                }
            })
            .preload()
    }

}