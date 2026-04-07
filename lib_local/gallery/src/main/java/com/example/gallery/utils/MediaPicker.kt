package com.example.gallery.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.common.base.page.ResultCode.RESULT_ALBUM
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.mb
import com.example.common.utils.function.string
import com.example.framework.utils.function.color
import com.example.framework.utils.function.value.hour
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toNewList
import com.example.gallery.R
import com.example.gallery.activity.CameraActivity
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_BYTES
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_DURATION
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_FUNCTION
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_FUNCTION_ALBUM
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_FUNCTION_IMAGE
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_FUNCTION_VIDEO
import com.example.gallery.activity.CameraActivity.Companion.CAMERA_QUALITY
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.api.ImageMultipleWrapper
import com.yanzhenjie.album.api.ImageSingleWrapper
import com.yanzhenjie.album.api.VideoMultipleWrapper
import com.yanzhenjie.album.api.VideoSingleWrapper
import com.yanzhenjie.album.api.choice.Choice
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.durban.model.Controller
import com.yanzhenjie.durban.Durban

/**
 * author: wyb
 * date: 2017/9/29.
 * 调用该类之前需检测权限，activity属性设为
 * android:configChanges="orientation|keyboardHidden|screenSize"
 */
class MediaPicker {
    private var context: Context? = null
    private var widget: Widget? = null
    private var durban: Durban? = null
    //    private var imageCamera: Camera<ImageCameraWrapper, VideoCameraWrapper>? = null
    private var imageMultiple: Choice<ImageMultipleWrapper, ImageSingleWrapper>? = null
    private var videoMultiple: Choice<VideoMultipleWrapper, VideoSingleWrapper>? = null

    companion object {
        /**
         * 计算适合第三方库aspectRatio(x: Float, y: Float)方法的浮点参数
         * @return Pair<Float, Float> 宽比例和高比例的浮点形式（如16:9返回(16f, 9f)）
         */
        @JvmStatic
        fun calculateFloatAspectRatio(width: Int, height: Int): Pair<Float, Float> {
            if (width == 0 || height == 0) {
                return 0f to 0f // 处理无效值
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
    }

    /**
     * 1) AppCompatActivity和Fragment在裁剪或者OnActivityResult时是必须指明的，不然返回会错误
     * 2) FragmentActivity无需指明,在Activity中传this,在Fragment传getActivity(),系统会做发起判断
     */
    constructor(any: Any) {
        when (any) {
            is AppCompatActivity, is FragmentActivity -> {
//                imageCamera = Album.camera(any)
                videoMultiple = Album.video(any)
                imageMultiple = Album.image(any)
                widget = any.getAlbumWidget()
                durban = Durban.with(any)
            }
            is Fragment -> {
//                imageCamera = Album.camera(any)
                videoMultiple = Album.video(any)
                imageMultiple = Album.image(any)
                widget = any.context.getAlbumWidget()
                durban = Durban.with(any)
            }
        }
    }

    /**
     * 创建相册统一配置
     */
    private fun Context?.getAlbumWidget(barColor: Int = R.color.bgBlack): Widget? {
        this ?: return null
        context = this
        // 参考Widget -> getDefaultWidget()方法
        return Widget.newDarkBuilder(this)
            // 状态栏颜色
            .statusBarColor(barColor)
            // 导航栏颜色
            .navigationBarColor(barColor)
            // 标题 --- 标题文字颜色只有黑色白色
            .title(string(R.string.albumTitle))
            // 媒体条目选择框颜色
            .mediaItemCheckSelector(color(R.color.btnMainDisabled), color(R.color.btnMain))
            // 文件夹条目选择框颜色
            .bucketItemCheckSelector(color(R.color.btnMainDisabled), color(R.color.btnMain))
            // 按钮样式
            .buttonStyle(Widget.ButtonStyle
                .newDarkBuilder(this)
                .setButtonSelector(color(R.color.btnMain), color(R.color.btnMain)).build())
            // 构建配置
            .build()
    }

    /**
     * 跳转至相机-拍照
     */
    fun takePicture(hasDurban: Boolean = false, listener: (albumPath: String) -> Unit = {}) {
//        imageCamera?.image()
//            ?.filePath(root)
//            ?.onResult {
//                if (hasDurban) {
//                    toDurban(it)
//                } else {
//                    listener.invoke(it)
//                }
//            }
//            ?.start()
        CameraActivity.onResult = {
            if (hasDurban) {
                toDurban(it)
            } else {
                listener.invoke(it)
            }
        }
        val intent = Intent(context, CameraActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(CAMERA_FUNCTION, CAMERA_FUNCTION_IMAGE)
        context?.startActivity(intent)
    }

    /**
     * 跳转至相机-录像
     */
    fun recordVideo(maxDurationMs: Long = 1.hour, maxSizeMb: Long = 10L, quality: Int = 0, listener: (albumPath: String) -> Unit = {}) {
//        imageCamera?.video()
//            // 视频输出路径
//            ?.filePath(root)
//            // 视频质量, [0, 1].
//            ?.quality(1)
//            // 视频的最长持续时间以毫秒为单位
//            ?.limitDuration(duration)
////            // 视频的最大大小，以字节为单位
////            .limitBytes(Long.MAX_VALUE)
//            // 完成回调
//            ?.onResult {
//                listener.invoke(it)
//            }
//            ?.start()
        CameraActivity.onResult = {
            listener.invoke(it)
        }
        val intent = Intent(context, CameraActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(CAMERA_FUNCTION, CAMERA_FUNCTION_VIDEO)
        intent.putExtra(CAMERA_QUALITY, quality)
        intent.putExtra(CAMERA_DURATION, maxDurationMs)
        intent.putExtra(CAMERA_BYTES, maxSizeMb)
        context?.startActivity(intent)
    }

    /**
     * 选择图片-系统相册
     */
    fun pickImage(hasDurban: Boolean = false, listener: (albumPath: String) -> Unit = {}) {
        CameraActivity.onResult = {
            if (hasDurban) {
                toDurban(it)
            } else {
                listener.invoke(it)
            }
        }
        val intent = Intent(context, CameraActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(CAMERA_FUNCTION, CAMERA_FUNCTION_ALBUM)
        context?.startActivity(intent)
    }

    /**
     * 选择图片-单选
     */
    fun imageSelection(hasCamera: Boolean = true, hasDurban: Boolean = false, megabyte: Long = 10L, listener: (albumPath: String) -> Unit = {}) {
        imageMultiple
            // 多选模式为：multipleChoice(同时添加?.apply { multipleChoice()?.selectCount(100) }设定上限),单选模式为：singleChoice()
            ?.singleChoice()
            // 状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
            ?.widget(widget)
            // 是否具备相机
            ?.camera(hasCamera)
            // 页面列表的列数
            ?.columnCount(3)
            // 防止加载系统缓存图片
            ?.filterSize { it == 0L }
            // 筛选文件的可见性
            ?.afterFilterVisibility(false)
            // 选择后回调
            ?.onResult {
                it.safeGet(0)?.apply {
                    if (size > megabyte.mb) {
                        string(R.string.albumImageError, megabyte.mb.toString()).shortToast()
                        return@onResult
                    }
                    if (hasDurban) {
                        toDurban(path)
                    } else {
                        listener.invoke(path)
                    }
                }
            }
            ?.start()
    }

    /**
     * 选择图片-多选
     */
    fun imageMultipleSelection(hasCamera: Boolean = true, selectCount: Int = 100, listener: (list: ArrayList<String>) -> Unit = {}) {
        imageMultiple
            ?.multipleChoice()
            ?.selectCount(selectCount)
            ?.widget(widget)
            ?.camera(hasCamera)
            ?.columnCount(3)
            ?.filterSize { it == 0L }
            ?.afterFilterVisibility(false)
            ?.onResult { result ->
                listener.invoke(result.toNewList { it.path })
            }
            ?.start()
    }

    /**
     * 选择视频-单选
     */
    fun videoSelection(megabyte: Long = 100L, listener: (albumPath: String) -> Unit = {}) {
        videoMultiple
            ?.singleChoice()
            ?.widget(widget)
            ?.camera(true)
            ?.columnCount(3)
            ?.filterSize { it == 0L }
            ?.afterFilterVisibility(false)
            ?.onResult {
                it.safeGet(0)?.apply {
                    if (size > megabyte.mb) {
                        string(R.string.albumVideoError, megabyte.mb.toString()).shortToast()
                        return@onResult
                    }
                    listener.invoke(path)
                }
            }
            ?.start()
    }

    /**
     * 选择视频-多选
     */
    fun videoMultipleSelection(selectCount: Int = 100, listener: (list: ArrayList<String>) -> Unit = {}) {
        videoMultiple
            ?.multipleChoice()
            ?.selectCount(selectCount)
            ?.widget(widget)
            ?.camera(true)
            ?.columnCount(3)
            ?.filterSize { it == 0L }
            ?.afterFilterVisibility(false)
            ?.onResult { result ->
                listener.invoke(result.toNewList { it.path })
            }
            ?.start()
    }

    /**
     * 开始裁剪
     * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     * super.onActivityResult(requestCode, resultCode, data)
     * if (requestCode == RESULT_ALBUM) {
     * data ?: return
     * val mImageList = Durban.parseResult(data)
     * mImageList.safeGet(0).shortToast()
     * }
     * }
     */
    fun toDurban(vararg imagePathArray: String, width: Int = 500, height: Int = 500, x: Float = 1f, y: Float = 1f, quality: Int = 80, @Durban.FormatTypes format: Int = Durban.COMPRESS_JPEG, @Durban.GestureTypes gesture: Int = Durban.GESTURE_SCALE, controller: Boolean = false) {
        durban
            // 裁剪界面的标题
            ?.title(string(R.string.durbanTitle))
            // 状态栏颜色
            ?.statusBarColor(R.color.bgBlack)
            // 导航栏颜色
            ?.navigationBarColor(R.color.bgBlack)
            // 图片路径list或者数组
            ?.inputImagePaths(*imagePathArray)
            // 图片输出文件夹路径
            ?.outputDirectory(getStoragePath("裁剪图片"))
            // 裁剪图片输出的最大宽高
            ?.maxWidthHeight(width, height)
            // 裁剪时的宽高比
            ?.aspectRatio(x, y)
            // 图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
            ?.compressQuality(quality)
            // 图片压缩格式：JPEG、PNG
            ?.compressFormat(format)
            // 裁剪时的手势支持：ROTATE, SCALE, ALL, NONE.
            ?.gesture(gesture)
            // 底部操作栏配置
            ?.controller(Controller.newBuilder()
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
            ?.requestCode(RESULT_ALBUM)
            // 开始跳转
            ?.start()
    }

}