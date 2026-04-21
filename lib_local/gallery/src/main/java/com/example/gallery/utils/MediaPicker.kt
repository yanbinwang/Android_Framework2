package com.example.gallery.utils

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.common.base.page.ResultCode.RESULT_ALBUM
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.mb
import com.example.common.utils.function.string
import com.example.framework.utils.function.color
import com.example.framework.utils.function.value.hour
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toNewList
import com.example.gallery.R
import com.example.gallery.activity.CameraActivity.Companion.pickImage
import com.example.gallery.activity.CameraActivity.Companion.recordVideo
import com.example.gallery.activity.CameraActivity.Companion.takePicture
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.callback.Action
import com.example.gallery.feature.album.callback.Filter
import com.example.gallery.feature.album.model.AlbumFile
import com.example.gallery.feature.album.model.Widget
import com.yanzhenjie.durban.Durban
import com.yanzhenjie.durban.model.Controller

/**
 * author: wyb
 * date: 2017/9/29.
 * 1) 调用该类之前需检测权限，Activity属性设为
 *   android:configChanges="orientation|keyboardHidden|screenSize"
 * 2) 相机跳转在相册内部任务栈共享一个窗体所以不会出问题,外部直接调取是会污染window类的
 *   private var imageCamera: Camera<ImageCameraWrapper, VideoCameraWrapper>
 *   imageCamera = Album.camera(any)
 *   imageCamera?.image()
 *       ?.filePath(root)
 *       ?.onResult {
 *           if (hasDurban) {
 *               toDurban(it)
 *           } else {
 *               listener.invoke(it)
 *           }
 *       }
 *       ?.start()
 *
 *   imageCamera?.video()
 *       // 视频输出路径
 *       ?.filePath(root)
 *       // 视频质量, [0, 1].
 *       ?.quality(1)
 *       // 视频的最长持续时间以毫秒为单位
 *       ?.limitDuration(duration)
 *       // 视频的最大大小，以字节为单位
 *       .limitBytes(Long.MAX_VALUE)
 *       // 完成回调
 *       ?.onResult {
 *           listener.invoke(it)
 *       }
 *       ?.start()
 * 3) AppCompatActivity和Fragment在裁剪或者OnActivityResult时是必须指明的，不然返回会错误
 * 4) FragmentActivity无需指明,在Activity中传this,在Fragment传getActivity(),系统会做发起判断
 * 5) 不支持旧android.app.Fragment
 */
class MediaPicker(private val host: Any) {
    private val context: Context get() = when (host) {
        is FragmentActivity -> host
        is Fragment -> host.requireActivity()
        is android.app.Fragment -> throw RuntimeException("android.app.Fragment 已废弃，不支持！")
        else -> throw IllegalArgumentException("不支持的类型：${host::class.java.name}")
    }
    private val widget by lazy { context.getAlbumWidget() }
    private val imageMultiple by lazy { Album.image(host) }
    private val videoMultiple by lazy { Album.video(host) }
    private val durban by lazy { Durban.with(host) }

    companion object {
        /**
         * 状态栏
         */
        private val statusBarColorRes get() = R.color.appStatusBar

        /**
         * 导航栏
         */
        private val navigationBarColor get() = R.color.appNavigationBar

        /**
         * 计算适合第三方库aspectRatio(x: Float, y: Float)方法的浮点参数
         * @return Pair<Float, Float> 宽比例和高比例的浮点形式（如16:9返回(16f, 9f)）
         */
        @JvmStatic
        fun calculateFloatAspectRatio(width: Int, height: Int): Pair<Float, Float> {
            if (width == 0 || height == 0) {
                // 处理无效值
                return 0f to 0f
            }
            // 计算最大公约数简化比例
            val gcd = gcd(width, height)
            val simplifiedWidth = width / gcd
            val simplifiedHeight = height / gcd
            // 转换为浮点型返回
            return simplifiedWidth.toFloat() to simplifiedHeight.toFloat()
        }

        // 最大公约数计算（复用之前的实现）
        private fun gcd(a: Int, b: Int): Int {
            return if (b == 0) a else gcd(b, a % b)
        }

        /**
         * 创建相册统一配置
         */
        private fun Context?.getAlbumWidget(): Widget? {
            this ?: return null
            // 根据状态栏颜色区分主题
            val style = if (shouldUseWhiteSystemBarsForRes(statusBarColorRes)) Widget.STYLE_DARK else Widget.STYLE_LIGHT
            // 参考Widget -> getDefaultWidget()方法
            return Widget.newBuilder(this, style)
                // 状态栏颜色
                .statusBarColor(statusBarColorRes)
                // 导航栏颜色
                .navigationBarColor(navigationBarColor)
                // 标题 --- 标题文字颜色只有黑色白色
                .title(string(R.string.gallery_album_title))
                // 媒体条目选择框颜色
                .mediaItemCheckSelector(color(R.color.btnMainDisabled), color(R.color.btnMain))
                // 文件夹条目选择框颜色
                .bucketItemCheckSelector(color(R.color.btnMainDisabled), color(R.color.btnMain))
                // 按钮样式
                .buttonSelector(color(R.color.btnMain), color(R.color.btnMain))
                // 构建配置
                .build()
        }

        /**
         * 开启裁剪
         */
        fun Durban.cropImage(vararg imagePathArray: String, width: Int = 500, height: Int = 500, x: Float = 1f, y: Float = 1f, quality: Int = 80, format: Int = Durban.COMPRESS_JPEG, gesture: Int = Durban.GESTURE_SCALE, controller: Boolean = false) {
            // 裁剪界面的标题
            title(string(R.string.gallery_durban_title))
            // 状态栏颜色
            statusBarColor(statusBarColorRes)
            // 导航栏颜色
            navigationBarColor(navigationBarColor)
            // 图片路径list或者数组
            inputImagePaths(*imagePathArray)
            // 图片输出文件夹路径
            outputDirectory(getStoragePath("裁剪图片"))
            // 裁剪图片输出的最大宽高
            maxWidthHeight(width, height)
            // 裁剪时的宽高比
            aspectRatio(x, y)
            // 图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
            compressQuality(quality)
            // 图片压缩格式：JPEG、PNG
            compressFormat(format)
            // 裁剪时的手势支持：ROTATE, SCALE, ALL, NONE.
            gesture(gesture)
            // 底部操作栏配置
            controller(Controller.newBuilder()
                // 是否开启控制面板
                .enable(controller)
                // 是否有旋转按钮
                .rotation(true)
                // 旋转控制按钮上面的标题
                .rotationTitle(true)
                // 是否有缩放按钮
                .scale(true)
                // 缩放控制按钮上面的标题
                .scaleTitle(true)
                // 构建配置
                .build())
            // 创建控制面板配置
            requestCode(RESULT_ALBUM)
            // 开始跳转
            start()
        }
    }

    /**
     * 跳转至相机-拍照
     */
    fun takePicture(hasDurban: Boolean = false, listener: (albumPath: String) -> Unit = {}) {
        context.takePicture { albumPath ->
            if (hasDurban) {
                durban.cropImage(albumPath)
            } else {
                listener.invoke(albumPath)
            }
        }
    }

    /**
     * 跳转至相机-录像
     */
    fun recordVideo(maxDurationMs: Long = 1.hour, maxSizeMb: Long = 10L, quality: Int = 0, listener: (albumPath: String) -> Unit = {}) {
        context.recordVideo(maxDurationMs, maxSizeMb, quality) { albumPath ->
            listener.invoke(albumPath)
        }
    }

    /**
     * 选择图片-系统相册
     */
    fun pickImage(hasDurban: Boolean = false, listener: (albumPath: String) -> Unit = {}) {
        context.pickImage { albumPath ->
            if (hasDurban) {
                durban.cropImage(albumPath)
            } else {
                listener.invoke(albumPath)
            }
        }
    }

    /**
     * 选择图片-单选
     */
    fun imageSelection(hasCamera: Boolean = true, hasDurban: Boolean = false, megabyte: Long = 10L, listener: (albumPath: String) -> Unit = {}) {
        imageMultiple
            // 多选模式为：multipleChoice(同时添加?.apply { multipleChoice()?.selectCount(100) }设定上限),单选模式为：singleChoice()
            .singleChoice()
            // 状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
            .widget(widget)
            // 是否具备相机
            .camera(hasCamera)
            // 页面列表的列数
            .columnCount(3)
            // 筛选文件的可见性
            .afterFilterVisibility(false)
            // 防止加载系统缓存图片
            .filterSize(object : Filter<Long> {
                override fun filter(attributes: Long): Boolean {
                    return attributes == 0L
                }
            })
            // 选择后回调
            .onResult(object : Action<ArrayList<AlbumFile>> {
                override fun onAction(result: ArrayList<AlbumFile>) {
                    result.safeGet(0)?.also { file ->
                        if (file.size > megabyte.mb) {
                            string(R.string.gallery_album_image_error, megabyte.mb.toString()).shortToast()
                            return@also
                        }
                        file.path?.let { albumPath ->
                            if (hasDurban) {
                                durban.cropImage(albumPath)
                            } else {
                                listener.invoke(albumPath)
                            }
                        }
                    }
                }
            })
            .start()


    }

    /**
     * 选择图片-多选
     */
    fun imageMultipleSelection(hasCamera: Boolean = true, selectCount: Int = 100, listener: (list: ArrayList<String>) -> Unit = {}) {
        imageMultiple
            .multipleChoice()
            .selectCount(selectCount)
            .widget(widget)
            .camera(hasCamera)
            .columnCount(3)
            .afterFilterVisibility(false)
            .filterSize(object : Filter<Long> {
                override fun filter(attributes: Long): Boolean {
                    return attributes == 0L
                }
            })
            .onResult(object : Action<ArrayList<AlbumFile>> {
                override fun onAction(result: ArrayList<AlbumFile>) {
                    listener.invoke(result.toNewList { it.path })
                }
            })
            .start()
    }

    /**
     * 选择视频-单选
     */
    fun videoSelection(megabyte: Long = 100L, listener: (albumPath: String) -> Unit = {}) {
        videoMultiple
            .singleChoice()
            .widget(widget)
            .camera(true)
            .columnCount(3)
            .afterFilterVisibility(false)
            .filterSize(object : Filter<Long> {
                override fun filter(attributes: Long): Boolean {
                    return attributes == 0L
                }
            })
            .onResult(object : Action<ArrayList<AlbumFile>> {
                override fun onAction(result: ArrayList<AlbumFile>) {
                    result.safeGet(0)?.also { file ->
                        if (file.size > megabyte.mb) {
                            string(R.string.gallery_album_video_error, megabyte.mb.toString()).shortToast()
                            return@also
                        }
                        file.path?.let { albumPath ->
                            listener.invoke(albumPath)
                        }
                    }
                }
            })
            .start()
    }

    /**
     * 选择视频-多选
     */
    fun videoMultipleSelection(selectCount: Int = 100, listener: (list: ArrayList<String>) -> Unit = {}) {
        videoMultiple
            .multipleChoice()
            .selectCount(selectCount)
            .widget(widget)
            .camera(true)
            .columnCount(3)
            .afterFilterVisibility(false)
            .filterSize(object : Filter<Long> {
                override fun filter(attributes: Long): Boolean {
                    return attributes == 0L
                }
            })
            .onResult(object : Action<ArrayList<AlbumFile>> {
                override fun onAction(result: ArrayList<AlbumFile>) {
                    listener.invoke(result.toNewList { it.path })
                }
            })
            .start()
    }

}