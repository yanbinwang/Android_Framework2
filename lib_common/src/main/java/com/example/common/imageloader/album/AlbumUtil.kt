package com.example.common.imageloader.album

import android.app.Activity

import androidx.core.content.ContextCompat

import com.example.common.R
import com.example.common.constant.Constants
import com.example.common.constant.RequestCode
import com.example.framework.utils.ToastUtil
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.durban.Controller
import com.yanzhenjie.durban.Durban

import java.lang.ref.WeakReference

/**
 * author: wyb
 * date: 2017/9/29.
 * 调用该类之前需检测权限，activity属性设为
 * android:configChanges="orientation|keyboardHidden|screenSize"
 */
class AlbumUtil(activity: Activity) {
    private val mActivity: WeakReference<Activity> = WeakReference(activity)
    private var onAlbumListener: OnAlbumListener? = null //单选回调监听

    //跳转至相机
    fun toCamera(isNeedTailor: Boolean) {
        Album.camera(mActivity.get()) //相机功能。
            .image() //拍照。
            .onResult { result ->
                if (isNeedTailor) {
                    Durban.with(mActivity.get())
                        // 裁剪界面的标题。
                        .title(" ").statusBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.grey_333333)) // 状态栏颜色。
                        .toolBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.grey_333333)) // Toolbar颜色。
                        // 图片路径list或者数组。
                        .inputImagePaths(result)
                        // 图片输出文件夹路径。
                        .outputDirectory(Constants.BASE_PATH.absolutePath + "/" + Constants.APPLICATION_NAME + "/裁剪")
                        // 裁剪图片输出的最大宽高。
                        .maxWidthHeight(500, 500)
                        // 裁剪时的宽高比。
                        .aspectRatio(1f, 1f)
                        // 图片压缩格式：JPEG、PNG。
                        .compressFormat(Durban.COMPRESS_JPEG)
                        // 图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
                        .compressQuality(90)
                        // 裁剪时的手势支持：ROTATE, SCALE, ALL, NONE.
                        .gesture(Durban.GESTURE_SCALE).controller(Controller.newBuilder().enable(false) // 是否开启控制面板。
                            .rotation(true) // 是否有旋转按钮。
                            .rotationTitle(true) // 旋转控制按钮上面的标题。
                            .scale(true) // 是否有缩放按钮。
                            .scaleTitle(true) // 缩放控制按钮上面的标题。
                            .build()) // 创建控制面板配置。
                        .requestCode(RequestCode.PHOTO_REQUEST).start()
                } else {
                    if (null != onAlbumListener) {
                        onAlbumListener!!.onAlbumListener(result)
                    }
                }
            }.start()
    }

    //跳转至相册
    fun toPhotoAlbum(isNeedCamera: Boolean, isNeedTailor: Boolean) {
        Album.image(mActivity.get()) //选择图片。
            .singleChoice() //多选模式为：multipleChoice,单选模式为：singleChoice()。
            .widget(Widget.newDarkBuilder(mActivity.get()) //状态栏是深色背景时的构建newDarkBuilder ，状态栏是白色背景时的构建newLightBuilder
                .title(" ") //标题 ---标题颜色只有黑色白色
                .statusBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.grey_333333)) // 状态栏颜色。
                .toolBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.grey_333333)) // Toolbar颜色。
                .build()).camera(isNeedCamera).columnCount(3) // 页面列表的列数。
            .onResult { result ->
                val resultSize = result[0].size
                if (resultSize > 10 * 1024 * 1024) {
                    ToastUtil.mackToastSHORT(mActivity.get()!!.getString(R.string.toast_album_choice), mActivity.get()!!.applicationContext)
                    return@onResult
                }
                if (isNeedTailor) {
                    Durban.with(mActivity.get())
                        // 裁剪界面的标题。
                        .title(" ").statusBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.grey_333333)) // 状态栏颜色。
                        .toolBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.grey_333333)) // Toolbar颜色。
                        // 图片路径list或者数组。
                        .inputImagePaths(result[0].path)
                        // 图片输出文件夹路径。
                        .outputDirectory(Constants.BASE_PATH.absolutePath + "/" + Constants.APPLICATION_NAME + "/裁剪")
                        // 裁剪图片输出的最大宽高。
                        .maxWidthHeight(500, 500)
                        // 裁剪时的宽高比。
                        .aspectRatio(1f, 1f)
                        // 图片压缩格式：JPEG、PNG。
                        .compressFormat(Durban.COMPRESS_JPEG)
                        // 图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
                        .compressQuality(90)
                        // 裁剪时的手势支持：ROTATE, SCALE, ALL, NONE.
                        .gesture(Durban.GESTURE_SCALE).controller(Controller.newBuilder().enable(false) // 是否开启控制面板。
                            .rotation(true) // 是否有旋转按钮。
                            .rotationTitle(true) // 旋转控制按钮上面的标题。
                            .scale(true) // 是否有缩放按钮。
                            .scaleTitle(true) // 缩放控制按钮上面的标题。
                            .build()) // 创建控制面板配置。
                        .requestCode(RequestCode.PHOTO_REQUEST).start()
                } else {
                    if (null != onAlbumListener) {
                        onAlbumListener!!.onAlbumListener(result[0].path)
                    }
                }
            }.start()
    }

    fun setAlbumCallBack(onAlbumListener: OnAlbumListener) {
        this.onAlbumListener = onAlbumListener
    }

}