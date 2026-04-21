package com.example.gallery.album

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.gallery.album.api.AlbumMultipleWrapper
import com.example.gallery.album.api.AlbumSingleWrapper
import com.example.gallery.album.api.GalleryAlbumWrapper
import com.example.gallery.album.api.GalleryWrapper
import com.example.gallery.album.api.ImageCameraWrapper
import com.example.gallery.album.api.ImageMultipleWrapper
import com.example.gallery.album.api.ImageSingleWrapper
import com.example.gallery.album.api.VideoCameraWrapper
import com.example.gallery.album.api.VideoMultipleWrapper
import com.example.gallery.album.api.VideoSingleWrapper
import com.example.gallery.album.api.camera.AlbumCamera
import com.example.gallery.album.api.camera.Camera
import com.example.gallery.album.api.choice.AlbumChoice
import com.example.gallery.album.api.choice.Choice
import com.example.gallery.album.api.choice.ImageChoice
import com.example.gallery.album.api.choice.VideoChoice
import com.example.gallery.album.model.AlbumConfig

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
    fun camera(host: Any): Camera<ImageCameraWrapper, VideoCameraWrapper> {
        return AlbumCamera(applyContext(host))
    }

    /**
     * 选择图片
     */
    @JvmStatic
    fun image(host: Any): Choice<ImageMultipleWrapper, ImageSingleWrapper> {
        return ImageChoice(applyContext(host))
    }

    /**
     * 选择视频
     */
    @JvmStatic
    fun video(host: Any): Choice<VideoMultipleWrapper, VideoSingleWrapper> {
        return VideoChoice(applyContext(host))
    }

    /**
     * 选择图片+视频
     */
    @JvmStatic
    fun album(host: Any): Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
        return AlbumChoice(applyContext(host))
    }

    /**
     * 预览图片
     */
    @JvmStatic
    fun gallery(host: Any): GalleryWrapper {
        return GalleryWrapper(applyContext(host))
    }

    /**
     * 预览相册文件
     */
    @JvmStatic
    fun galleryAlbum(host: Any): GalleryAlbumWrapper {
        return GalleryAlbumWrapper(applyContext(host))
    }

    /**
     * 获取上下文
     */
    private fun applyContext(host: Any): Context {
        return when (host) {
            // Activity（兼容所有现代 Activity）
            is FragmentActivity -> host
            // AndroidX Fragment
            is Fragment -> host.requireActivity()
            // 旧系统Fragment
            is android.app.Fragment -> throw RuntimeException("android.app.Fragment is deprecated and not supported!")
            // 不认识的类型
            else -> throw IllegalArgumentException("Unsupported host type: ${host::class.java.name}")
        }
    }

}