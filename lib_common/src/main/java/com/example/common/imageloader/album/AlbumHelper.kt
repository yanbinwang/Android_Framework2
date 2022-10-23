package com.example.common.imageloader.album

import android.app.Activity
import com.example.base.utils.function.color
import com.example.base.utils.function.string
import com.example.base.utils.function.toast
import com.example.common.R
import com.example.common.constant.Constants
import com.example.common.constant.RequestCode
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
class AlbumHelper(private val activity: Activity) {
    private val color = activity.color(R.color.grey_333333)
    var onAlbum: ((albumPath: String?) -> Unit)? = null//单选回调监听

    /**
     * 跳转至相机-拍照
     */
    fun takePicture(filePath: String, hasTailor: Boolean = false): AlbumHelper {
        with(activity) {
            Album.camera(this)
                .image()
                .filePath(filePath)
                .onResult { if (hasTailor) toTailor(it) else onAlbum?.invoke(it) }
                .start()
        }
        return this
    }

    /**
     * 跳转至相机-录像(时间不一定能指定，大多数手机不兼容)
     */
    fun recordVideo(filePath: String, duration: Long = 1000 * 60 * 60): AlbumHelper {
        with(activity) {
            Album.camera(this)
                .video()
                .filePath(filePath)
                .quality(1)//视频质量, [0, 1].
                .limitDuration(duration)//视频的最长持续时间以毫秒为单位
//                           .limitBytes(Long.MAX_VALUE)//视频的最大大小，以字节为单位
                .onResult { onAlbum?.invoke(it) }
                .start()
        }
        return this
    }

    /**
     * 跳转至相册
     */
    fun imageSelection(hasCamera: Boolean = true, hasTailor: Boolean = false): AlbumHelper {
        with(activity) {
            //选择图片
            Album.image(this)
                //多选模式为：multipleChoice,单选模式为：singleChoice()
                .singleChoice()
                //状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
                .widget(
                    Widget.newDarkBuilder(this)
                    //标题 ---标题颜色只有黑色白色
                    .title(" ")
                    //状态栏颜色
                    .statusBarColor(color)
                    //Toolbar颜色
                    .toolBarColor(color)
                    .build())
                //是否具备相机
                .camera(hasCamera)
                //页面列表的列数
                .columnCount(3)
                //防止加载系统缓存图片
                .filterSize { it == 0L }
                .afterFilterVisibility(false)
                .onResult {
                    val resultSize = it[0].size
                    if (resultSize > 10 * 1024 * 1024) {
                        toast(string(R.string.toast_album_pic_choice))
                        return@onResult
                    }
                    if (hasTailor) toTailor(it[0].path) else onAlbum?.invoke(it[0].path)
                }.start()
        }
        return this
    }

    fun videoSelection(): AlbumHelper {
        with(activity) {
            //选择视频
            Album.video(this)
                //多选模式为：multipleChoice,单选模式为：singleChoice()
                .singleChoice()
                //状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
                .widget(
                    Widget.newDarkBuilder(this)
                    //标题 ---标题颜色只有黑色白色
                    .title(" ")
                    //状态栏颜色
                    .statusBarColor(color)
                    //Toolbar颜色
                    .toolBarColor(color)
                    .build())
                //是否具备相机
                .camera(true)
                //页面列表的列数
                .columnCount(3)
                //防止加载系统缓存图片
                .filterSize { it == 0L }
                .afterFilterVisibility(false)
                .onResult {
                    val resultSize = it[0].size
                    if (resultSize > 100 * 1024 * 1024) {
                        toast(string(R.string.toast_album_video_choice))
                        return@onResult
                    }
                    onAlbum?.invoke(it[0].path)
                }.start()
        }
        return this
    }

    /**
     * 开始裁剪
     */
    private fun toTailor(vararg imagePathArray: String) {
        with(activity) {
            Durban.with(this)
                //裁剪界面的标题
                .title(" ")
                //状态栏颜色
                .statusBarColor(color)
                //Toolbar颜色
                .toolBarColor(color)
                //图片路径list或者数组
                .inputImagePaths(*imagePathArray)
                //图片输出文件夹路径
                .outputDirectory("${Constants.APPLICATION_FILE_PATH}/裁剪图片")
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
                .controller(
                    Controller.newBuilder()
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
                .requestCode(RequestCode.REQUEST_PHOTO).start()
        }
    }

}