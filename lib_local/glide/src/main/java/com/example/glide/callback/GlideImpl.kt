package com.example.glide.callback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.drawable
import com.example.glide.R
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 */
interface GlideImpl {

    companion object {
        private const val DEFAULT_CORNER_RADIUS = 5
        private val DEFAULT_OVERRIDE_CORNERS = booleanArrayOf(false, false, false, false)
        private val DEFAULT_ERROR_RESOURCE = R.drawable.shape_glide_bg
        private val DEFAULT_CIRCULAR_ERROR_RESOURCE = R.drawable.shape_glide_oval_bg
        private fun getDefaultErrorDrawable(view: ImageView?) = view?.context?.drawable(DEFAULT_ERROR_RESOURCE)
        private fun getDefaultCircularErrorDrawable(view: ImageView?) = view?.context?.drawable(DEFAULT_CIRCULAR_ERROR_RESOURCE)
    }

    //---------------------------------------------图片加载开始---------------------------------------------
    /**
     * 加载图片并根据设置的宽度等比例拉伸高度
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Bitmap
     */
    fun loadScaledImage(view: ImageView?, imageUrl: String?, onLoadStart: () -> Unit = {}, onLoadComplete: (bitmap: Bitmap?) -> Unit = {})

    /**
     * 加载线上视频的某一帧，所需帧的时间位置，单位为微秒。如果为负，返回一个代表性帧
     *  1秒 = 10分秒
     *  1分秒 = 10厘秒
     *  1厘秒 = 10毫秒
     *  1毫秒 = 1000微秒
     *  1微秒 = 1000纳秒->取得的是微秒
     *  1纳秒 = 1000皮秒
     * @param view 用于显示视频帧的 ImageView
     * @param videoUrl 视频的 URL 地址
     * @param frameTimeMicros 要提取的帧的时间（微秒）
     */
    fun loadVideoFrame(view: ImageView?, videoUrl: String?, frameTimeMicros: Long = 1000000000)

    /**
     * 加载网络 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifUrl GIF 图片的 URL 地址
     */
    fun loadGifFromUrl(view: ImageView?, gifUrl: String?)

    /**
     * 加载本地 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifResource 本地 GIF 图片的资源 ID
     */
    fun loadGifFromResource(view: ImageView?, gifResource: Int?)

    /**
     * 加载图片并捕获加载进度
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址，不能为空
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadProgress 图片加载进度的回调
     * @param onLoadResult 图片加载结果的回调，true 表示加载成功，false 表示失败
     */
    fun loadImageWithProgress(view: ImageView?, imageUrl: String, onLoadStart: () -> Unit = {}, onLoadProgress: (progress: Int?) -> Unit = {}, onLoadResult: (result: Boolean) -> Unit = {})

    /**
     * 加载图片，支持不同的展示方式（URL 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int? = DEFAULT_ERROR_RESOURCE, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {})

    /**
     * 加载图片，支持不同的展示方式（资源 ID 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageResource 图片的资源 ID
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int? = DEFAULT_ERROR_RESOURCE, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {})

    /**
     * 加载图片，支持不同的展示方式（Drawable 方式，URL 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable? = getDefaultErrorDrawable(view), onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {})

    /**
     * 加载图片，支持不同的展示方式（Drawable 方式，资源 ID 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageDrawable 图片的 Drawable
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable? = getDefaultErrorDrawable(view), onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {})

    /**
     * 加载圆角图片（URL 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param cornerRadius 圆角半径
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int? = DEFAULT_ERROR_RESOURCE, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS)

    /**
     * 加载圆角图片（资源 ID 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageResource 图片的资源 ID
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param cornerRadius 圆角半径
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int? = DEFAULT_ERROR_RESOURCE, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS)

    /**
     * 加载圆角图片（Drawable 方式，URL 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param cornerRadius 圆角半径，默认值为 5
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable? = getDefaultErrorDrawable(view), cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS)

    /**
     * 加载圆角图片（Drawable 方式，资源 ID 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageDrawable 图片的 Drawable
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param cornerRadius 圆角半径，默认值为 5
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable? = getDefaultErrorDrawable(view), cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS)

    /**
     * 加载圆形图片（URL 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorResource 加载失败时显示的错误图片资源 ID
     */
    fun loadCircularImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int? = DEFAULT_CIRCULAR_ERROR_RESOURCE)

    /**
     * 加载圆形图片（资源 ID 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageResource 图片的资源 ID
     * @param errorResource 加载失败时显示的错误图片资源 ID
     */
    fun loadCircularImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int? = DEFAULT_CIRCULAR_ERROR_RESOURCE)

    /**
     * 加载圆形图片（Drawable 方式，URL 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorDrawable 加载失败时显示的错误 Drawable
     */
    fun loadCircularDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable? = getDefaultCircularErrorDrawable(view))

    /**
     * 加载圆形图片（Drawable 方式，资源 ID 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageDrawable 图片的 Drawable
     * @param errorDrawable 加载失败时显示的错误 Drawable
     */
    fun loadCircularDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable? = getDefaultCircularErrorDrawable(view))
    //---------------------------------------------图片加载结束---------------------------------------------

    //---------------------------------------------图片库方法开始---------------------------------------------
    /**
     * 获取图片缓存目录
     * @param context 上下文对象
     * @return 图片缓存目录的 File 对象，如果获取失败返回 null
     */
    fun getImageCacheDir(context: Context): File?

    /**
     * 清除内存缓存
     * @param context 上下文对象
     * @param owner 生命周期所有者，用于管理协程等操作
     */
    fun clearMemoryCache(context: Context, owner: LifecycleOwner)

    /**
     * 清除磁盘缓存
     * @param context 上下文对象
     * @param owner 生命周期所有者，用于管理协程等操作
     */
    fun clearDiskCache(context: Context, owner: LifecycleOwner)

    /**
     * 下载图片
     * @param context 上下文对象
     * @param imageUrl 图片的 URL 地址
     * @param onDownloadStart 下载开始时的回调
     * @param onDownloadComplete 下载完成时的回调，返回下载的文件
     */
    fun downloadImage(context: Context, imageUrl: String? = null, onDownloadStart: () -> Unit = {}, onDownloadComplete: (file: File?) -> Unit = {})
    //---------------------------------------------图片库方法结束---------------------------------------------

}