package com.example.gallery.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.common.base.page.ResultCode.RESULT_ALBUM
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.mb
import com.example.common.utils.function.string
import com.example.framework.utils.function.value.hour
import com.example.framework.utils.function.value.safeGet
import com.example.gallery.R
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.api.ImageCameraWrapper
import com.yanzhenjie.album.api.ImageMultipleWrapper
import com.yanzhenjie.album.api.ImageSingleWrapper
import com.yanzhenjie.album.api.VideoCameraWrapper
import com.yanzhenjie.album.api.VideoMultipleWrapper
import com.yanzhenjie.album.api.VideoSingleWrapper
import com.yanzhenjie.album.api.camera.Camera
import com.yanzhenjie.album.api.choice.Choice
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.durban.Controller
import com.yanzhenjie.durban.Durban

/**
 * author: wyb
 * date: 2017/9/29.
 * 调用该类之前需检测权限，activity属性设为
 * android:configChanges="orientation|keyboardHidden|screenSize"
 */
class GalleryHelper {
    private var widget: Widget? = null
    private var durban: Durban? = null
    private var imageCamera: Camera<ImageCameraWrapper, VideoCameraWrapper>? = null
    private var imageMultiple: Choice<ImageMultipleWrapper, ImageSingleWrapper>? = null
    private var videoMultiple: Choice<VideoMultipleWrapper, VideoSingleWrapper>? = null

    /**
     * 1.AppCompatActivity和Fragment在裁剪或者OnActivityResult时是必须指明的，不然返回会错误
     * 2.FragmentActivity无需指明,在Activity中传this,在Fragment传getActivity(),系统会做发起判断
     */
    constructor(activity: AppCompatActivity) {
        imageCamera = Album.camera(activity)
        videoMultiple = Album.video(activity)
        imageMultiple = Album.image(activity)
        widget = activity.getAlbumWidget()
        durban = Durban.with(activity)
    }

    constructor(fragment: Fragment) {
        imageCamera = Album.camera(fragment)
        videoMultiple = Album.video(fragment)
        imageMultiple = Album.image(fragment)
        widget = fragment.context.getAlbumWidget()
        durban = Durban.with(fragment)
    }

    constructor(activity: FragmentActivity) {
        imageCamera = Album.camera(activity)
        videoMultiple = Album.video(activity)
        imageMultiple = Album.image(activity)
        widget = activity.getAlbumWidget()
        durban = Durban.with(activity)
    }

    /**
     * 创建相册统一配置
     */
    private fun Context?.getAlbumWidget(color: Int = R.color.bgBlack): Widget? {
        this ?: return null
        return Widget.newDarkBuilder(this)
            //标题 ---标题颜色只有黑色白色
            .title(string(R.string.albumTitle))
            //状态栏颜色
            .statusBarColor(color)
            //导航栏颜色
            .navigationBarColor(color)
            //构建配置
            .build()
    }

    /**
     * 跳转至相机-拍照
     */
    fun takePicture(filePath: String, hasDurban: Boolean = false, listener: (albumPath: String?) -> Unit = {}) {
        imageCamera?.image()
            ?.filePath(filePath)
            ?.onResult {
                if (hasDurban) toDurban(it) else listener.invoke(it)
            }
            ?.start()
    }

    /**
     * 跳转至相机-录像(时间不一定能指定，大多数手机不兼容)
     */
    fun recordVideo(filePath: String, duration: Long = 1.hour, listener: (albumPath: String?) -> Unit = {}) {
        imageCamera?.video()
            //视频输出路径
            ?.filePath(filePath)
            //视频质量, [0, 1].
            ?.quality(1)
            //视频的最长持续时间以毫秒为单位
            ?.limitDuration(duration)
//                           .limitBytes(Long.MAX_VALUE)//视频的最大大小，以字节为单位
            //完成回调
            ?.onResult {
                listener.invoke(it)
            }
            ?.start()
    }

    /**
     * 选择图片
     */
    fun imageSelection(hasCamera: Boolean = true, hasDurban: Boolean = false, megabyte: Long = 10, listener: (albumPath: String?) -> Unit = {}) {
        imageMultiple
            //多选模式为：multipleChoice(同时添加?.apply { multipleChoice()?.selectCount(100) }设定上限),单选模式为：singleChoice()
            ?.singleChoice()
            //状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
            ?.widget(widget)
            //是否具备相机
            ?.camera(hasCamera)
            //页面列表的列数
            ?.columnCount(3)
            //防止加载系统缓存图片
            ?.filterSize { it == 0L }
            //筛选文件的可见性
            ?.afterFilterVisibility(false)
            //选择后回调
            ?.onResult {
                it.safeGet(0)?.apply {
                    if (size > megabyte.mb) {
                        string(R.string.albumImageError, megabyte.mb.toString()).shortToast()
                        return@onResult
                    }
                    if (hasDurban) toDurban(path) else listener.invoke(path)
                }
            }
            ?.start()
    }

    /**
     * 选择视频
     */
    fun videoSelection(megabyte: Long = 100, listener: (albumPath: String?) -> Unit = {}) {
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
    fun toDurban(vararg imagePathArray: String, width: Int = 500, height: Int = 500, quality: Int = 80) {
        durban
            //裁剪界面的标题
            ?.title(string(R.string.durbanTitle))
            //状态栏颜色
            ?.statusBarColor(R.color.bgBlack)
            //导航栏颜色
            ?.navigationBarColor(R.color.bgBlack)
            //图片路径list或者数组
            ?.inputImagePaths(*imagePathArray)
            //图片输出文件夹路径
            ?.outputDirectory(getStoragePath("裁剪图片"))
            //裁剪图片输出的最大宽高
            ?.maxWidthHeight(width, height)
            //裁剪时的宽高比
            ?.aspectRatio(1f, 1f)
            //图片压缩格式：JPEG、PNG
            ?.compressFormat(Durban.COMPRESS_JPEG)
            //图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
            ?.compressQuality(quality)
            //裁剪时的手势支持：ROTATE, SCALE, ALL, NONE.
            ?.gesture(Durban.GESTURE_SCALE)
            //底部操作栏配置
            ?.controller(Controller.newBuilder()
                //是否开启控制面板
                .enable(false)
                //是否有旋转按钮
                .rotation(true)
                //旋转控制按钮上面的标题
                .rotationTitle(true)
                //是否有缩放按钮
                .scale(true)
                //缩放控制按钮上面的标题
                .scaleTitle(true)
                //构建配置
                .build())
            //创建控制面板配置
            ?.requestCode(RESULT_ALBUM)
            //开始跳转
            ?.start()
    }

}