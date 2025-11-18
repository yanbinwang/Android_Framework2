package com.example.glide

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.cardview.widget.CardView
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
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.RequestOptions
import com.example.framework.utils.PropertyAnimator
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.size
import com.example.glide.callback.GlideRequestListener
import com.example.glide.callback.progress.ProgressInterceptor
import com.example.glide.transform.CornerTransform
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
         * 获取默认弧形遮罩背景
         */
        @JvmStatic
        val DEFAULT_CORNER_COLOR = Color.WHITE

        /**
         * 圆角图片4个边是否都是弧线
         */
        @JvmStatic
        val DEFAULT_OVERRIDE_CORNERS = booleanArrayOf(false, false, false, false)

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

        /**
         * 获取弧度变化配置
         */
        private fun getCornerTransform(view: ImageView?, cornerRadius: Int, overrideCorners: BooleanArray, overrideColor: Int): RequestOptions? {
            view ?: return null
            return if (cornerRadius > 0) {
                val transformation = CornerTransform(view.context, overrideCorners, cornerRadius.toSafeFloat(), overrideColor)
                RequestOptions.bitmapTransform(transformation)
            } else {
                null
            }
        }

        /**
         * 外层嵌套CardView内部保证获取对应的唯一ImageView
         */
        private fun getCardViewImage(view: CardView?): ImageView? {
            view ?: return null
            view.removeAllViews()
            val imageView = ImageView(view.context)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            view.addView(imageView)
            imageView.size(MATCH_PARENT, MATCH_PARENT)
            return imageView
        }

        /**
         * 根据图片类型获取对应的默认占位图/错误图
         */
        private fun getDefaultResourceByType(imageType: ImageType): Int {
            return when (imageType) {
                ImageType.ROUNDED -> DEFAULT_ROUNDED_RESOURCE
                ImageType.CIRCULAR -> DEFAULT_CIRCULAR_RESOURCE
                ImageType.NORMAL -> DEFAULT_RESOURCE
            }
        }

        /**
         * 图片展示样式枚举
         */
        private enum class ImageType {
            NORMAL, // 普通图（无特殊样式）
            ROUNDED, // 圆角图（需要圆角参数）
            CIRCULAR // 圆形图（无需额外参数，用 Glide 的 circleCrop）
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
    fun loadVideoFrameFromUrl(view: ImageView?, videoUrl: String?, frameTimeMicros: Long = 1000000000, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
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
                .listener(object : GlideRequestListener<Drawable>() {
                    override fun onLoadStart() {
                        onLoadStart()
                    }

                    override fun onLoadFinished(resource: Drawable?) {
                        onLoadComplete(resource)
                    }
                })
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
     * @param onLoadComplete 图片加载结果的回调，true 表示加载成功，false 表示失败
     */
    fun loadProgressFromUrl(view: ImageView?, imageUrl: String, onLoadStart: () -> Unit = {}, onLoadProgress: (progress: Int?) -> Unit = {}, onLoadComplete: (resource: Drawable?) -> Unit = {}) {
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
            .apply(RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE))
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
                    onLoadComplete(resource)
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
     * @param error 参数支持 Int（资源 ID）/Drawable/null
     * @param onLoadStart 图片开始加载时的回调
     * @param onLoadComplete 图片加载完成时的回调，返回加载的 Bitmap
     */
    fun loadScaledFromUrl(view: ImageView?, imageUrl: String?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (bitmap: Bitmap?) -> Unit = {}) {
        view ?: return
        view.doOnceAfterLayout {
            // 图片加载错误资源 (@DrawableRes / Drawable)
            val defaultResource = getDefaultResourceByType(ImageType.NORMAL)
            val validErrorSource = when (error) {
                is Int -> if (error != 0) error else null
                is Drawable -> error.takeIf { it.isVisible }
                else -> null
            } ?: defaultResource
            // 加载伸缩图片
            Glide.with(view.context)
                .asBitmap()
                .load(imageUrl)
                .apply(RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE))
                .placeholder(defaultResource)
                .error(validErrorSource)
//                .smartFade(view)
                .listener(object : GlideRequestListener<Bitmap>() {
                    override fun onLoadStart() {
                        onLoadStart()
                        view.gone()
                    }

                    override fun onLoadFinished(resource: Bitmap?) {
                        if (null != resource) {
                            transform(view, resource, onLoadComplete)
                        } else {
                            view.appear()
                            onLoadComplete(resource)
                        }
                    }
                })
                .into(view)
        }
    }

    private fun transform(target: ImageView, resource: Bitmap, onLoadComplete: (bitmap: Bitmap?) -> Unit = {}) {
        // 执行渐隐藏动画
        target.appear()
        // 获取原图宽高
        val originalWidth = resource.width
        val originalHeight = resource.height
        // 获取ImageView宽高（此时已确保布局完成）
        val targetWidth = target.width
        // 安全校验：避免原图宽高为0导致的异常
        if (originalWidth <= 0 || originalHeight <= 0 || targetWidth <= 0) {
            return
        }
        // 计算缩放比例
        val scale = targetWidth.toFloat() / originalWidth.toFloat()
        // 计算目标高度（保持比例）
        val targetHeight = (originalHeight * scale).toInt()
//        // 调整高度
//        target.layoutParams?.height = targetHeight
//        // 返回
//        onLoadComplete(resource)
        // 执行伸缩动画
        PropertyAnimator(target, 300)
            .animateHeight(originalHeight, targetHeight)
            .start(onEnd = {
                onLoadComplete(resource)
            })
    }

    /**
     * 加载网络 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifUrl GIF 图片的 URL 地址
     */
    fun loadGifFromUrl(view: ImageView?, gifUrl: String?) {
        loadGif(view, gifUrl)
    }

    /**
     * 加载本地 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifResource 本地 GIF 图片的资源 ID
     */
    fun loadGifFromResource(view: ImageView?, @RawRes @DrawableRes gifResource: Int?) {
        loadGif(view, gifResource)
    }

    /**
     * 加载本地 GIF 图片
     * @param view 用于显示 GIF 图片的 ImageView
     * @param gifDrawable 本地 GIF 图片的资源 ID
     */
    fun loadGifFromDrawable(view: ImageView?, gifDrawable: Drawable?) {
        loadGif(view, gifDrawable)
    }

    private fun loadGif(view: ImageView?, source: Any?) {
        // 本身不可为空
        view ?: return
        // 过滤无效来源，避免 Glide 加载异常
        val validSource = when (source) {
            // 排除无效资源 ID（0 是默认无效值）
            is Int -> if (source != 0) source else null
            // 排除空字符串 URL
            is String -> source.ifBlank { null }
            // 排除不可见的无效Drawable
            is Drawable -> source.takeIf { it.isVisible }
            // 余下一律为null
            else -> null
            // 无效来源直接返回，避免无意义加载
        } ?: return
        // 开始加载
        Glide.with(view.context)
            .asGif()
            .load(validSource)
            .into(view)
    }

    /**
     * 加载图片，支持不同的展示方式（URL/资源 ID/Drawable 方式）
     */
    fun loadImageFromUrl(view: ImageView?, imageUrl: String?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageUrl, error, onLoadStart = onLoadStart, onLoadComplete = onLoadComplete)
    }

    fun loadImageFromResource(view: ImageView?, @RawRes @DrawableRes imageResource: Int?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageResource, error, onLoadStart = onLoadStart, onLoadComplete = onLoadComplete)
    }

    fun loadImageFromDrawable(view: ImageView?, imageDrawable: Drawable?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageDrawable, error, onLoadStart = onLoadStart, onLoadComplete = onLoadComplete)
    }

    /**
     * 加载圆形图片
     * @param cornerRadius 圆角半径
     * @param overrideCorners 用于指定是否覆盖某些角的圆角设置，长度为 4 的布尔数组，顺序为左上、右上、右下、左下
     */
    fun loadRoundedImageFromUrl(view: ImageView?, imageUrl: String?, error: Any? = null, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS, overrideColor: Int = DEFAULT_CORNER_COLOR, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageUrl, error, getCornerTransform(view, cornerRadius, overrideCorners, overrideColor), ImageType.ROUNDED, onLoadStart, onLoadComplete)
    }

    fun loadRoundedImageFromResource(view: ImageView?, @RawRes @DrawableRes imageResource: Int?, error: Any? = null, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS, overrideColor: Int = DEFAULT_CORNER_COLOR, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageResource, error, getCornerTransform(view, cornerRadius, overrideCorners, overrideColor), ImageType.ROUNDED, onLoadStart, onLoadComplete)
    }

    fun loadRoundedImageFromDrawable(view: ImageView?, imageDrawable: Drawable?, error: Any? = null, cornerRadius: Int = DEFAULT_CORNER_RADIUS, overrideCorners: BooleanArray = DEFAULT_OVERRIDE_CORNERS, overrideColor: Int = DEFAULT_CORNER_COLOR, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageDrawable, error, getCornerTransform(view, cornerRadius, overrideCorners, overrideColor), ImageType.ROUNDED, onLoadStart, onLoadComplete)
    }

    /**
     * 加载圆形图片
     * 高版本安卓对于圆角绘制会带有阴影,故而做特殊处理,CardView绘制完成后,内部加载图片使用该方法
     */
    fun loadCardViewFromUrl(view: CardView?, imageUrl: String?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(getCardViewImage(view), imageUrl, error, onLoadStart = onLoadStart, onLoadComplete = onLoadComplete)
    }

    fun loadCardViewFromResource(view: CardView?, @RawRes @DrawableRes imageResource: Int?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(getCardViewImage(view), imageResource, error, onLoadStart = onLoadStart, onLoadComplete = onLoadComplete)
    }

    fun loadCardViewFromDrawable(view: CardView?, imageDrawable: Drawable?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(getCardViewImage(view), imageDrawable, error, onLoadStart = onLoadStart, onLoadComplete = onLoadComplete)
    }

    /**
     * 加载圆形图片
     */
    fun loadCircularImageFromUrl(view: ImageView?, imageUrl: String?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageUrl, error, RequestOptions.circleCropTransform(), ImageType.CIRCULAR, onLoadStart, onLoadComplete)
    }

    fun loadCircularImageFromResource(view: ImageView?, @RawRes @DrawableRes imageResource: Int?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageResource, error, RequestOptions.circleCropTransform(), ImageType.CIRCULAR, onLoadStart, onLoadComplete)
    }

    fun loadCircularImageFromDrawable(view: ImageView?, imageDrawable: Drawable?, error: Any? = null, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        loadImage(view, imageDrawable, error, RequestOptions.circleCropTransform(), ImageType.CIRCULAR, onLoadStart, onLoadComplete)
    }

    private fun loadImage(view: ImageView?, source: Any?, errorSource: Any?, requestOptions: BaseRequestOptions<*>? = null, imageType: ImageType = ImageType.NORMAL, onLoadStart: () -> Unit = {}, onLoadComplete: (drawable: Drawable?) -> Unit = {}) {
        view ?: return
        // 图片资源
        val validSource = when (source) {
            is Int -> if (source != 0) source else null
            is String -> source.ifBlank { null }
            is Drawable -> source.takeIf { it.isVisible }
            else -> null
        } ?: return
        // 图片加载错误资源 (@DrawableRes / Drawable)
        val defaultResource = getDefaultResourceByType(imageType)
        val validErrorSource = when (errorSource) {
            is Int -> if (errorSource != 0) errorSource else null
            is Drawable -> errorSource.takeIf { it.isVisible }
            else -> null
        } ?: defaultResource
        // Glide 加载配置：占位图直接用工具方法返回的默认资源
        Glide.with(view.context)
            .load(validSource)
            .also {
                if (null != requestOptions) {
                    it.apply(requestOptions)
                }
            }
            .placeholder( defaultResource)
            .error(validErrorSource)
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