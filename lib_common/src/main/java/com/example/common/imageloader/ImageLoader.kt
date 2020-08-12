package com.example.common.imageloader

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.imageloader.glide.callback.GlideImpl
import com.example.common.imageloader.glide.callback.GlideModule
import com.example.common.imageloader.glide.callback.GlideRequestListener
import com.example.common.imageloader.glide.transform.CornerTransform
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 * 图片加载库使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
 */
class ImageLoader private constructor() : GlideModule(), GlideImpl {
    private var context = BaseApplication.instance.applicationContext
    private var manager: RequestManager? = null

    init {
        this.manager = Glide.with(context)
    }

    companion object {
        @JvmStatic
        val instance: ImageLoader by lazy {
            ImageLoader()
        }
    }

    override fun displayImage(view: ImageView?, string: String?) {
        displayImage(view, string, 0)
    }

    override fun displayImage(view: ImageView?, string: String?, errorId: Int) {
        displayImage(view, string, R.drawable.shape_loading_normal, errorId, null)
    }

    override fun displayImage(view: ImageView?, string: String?, requestListener: GlideRequestListener<Drawable?>?) {
        displayImage(view, string, R.drawable.shape_loading_normal, 0, requestListener)
    }

    override fun displayImage(view: ImageView?, string: String?, placeholderId: Int, errorId: Int, requestListener: GlideRequestListener<Drawable?>?) {
        manager!!
            .load(string)
            .placeholder(placeholderId)
            .error(errorId)
            .dontAnimate()
            .listener(requestListener)
            .into(view!!)
    }

    override fun displayRoundImage(view: ImageView?, string: String?, roundingRadius: Int) {
        displayRoundImage(view, string, 0, roundingRadius)
    }

    override fun displayRoundImage(view: ImageView?, string: String?, errorId: Int, roundingRadius: Int) {
        manager!!
            .load(string)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(roundingRadius)))
            .placeholder(R.drawable.shape_loading_normal)
            .error(errorId)
            .dontAnimate()
            .into(view!!)
    }

    override fun displayRoundImage(view: ImageView?, string: String?, roundingRadius: Int, overRide: BooleanArray) {
        displayRoundImage(view, string, 0, roundingRadius, overRide)
    }

    //leftTop, rightTop, leftBottom, rightBottom
    override fun displayRoundImage(view: ImageView?, string: String?, errorId: Int, roundingRadius: Int, overRide: BooleanArray) {
        val transformation = CornerTransform(context, roundingRadius.toFloat())
        transformation.setExceptCorner(overRide[0], overRide[1], overRide[2], overRide[3])
        manager!!
            .load(string)
            .transform(transformation)
            .placeholder(R.drawable.shape_loading_normal)
            .error(errorId)
            .dontAnimate()
            .into(view!!)
    }

    override fun displayCircleImage(view: ImageView?, string: String?) {
        displayCircleImage(view, string, R.drawable.shape_loading_round)
    }

    override fun displayCircleImage(view: ImageView?, string: String?, errorId: Int) {
        manager!!
            .load(string)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.shape_loading_round)
            .error(errorId)
            .dontAnimate()
            .into(view!!)
    }

    override fun downloadImage(string: String?, width: Int, height: Int, requestListener: GlideRequestListener<File?>?) {
//        //创建保存的文件目录
//        val destFile = File(FileUtil.isExistDir(Constants.APPLICATION_FILE_PATH + "/图片"))
//        //下载对应的图片文件
//        val srcFile: FutureTarget<File> = manager!!
//            .asFile()
//            .load(string)
//            .listener(requestListener)
//            .submit(width, height)
//        //下载的文件从缓存目录拷贝到指定目录
//        FileUtil.copyFile(srcFile.get(), destFile)
        //下载对应的图片文件
        manager!!
            .asFile()
            .load(string)
            .listener(requestListener)
            .submit(width, height)
    }

    override fun clearMemoryCache() {
        Glide.get(context).clearMemory()
    }

    override fun clearDiskCache() {
        Glide.get(context).clearDiskCache()
    }

    override val cacheDir: File?
        get() = Glide.getPhotoCacheDir(context)

}