package com.example.common.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.imageloader.glide.callback.GlideImpl
import com.example.common.imageloader.glide.callback.GlideModule
import com.example.common.imageloader.glide.callback.GlideRequestListener
import com.example.common.imageloader.glide.callback.progress.ProgressInterceptor
import com.example.common.imageloader.glide.transform.CornerTransform
import com.example.common.imageloader.glide.transform.ZoomTransform
import java.io.File


/**
 * Created by WangYanBin on 2020/5/29.
 * 图片加载库使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
 */
class ImageLoader private constructor() : GlideModule(), GlideImpl {

    companion object {
        @JvmStatic
        val instance by lazy { ImageLoader() }
    }

    override fun displayZoom(view: ImageView, string: String?, listener: GlideRequestListener<Bitmap?>?) {
        Glide.with(view.context)
            .asBitmap()
            .load(string)
            .placeholder(R.drawable.shape_black_image_loading)
            .dontAnimate()
            .listener(listener)
            .into(ZoomTransform(view))
    }

    override fun displayCover(view: ImageView, string: String?) {
        Glide.with(view.context)
            .setDefaultRequestOptions(RequestOptions().frame(1000000).centerCrop())
            .load(string)
            .dontAnimate()
            .into(view)
    }

    override fun displayProgress(view: ImageView, string: String, onStart: () -> Unit?, onProgress: (progress: Int?) -> Unit, onComplete: () -> Unit?) {
        ProgressInterceptor.addListener(string) { onProgress(it) }
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
            .addListener(object : GlideRequestListener<Drawable?>() {
                override fun onStart() {
                    onStart()
                }

                override fun onComplete(resource: Drawable?) {
                    ProgressInterceptor.removeListener(string)
                    onComplete()
                }
            })
            .into(view)
    }

    override fun display(view: ImageView, string: String?, placeholderId: Int, errorId: Int, listener: GlideRequestListener<Drawable?>?) {
        Glide.with(view.context)
            .load(string)
            .placeholder(placeholderId)
            .error(errorId)
            .dontAnimate()
            .listener(listener)
            .into(view)
    }

    override fun displayRound(view: ImageView, string: String?, errorId: Int, roundingRadius: Int, overRide: BooleanArray) {
//        Glide.with(view.context)
//            .load(string)
//            .apply(RequestOptions.bitmapTransform(RoundedCorners(roundingRadius)))
//            .placeholder(R.drawable.shape_image_loading)
//            .error(errorId)
//            .dontAnimate()
//            .into(view)
        val transformation = CornerTransform(view.context, roundingRadius.toFloat())
        transformation.setExceptCorner(overRide[0], overRide[1], overRide[2], overRide[3])
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions.bitmapTransform(transformation))
            .placeholder(R.drawable.shape_white_image_loading)
            .error(errorId)
            .dontAnimate()
            .into(view)
    }

    override fun displayCircle(view: ImageView, string: String?, errorId: Int) {
        Glide.with(view.context)
            .load(string)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_white_oval_image_loading)
            .error(errorId)
            .dontAnimate()
            .into(view)
    }

    override fun download(context: Context, string: String?, listener: GlideRequestListener<File?>?) {
//    override fun downloadImage(context: Context, string: String?, width: Int, height: Int, listener: GlideRequestListener<File?>?) {
//        //创建保存的文件目录
//        val destFile = File(FileUtil.isExistDir(Constants.APPLICATION_FILE_PATH + "/图片"))
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
            .listener(listener)
            .preload()
    }

    //清除内存缓存是在主线程中
    override fun clearMemoryCache(context: Context) {
        Glide.get(context).clearMemory()
    }

    //清除磁盘缓存是在子线程中进行
    override fun clearDiskCache(context: Context) {
        Thread { Glide.get(context).clearDiskCache() }.start()
    }

    override val cacheDir: File?
        get() = Glide.getPhotoCacheDir(BaseApplication.instance?.applicationContext!!)

}