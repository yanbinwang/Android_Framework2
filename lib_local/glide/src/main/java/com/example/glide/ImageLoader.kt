package com.example.glide

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.example.framework.utils.function.drawable
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.glide.callback.GlideRequestListener
import com.example.glide.callback.progress.ProgressInterceptor
import com.example.glide.transform.CornerTransform
import com.example.glide.transform.ZoomTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 * 1.如果图片加载库使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
 * 2.GlideModule在高版本已经不需要继承，写好打上注解全局就会应用（glide的依赖需要都引入）
 */
//class ImageLoader private constructor() : GlideModule(), GlideImpl {
@SuppressLint("CheckResult")
class ImageLoader private constructor() {
    private val scope by lazy { CoroutineScope(SupervisorJob() + Main.immediate) }

    companion object {
        @JvmStatic
        val instance by lazy { ImageLoader() }

        /**
         * 默认遮罩
         */
        @JvmStatic
        val DEFAULT_MASK_RESOURCE = R.drawable.shape_glide_mask

        /**
         * 默认加载
         */
        @JvmStatic
        val DEFAULT_RESOURCE = R.drawable.shape_glide_default

        /**
         * 默认弧形加载
         */
        @JvmStatic
        val DEFAULT_ROUNDED_RESOURCE = R.drawable.shape_glide_rounded

        /**
         * 默认圆形加载
         */
        @JvmStatic
        val DEFAULT_CIRCULAR_RESOURCE = R.drawable.shape_glide_circular

        /**
         * 圆角图片弧度
         */
        @JvmStatic
        val DEFAULT_CORNER_RADIUS = 5

        /**
         * 圆角图片4个边是否都是弧线
         */
        @JvmStatic
        val DEFAULT_OVERRIDE_CORNERS = booleanArrayOf(false, false, false, false)

        /**
         * 获取drawable的图片
         */
        @JvmStatic
        private fun getDefaultDrawable(view: ImageView?) = view?.context?.drawable(DEFAULT_RESOURCE)

        @JvmStatic
        private fun getDefaultRoundedDrawable(view: ImageView?) = view?.context?.drawable(DEFAULT_ROUNDED_RESOURCE)

        @JvmStatic
        private fun getDefaultCircularDrawable(view: ImageView?) = view?.context?.drawable(DEFAULT_CIRCULAR_RESOURCE)

        /**
         * dontAnimate()会造成闪屏，切换为渐隐动画，使其“流畅”
         * // 假设未来添加了对 SVG 的支持
         * import com.bumptech.glide.load.resource.svg.SvgDrawable
         * // 在 when 表达式中添加新分支
         * SvgDrawable::class -> DrawableTransitionOptions.withCrossFade(duration)
         */
        @JvmStatic
        inline fun <reified T : Any> RequestBuilder<T>.smartFade(imageView: ImageView, firstLoadDuration: Int = 300, reloadDuration: Int = 0): RequestBuilder<T> {
            val duration = if (imageView.drawable == null) firstLoadDuration else reloadDuration
            // 根据泛型类型动态选择正确的 TransitionOptions
            val options = when (T::class) {
                Drawable::class, GifDrawable::class -> DrawableTransitionOptions.withCrossFade(duration)
                Bitmap::class -> BitmapTransitionOptions.withCrossFade(duration)
                else -> throw IllegalArgumentException("Unsupported type: ${T::class.java.simpleName}. Currently only supports Drawable and Bitmap.")
            }
            return transition(options as TransitionOptions<*, in T>)
        }
    }

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
    fun loadVideoFrame(view: ImageView?, videoUrl: String?, frameTimeMicros: Long = 1000000000) {
        view ?: return
        try {
            // 使用RequestOptions构建器明确配置
            val options = RequestOptions()
                .frame(frameTimeMicros)
                .fitCenter()
                .placeholder(DEFAULT_RESOURCE)
                .error(DEFAULT_MASK_RESOURCE)
                // 禁用内存缓存
                .skipMemoryCache(true)
                // 仅缓存原始数据（减少缓存占用，保留基本容错）
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                // 增加解码选项，提高准确性
                .format(DecodeFormat.PREFER_ARGB_8888)
                // 禁用硬件解码，提高兼容性
                .disallowHardwareConfig()
            // 开始尝试加载视频1s的图片
            Glide.with(view.context)
                .setDefaultRequestOptions(options)
                .load(videoUrl)
                .smartFade(view)
                .into(view)
        } catch (e: Exception) {
            e.printStackTrace()
            view.setBackgroundResource(DEFAULT_MASK_RESOURCE)
        }
    }

    /**
     * 加载图片并捕获加载进度
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址，不能为空
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadProgress 图片加载进度的回调
     * @param onLoadResult 图片加载结果的回调，true 表示加载成功，false 表示失败
     */
    fun loadImageWithProgress(view: ImageView?, imageUrl: String, onLoadStart: () -> Unit = {}, onLoadProgress: (progress: Int?) -> Unit = {}, onLoadResult: (result: Boolean) -> Unit = {}) {
        view ?: return
        /**
         * 避免频繁创建协程
         * callbackFlow 是 Kotlin 协程中专门用于将回调式 API 转换为流的构建器。它允许你在回调函数中向流中发射数据，并且可以处理流的关闭操作。
         * callbackFlow 构建器内部会创建一个 SendChannel，你可以通过 trySend 方法向这个通道发送数据，这些数据会作为流中的元素被发射出去。
         * 当流被收集时，callbackFlow 内部的代码会开始执行，通常会在这里注册回调函数。
         * 当回调函数被触发时，调用 trySend 方法将数据发送到流中。
         * 当流不再被收集或者需要关闭时，awaitClose 方法会被调用，你可以在 awaitClose 中进行资源清理操作，比如取消回调注册。
         */
        val progressFlow = createProgressFlow(imageUrl)
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
            .addListener(object : GlideRequestListener<Drawable>() {
                override fun onLoadStart() {
                    scope.launch {
                        progressFlow.flowOn(Main.immediate).catch {
                            it.printStackTrace()
                        }.collect { progress ->
                            onLoadProgress(progress)
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

    private fun createProgressFlow(imageUrl: String) = callbackFlow {
        ProgressInterceptor.addListener(imageUrl) {
            trySend(it)
        }
        awaitClose {
            ProgressInterceptor.removeListener(imageUrl)
        }
    }

    /**
     * 加载图片并根据设置的宽度等比例拉伸高度
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Bitmap
     */
    fun loadScaledImage(view: ImageView?, imageUrl: String?, onLoadStart: () -> Unit, onLoadComplete: (bitmap: Bitmap?) -> Unit) {
        view ?: return
        Glide.with(view.context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(DEFAULT_MASK_RESOURCE)
//            .dontAnimate()
            .smartFade(view)
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

    /**
     * 加载网络 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifUrl GIF 图片的 URL 地址
     */
    fun loadGifFromUrl(view: ImageView?, gifUrl: String?) {
        view ?: return
        Glide.with(view.context)
            .asGif()
            .load(gifUrl)
            .into(view)
    }

    /**
     * 加载本地 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifResource 本地 GIF 图片的资源 ID
     */
    fun loadGifFromResource(view: ImageView?, gifResource: Int?) {
        view ?: return
        Glide.with(view.context)
            .asGif()
            .load(gifResource)
            .into(view)
    }

    /**
     * 加载图片，支持不同的展示方式（URL 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int? = DEFAULT_RESOURCE, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImageDrawableFromUrl(view, imageUrl, view?.context?.drawable(errorResource.orZero), onLoadStart, onLoadComplete)
    }

    /**
     * 加载图片，支持不同的展示方式（资源 ID 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageResource 图片的资源 ID
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int? = DEFAULT_RESOURCE, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImageDrawableFromResource(view, view?.context?.drawable(imageResource.orZero), view?.context?.drawable(errorResource.orZero), onLoadStart, onLoadComplete)
    }

    /**
     * 加载图片，支持不同的展示方式（Drawable 方式，URL 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable? = getDefaultDrawable(view), onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .placeholder(DEFAULT_RESOURCE)
            .error(errorDrawable)
//            .dontAnimate()
            .smartFade(view)
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

    /**
     * 加载图片，支持不同的展示方式（Drawable 方式，资源 ID 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageDrawable 图片的 Drawable
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Drawable
     */
    fun loadImageDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable? = getDefaultDrawable(view), onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        view ?: return
        Glide.with(view.context)
            .load(imageDrawable)
            .placeholder(DEFAULT_RESOURCE)
            .error(errorDrawable)
//            .dontAnimate()
            .smartFade(view)
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

    /**
     * 加载圆角图片（URL 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param cornerRadius 圆角半径
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int? = DEFAULT_ROUNDED_RESOURCE, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS) {
        loadRoundedDrawableFromUrl(view, imageUrl, view?.context?.drawable(errorResource.orZero), cornerRadius, overrideCorners)
    }

    /**
     * 加载圆角图片（资源 ID 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageResource 图片的资源 ID
     * @param errorResource 加载失败时显示的错误图片资源 ID
     * @param cornerRadius 圆角半径
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int? = DEFAULT_ROUNDED_RESOURCE, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS) {
        loadRoundedDrawableFromResource(view, view?.context?.drawable(imageResource.orZero), view?.context?.drawable(errorResource.orZero), cornerRadius, overrideCorners)
    }

    /**
     * 加载圆角图片（Drawable 方式，URL 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param cornerRadius 圆角半径，默认值为 5
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable? = getDefaultRoundedDrawable(view), cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .also {
                if (cornerRadius > 0) {
                    val cornerTransform = CornerTransform(view.context, cornerRadius.toSafeFloat())
                    cornerTransform.setExceptCorner(overrideCorners)
                    it.apply(RequestOptions.bitmapTransform(cornerTransform))
                }
            }
            .placeholder(DEFAULT_ROUNDED_RESOURCE)
            .error(errorDrawable)
//            .dontAnimate()
            .smartFade(view)
            .into(view)
    }

    /**
     * 加载圆角图片（Drawable 方式，资源 ID 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageDrawable 图片的 Drawable
     * @param errorDrawable 加载失败时显示的错误 Drawable
     * @param cornerRadius 圆角半径，默认值为 5
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable? = getDefaultRoundedDrawable(view), cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS) {
        view ?: return
        Glide.with(view.context)
            .load(imageDrawable)
            .also {
                if (cornerRadius > 0) {
                    val cornerTransform = CornerTransform(view.context, cornerRadius.toSafeFloat())
                    cornerTransform.setExceptCorner(overrideCorners)
                    it.apply(RequestOptions.bitmapTransform(cornerTransform))
                }
            }
            .placeholder(DEFAULT_ROUNDED_RESOURCE)
            .error(errorDrawable)
//            .dontAnimate()
            .smartFade(view)
            .into(view)
    }

    /**
     * 加载圆形图片（URL 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorResource 加载失败时显示的错误图片资源 ID
     */
    fun loadCircularImageFromUrl(view: ImageView?, imageUrl: String?, errorResource: Int? = DEFAULT_CIRCULAR_RESOURCE) {
        loadCircularDrawableFromUrl(view, imageUrl, view?.context?.drawable(errorResource.orZero))
    }

    /**
     * 加载圆形图片（资源 ID 方式）
     * @param view 用于显示图片的 ImageView
     * @param imageResource 图片的资源 ID
     * @param errorResource 加载失败时显示的错误图片资源 ID
     */
    fun loadCircularImageFromResource(view: ImageView?, imageResource: Int?, errorResource: Int? = DEFAULT_CIRCULAR_RESOURCE) {
        loadCircularDrawableFromResource(view, view?.context?.drawable(imageResource.orZero), view?.context?.drawable(errorResource.orZero))
    }

    /**
     * 加载圆形图片（Drawable 方式，URL 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageUrl 图片的 URL 地址
     * @param errorDrawable 加载失败时显示的错误 Drawable
     */
    fun loadCircularDrawableFromUrl(view: ImageView?, imageUrl: String?, errorDrawable: Drawable? = getDefaultCircularDrawable(view)) {
        view ?: return
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(DEFAULT_CIRCULAR_RESOURCE)
            .error(errorDrawable)
//            .dontAnimate()
            .smartFade(view)
            .into(view)
    }

    /**
     * 加载圆形图片（Drawable 方式，资源 ID 来源）
     * @param view 用于显示图片的 ImageView
     * @param imageDrawable 图片的 Drawable
     * @param errorDrawable 加载失败时显示的错误 Drawable
     */
    fun loadCircularDrawableFromResource(view: ImageView?, imageDrawable: Drawable?, errorDrawable: Drawable? = getDefaultCircularDrawable(view)) {
        view ?: return
        Glide.with(view.context)
            .load(imageDrawable)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(DEFAULT_CIRCULAR_RESOURCE)
            .error(errorDrawable)
//            .dontAnimate()
            .smartFade(view)
            .into(view)
    }

    /**
     * 获取图片缓存目录
     * @param context 上下文对象
     * @return 图片缓存目录的 File 对象，如果获取失败返回 null
     */
    fun getImageCacheDir(context: Context): File? {
        return Glide.getPhotoCacheDir(context)
    }

    /**
     * 清除内存缓存
     * @param context 上下文对象
     * @param owner 生命周期所有者，用于管理协程等操作
     */
    fun clearMemoryCache(context: Context, owner: LifecycleOwner) {
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
     * 清除磁盘缓存
     * @param context 上下文对象
     * @param owner 生命周期所有者，用于管理协程等操作
     */
    fun clearDiskCache(context: Context, owner: LifecycleOwner) {
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
     * 下载图片
     * @param context 上下文对象
     * @param imageUrl 图片的 URL 地址
     * @param onDownloadStart 下载开始时的回调
     * @param onDownloadComplete 下载完成时的回调，返回下载的文件
     */
    fun downloadImage(context: Context, imageUrl: String? = null, onDownloadStart: () -> Unit = {}, onDownloadComplete: (file: File?) -> Unit = {}) {
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