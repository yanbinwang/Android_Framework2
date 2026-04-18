package com.yanzhenjie.album

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.yanzhenjie.album.api.AlbumMultipleWrapper
import com.yanzhenjie.album.api.AlbumSingleWrapper
import com.yanzhenjie.album.api.GalleryAlbumWrapper
import com.yanzhenjie.album.api.GalleryWrapper
import com.yanzhenjie.album.api.ImageCameraWrapper
import com.yanzhenjie.album.api.ImageMultipleWrapper
import com.yanzhenjie.album.api.ImageSingleWrapper
import com.yanzhenjie.album.api.VideoCameraWrapper
import com.yanzhenjie.album.api.VideoMultipleWrapper
import com.yanzhenjie.album.api.VideoSingleWrapper
import com.yanzhenjie.album.api.camera.AlbumCamera
import com.yanzhenjie.album.api.camera.Camera
import com.yanzhenjie.album.api.choice.AlbumChoice
import com.yanzhenjie.album.api.choice.Choice
import com.yanzhenjie.album.api.choice.ImageChoice
import com.yanzhenjie.album.api.choice.VideoChoice
import com.yanzhenjie.album.model.AlbumConfig

/**
 * 相册库总入口
 * 整个相册库唯一对外暴露的使用入口，提供：选择图片/视频、拍照、预览等所有功能
 */
object Album {
    // 全局通用 Key
    const val KEY_INPUT_WIDGET = "KEY_INPUT_WIDGET"
    const val KEY_INPUT_CHECKED_LIST = "KEY_INPUT_CHECKED_LIST"

    // 相册选择功能常量
    const val KEY_INPUT_FUNCTION = "KEY_INPUT_FUNCTION"
    const val FUNCTION_CHOICE_IMAGE = 0 // 仅选择图片
    const val FUNCTION_CHOICE_VIDEO = 1 // 仅选择视频
    const val FUNCTION_CHOICE_ALBUM = 2 // 选择图片+视频

    // 相机功能常量
    const val FUNCTION_CAMERA_IMAGE = 0 // 相机拍照
    const val FUNCTION_CAMERA_VIDEO = 1 // 相机录像

    // 选择模式常量
    const val KEY_INPUT_CHOICE_MODE = "KEY_INPUT_CHOICE_MODE"
    const val MODE_MULTIPLE = 1 // 多选模式
    const val MODE_SINGLE = 2 // 单选模式

    // 相册配置 Key
    const val KEY_INPUT_COLUMN_COUNT = "KEY_INPUT_COLUMN_COUNT" // 列表列数
    const val KEY_INPUT_ALLOW_CAMERA = "KEY_INPUT_ALLOW_CAMERA" // 是否显示相机入口
    const val KEY_INPUT_LIMIT_COUNT = "KEY_INPUT_LIMIT_COUNT" // 最大选择数量

    // 预览功能 Key
    const val KEY_INPUT_CURRENT_POSITION = "KEY_INPUT_CURRENT_POSITION" // 当前预览位置
    const val KEY_INPUT_GALLERY_CHECKABLE = "KEY_INPUT_GALLERY_CHECKABLE" // 预览页是否可选择

    // 相机参数 Key
    const val KEY_INPUT_FILE_PATH = "KEY_INPUT_FILE_PATH" // 拍照保存路径
    const val KEY_INPUT_CAMERA_QUALITY = "KEY_INPUT_CAMERA_QUALITY" // 相机质量
    const val KEY_INPUT_CAMERA_DURATION = "KEY_INPUT_CAMERA_DURATION" // 视频最大时长
    const val KEY_INPUT_CAMERA_BYTES = "KEY_INPUT_CAMERA_BYTES" // 视频大小限制

    // 过滤参数 Key
    const val KEY_INPUT_FILTER_VISIBILITY = "KEY_INPUT_FILTER_VISIBILITY" // 文件过滤开关

    // 全局唯一的相册配置实例
    private var sAlbumConfig: AlbumConfig? = null

    /**
     * 初始化相册库（全局只调用一次 -> Application）
     */
    @JvmStatic
    fun initialize(albumConfig: AlbumConfig) {
        if (sAlbumConfig == null) {
            sAlbumConfig = albumConfig
        }
    }

    /**
     * 获取全局相册配置
     */
    @JvmStatic
    fun getAlbumConfig(): AlbumConfig {
        return sAlbumConfig ?: AlbumConfig.newBuilder().build()
    }

    /**
     * 打开相机
     */
    @JvmStatic
    fun camera(any: Any): Camera<ImageCameraWrapper, VideoCameraWrapper> {
        return AlbumCamera(applyContext(any))
    }

    /**
     * 选择图片
     */
    @JvmStatic
    fun image(any: Any): Choice<ImageMultipleWrapper, ImageSingleWrapper> {
        return ImageChoice(applyContext(any))
    }

    /**
     * 选择视频
     */
    @JvmStatic
    fun video(any: Any): Choice<VideoMultipleWrapper, VideoSingleWrapper> {
        return VideoChoice(applyContext(any))
    }

    /**
     * 选择图片+视频
     */
    @JvmStatic
    fun album(any: Any): Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
        return AlbumChoice(applyContext(any))
    }

    /**
     * 预览图片
     */
    @JvmStatic
    fun gallery(any: Any): GalleryWrapper {
        return GalleryWrapper(applyContext(any))
    }

    /**
     * 预览相册文件
     */
    @JvmStatic
    fun galleryAlbum(any: Any): GalleryAlbumWrapper {
        return GalleryAlbumWrapper(applyContext(any))
    }

    /**
     * 获取上下文
     */
    private fun applyContext(any: Any): Context {
        return when (any) {
            // Activity（兼容所有现代 Activity）
            is FragmentActivity -> any
            // AndroidX Fragment
            is Fragment -> any.requireActivity()
            // 旧系统Fragment
            is android.app.Fragment -> throw RuntimeException("android.app.Fragment is deprecated and not supported!")
            // 不认识的类型
            else -> throw IllegalArgumentException("Unsupported host type: ${any::class.java.name}")
        }
    }

}