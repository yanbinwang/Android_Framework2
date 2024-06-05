package com.example.glide

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Looper
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.value.toSafeFloat
import com.example.glide.callback.GlideImpl
import com.example.glide.callback.GlideModule
import com.example.glide.callback.GlideRequestListener
import com.example.glide.callback.progress.ProgressInterceptor
import com.example.glide.transform.CornerTransform
import com.example.glide.transform.ZoomTransform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    override fun displayProgress(view: ImageView?, string: String?, onStart: () -> Unit, onProgress: (progress: Int?) -> Unit, onComplete: (result: Boolean) -> Unit) {
        view ?: return
        string ?: return
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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun display(view: ImageView?, string: String?, errorId: Drawable?, onStart: () -> Unit, onComplete: (drawable: Drawable?) -> Unit) {
        view ?: return
//        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
//        Glide.with(view.context)
//            .load(string)
//            .placeholder(placeholderId)
//            .error(errorId)
//            .transition(DrawableTransitionOptions.withCrossFade(factory))
//            .listener(object : GlideRequestListener<Drawable?>() {
//                override fun onStart() {
//                    onStart()
//                }
//
//                override fun onComplete(resource: Drawable?) {
//                    onComplete.invoke(resource)
//                }
//            })
//            .into(view)
        Glide.with(view.context)
            .load(string)
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorId)
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

    override fun display(view: ImageView?, resourceId: Drawable?, errorId: Drawable?, onStart: () -> Unit, onComplete: (drawable: Drawable?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .load(resourceId)
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorId)
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

    override fun displayRound(view: ImageView?, string: String?, errorId: Drawable?, radius: Int, overRide: BooleanArray) {
        view ?: return
//        Glide.with(view.context)
//            .load(string)
//            .apply(RequestOptions.bitmapTransform(RoundedCorners(roundingRadius)))
//            .placeholder(R.drawable.shape_image_loading)
//            .error(errorId)
//            .dontAnimate()
//            .into(view)
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions.bitmapTransform(CornerTransform(view.context, radius.toSafeFloat()).apply { setExceptCorner(overRide) }))
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorId)
            .dontAnimate()
            .into(view)
    }

    override fun displayRound(view: ImageView?, resourceId: Drawable?, errorId: Drawable?, radius: Int, overRide: BooleanArray) {
        view ?: return
        Glide.with(view.context)
            .load(resourceId)
            .apply(RequestOptions.bitmapTransform(CornerTransform(view.context, radius.toSafeFloat()).apply { setExceptCorner(overRide) }))
            .placeholder(R.drawable.shape_glide_bg)
            .error(errorId)
            .dontAnimate()
            .into(view)
    }

    override fun displayCircle(view: ImageView?, string: String?, errorId: Drawable?) {
        view ?: return
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_glide_oval_bg)
            .error(errorId)
            .dontAnimate()
            .into(view)
    }

    override fun displayCircle(view: ImageView?, resourceId: Drawable?, errorId: Drawable?) {
        view ?: return
        Glide.with(view.context)
            .load(resourceId)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_glide_oval_bg)
            .error(errorId)
            .dontAnimate()
            .into(view)
    }

    override fun download(context: Context, string: String?, onStart: () -> Unit, onComplete: (file: File?) -> Unit) {
//        //创建保存的文件目录
//        val destFile = File(FileUtil.isMkdirs(Constants.APPLICATION_FILE_PATH + "/图片"))
//        //下载对应的图片文件
//        val srcFile = Glide.with(context)
//            .asFile()
//            .load(string)
//            .listener(requestListener)
//            .submit(width, height)
//        //下载的文件从缓存目录拷贝到指定目录
//        FileUtil.copyFile(srcFile.get(), destFile)
        //下载对应的图片文件
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

    //清除内存缓存是在主线程中
    override fun clearMemoryCache(context: Context) {
        try {
            if (!isMainThread) {
                GlobalScope.launch(Dispatchers.Main) { Glide.get(context).clearMemory() }
            } else {
                Glide.get(context).clearMemory()
            }
        } catch (ignore: Exception) {
        }
    }

    //清除磁盘缓存是在子线程中进行
    override fun clearDiskCache(context: Context) {
        try {
            if (isMainThread) {
                GlobalScope.launch(Dispatchers.IO) { Glide.get(context).clearDiskCache() }
            } else {
                Glide.get(context).clearDiskCache()
            }
        } catch (_: Exception) {
        }
    }

    //获取用于缓存图片的路劲
    override fun cacheDir(context: Context): File? {
        return Glide.getPhotoCacheDir(context)
    }

}