package com.example.thirdparty.album

import android.app.Activity
import android.graphics.Color
import com.example.common.base.page.RequestCode.REQUEST_PHOTO
import com.example.common.config.Constants.STORAGE
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.mb
import com.example.common.utils.function.string
import com.example.framework.utils.function.value.hour
import com.example.framework.utils.function.value.safeGet
import com.example.thirdparty.R
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.durban.Controller
import com.yanzhenjie.durban.Durban

/**
 * author: wyb
 * date: 2017/9/29.
 * 调用该类之前需检测权限，activity属性设为
 * android:configChanges="orientation|keyboardHidden|screenSize"
 */
class AlbumHelper(mActivity: Activity) {
    private val mCamera by lazy { Album.camera(mActivity) }
    private val mVideo by lazy { Album.video(mActivity) }
    private val mImage by lazy { Album.image(mActivity) }
    private val mDurban by lazy { Durban.with(mActivity) }
    private val mWidget by lazy {
        Widget.newDarkBuilder(mActivity)
            //标题 ---标题颜色只有黑色白色
            .title(" ")
            //状态栏颜色
            .statusBarColor(mColor)
            //Toolbar颜色
            .toolBarColor(mColor)
            .build()
    }
    private val mColor by lazy { Color.BLACK }

    /**
     * 跳转至相机-拍照
     */
    fun takePicture(filePath: String, hasDurban: Boolean = false, onResult: (albumPath: String?) -> Unit = {}) {
        mCamera
            .image()
            .filePath(filePath)
            .onResult { if (hasDurban) toDurban(it) else onResult.invoke(it) }
            .start()
    }

    /**
     * 跳转至相机-录像(时间不一定能指定，大多数手机不兼容)
     */
    fun recordVideo(filePath: String, duration: Long = 1.hour, onResult: (albumPath: String?) -> Unit = {}) {
        mCamera
            .video()
            .filePath(filePath)
            .quality(1)//视频质量, [0, 1].
            .limitDuration(duration)//视频的最长持续时间以毫秒为单位
//                           .limitBytes(Long.MAX_VALUE)//视频的最大大小，以字节为单位
            .onResult { onResult.invoke(it) }
            .start()
    }

    /**
     * 选择图片
     */
    fun imageSelection(hasCamera: Boolean = true, hasDurban: Boolean = false, megabyte: Long = 10, onResult: (albumPath: String?) -> Unit = {}) {
        mImage
            //多选模式为：multipleChoice,单选模式为：singleChoice()
            .singleChoice()
            //状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
            .widget(mWidget)
            //是否具备相机
            .camera(hasCamera)
            //页面列表的列数
            .columnCount(3)
            //防止加载系统缓存图片
            .filterSize { it == 0L }
            .afterFilterVisibility(false)
            .onResult {
                it.safeGet(0)?.apply {
                    if (size > megabyte.mb) {
                        string(R.string.albumImageError, megabyte.mb.toString()).shortToast()
                        return@onResult
                    }
                    if (hasDurban) toDurban(path) else onResult.invoke(path)
                }
            }.start()
    }

    /**
     * 选择视频
     */
    fun videoSelection(megabyte: Long = 100, onResult: (albumPath: String?) -> Unit = {}) {
        mVideo
            //多选模式为：multipleChoice,单选模式为：singleChoice()
            .singleChoice()
            //状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
            .widget(mWidget)
            //是否具备相机
            .camera(true)
            //页面列表的列数
            .columnCount(3)
            //防止加载系统缓存图片
            .filterSize { it == 0L }
            .afterFilterVisibility(false)
            .onResult {
                it.safeGet(0)?.apply {
                    if (size > megabyte.mb) {
                        string(R.string.albumVideoError, megabyte.mb.toString()).shortToast()
                        return@onResult
                    }
                    onResult.invoke(path)
                }
            }.start()
    }

    /**
     * 开始裁剪
     */
    private fun toDurban(vararg imagePathArray: String) {
        mDurban
            //裁剪界面的标题
            .title(" ")
            //状态栏颜色
            .statusBarColor(mColor)
            //Toolbar颜色
            .toolBarColor(mColor)
            //图片路径list或者数组
            .inputImagePaths(*imagePathArray)
            //图片输出文件夹路径
            .outputDirectory("${STORAGE}/裁剪图片")
            //裁剪图片输出的最大宽高
            .maxWidthHeight(500, 500)
            //裁剪时的宽高比
            .aspectRatio(1f, 1f)
            //图片压缩格式：JPEG、PNG
            .compressFormat(Durban.COMPRESS_JPEG)
            //图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
            .compressQuality(90)
            //裁剪时的手势支持：ROTATE, SCALE, ALL, NONE.
            .gesture(Durban.GESTURE_SCALE)
            .controller(Controller.newBuilder()
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
                .build())
            //创建控制面板配置
            .requestCode(REQUEST_PHOTO).start()
    }

}