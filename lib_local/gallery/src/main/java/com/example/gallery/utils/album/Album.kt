package com.example.gallery.utils.album

import android.content.Context
import android.util.Log
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.gallery.utils.album.api.album.AlbumCamera
import com.example.gallery.utils.album.api.camera.ImageCameraWrapper
import com.example.gallery.utils.album.api.camera.VideoCameraWrapper
import com.example.gallery.utils.album.api.choice.AlbumChoice
import com.example.gallery.utils.album.api.choice.AlbumMultipleWrapper
import com.example.gallery.utils.album.api.choice.AlbumSingleWrapper
import com.example.gallery.utils.album.api.choice.ImageChoice
import com.example.gallery.utils.album.api.choice.ImageMultipleWrapper
import com.example.gallery.utils.album.api.choice.ImageSingleWrapper
import com.example.gallery.utils.album.api.choice.VideoChoice
import com.example.gallery.utils.album.api.choice.VideoMultipleWrapper
import com.example.gallery.utils.album.api.choice.VideoSingleWrapper
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.api.BasicGalleryWrapper
import com.yanzhenjie.album.api.GalleryAlbumWrapper
import com.yanzhenjie.album.api.GalleryWrapper
import com.yanzhenjie.album.api.camera.Camera
import com.yanzhenjie.album.api.choice.Choice

object Album {
    private var sAlbumConfig: AlbumConfig? = null
    // All.
    const val KEY_INPUT_WIDGET = "KEY_INPUT_WIDGET"
    const val KEY_INPUT_CHECKED_LIST = "KEY_INPUT_CHECKED_LIST"
    // Album.
    const val KEY_INPUT_FUNCTION = "KEY_INPUT_FUNCTION"
    const val FUNCTION_CHOICE_IMAGE = 0
    const val FUNCTION_CHOICE_VIDEO = 1
    const val FUNCTION_CHOICE_ALBUM = 2
    const val FUNCTION_CAMERA_IMAGE = 0
    const val FUNCTION_CAMERA_VIDEO = 1
    const val KEY_INPUT_CHOICE_MODE = "KEY_INPUT_CHOICE_MODE"
    const val MODE_MULTIPLE = 1
    const val MODE_SINGLE = 2
    const val KEY_INPUT_COLUMN_COUNT = "KEY_INPUT_COLUMN_COUNT"
    const val KEY_INPUT_ALLOW_CAMERA = "KEY_INPUT_ALLOW_CAMERA"
    const val KEY_INPUT_LIMIT_COUNT = "KEY_INPUT_LIMIT_COUNT"
    // Gallery.
    const val KEY_INPUT_CURRENT_POSITION = "KEY_INPUT_CURRENT_POSITION"
    const val KEY_INPUT_GALLERY_CHECKABLE = "KEY_INPUT_GALLERY_CHECKABLE"
    // Camera.
    const val KEY_INPUT_FILE_PATH = "KEY_INPUT_FILE_PATH"
    const val KEY_INPUT_CAMERA_QUALITY = "KEY_INPUT_CAMERA_QUALITY"
    const val KEY_INPUT_CAMERA_DURATION = "KEY_INPUT_CAMERA_DURATION"
    const val KEY_INPUT_CAMERA_BYTES = "KEY_INPUT_CAMERA_BYTES"
    // Filter.
    const val KEY_INPUT_FILTER_VISIBILITY = "KEY_INPUT_FILTER_VISIBILITY"

    @IntDef(Album.FUNCTION_CHOICE_IMAGE, Album.FUNCTION_CHOICE_VIDEO, Album.FUNCTION_CHOICE_ALBUM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ChoiceFunction

    @IntDef(Album.FUNCTION_CAMERA_IMAGE, Album.FUNCTION_CAMERA_VIDEO)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CameraFunction

    @IntDef(Album.MODE_MULTIPLE, Album.MODE_SINGLE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ChoiceMode

    /**
     * Initialize Album.
     * Params:
     * albumConfig – AlbumConfig.
     */
    @JvmStatic
    fun initialize(albumConfig: AlbumConfig?) {
        if (sAlbumConfig == null) sAlbumConfig = albumConfig
        else Log.w("Album", IllegalStateException("Illegal operation, only allowed to configure once."))
    }

    /**
     * Get the album configuration.
     */
    @JvmStatic
    fun getAlbumConfig(): AlbumConfig? {
        if (sAlbumConfig == null) {
            sAlbumConfig = AlbumConfig.newBuilder(null).build()
        }
        return sAlbumConfig
    }

    @JvmStatic
    fun camera(context: Context): Camera<ImageCameraWrapper, VideoCameraWrapper> {
        return AlbumCamera(context)
    }

    /**
     * Select images.
     */
    @JvmStatic
    fun image(context: Context?): Choice<ImageMultipleWrapper, ImageSingleWrapper> {
        return ImageChoice(context)
    }

    /**
     * Select videos.
     */
    @JvmStatic
    fun video(context: Context): Choice<VideoMultipleWrapper, VideoSingleWrapper> {
        return VideoChoice(context)
    }

    /**
     * Select images and videos.
     */
    @JvmStatic
    fun album(context: Context): Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
        return AlbumChoice(context)
    }

    /**
     * Preview picture.
     */
    @JvmStatic
    fun gallery(context: Context): GalleryWrapper {
        return GalleryWrapper(context)
    }

    /**
     * Preview Album.
     */
    @JvmStatic
    fun galleryAlbum(context: Context): GalleryAlbumWrapper {
        return GalleryAlbumWrapper(context)
    }

    /**
     * Open the camera from the activity.
     */
    @JvmStatic
    fun camera(activity: AppCompatActivity): Camera<ImageCameraWrapper, VideoCameraWrapper> {
        return AlbumCamera(activity)
    }

    /**
     * Select images.
     */
    @JvmStatic
    fun image(activity: AppCompatActivity): Choice<ImageMultipleWrapper, ImageSingleWrapper> {
        return ImageChoice(activity)
    }

    /**
     * Select videos.
     */
    @JvmStatic
    fun video(activity: AppCompatActivity): Choice<VideoMultipleWrapper, VideoSingleWrapper> {
        return VideoChoice(activity)
    }

    /**
     * Select images and videos.
     */
    @JvmStatic
    fun album(activity: AppCompatActivity): Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
        return AlbumChoice(activity)
    }

    /**
     * Preview picture.
     */
    @JvmStatic
    fun gallery(activity: AppCompatActivity): BasicGalleryWrapper<GalleryWrapper, String, String, String> {
        return GalleryWrapper(activity)
    }

    /**
     * Preview Album.
     */
    @JvmStatic
    fun galleryAlbum(activity: AppCompatActivity): BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> {
        return GalleryAlbumWrapper(activity)
    }

    /**
     * Open the camera from the activity.
     */
    @JvmStatic
    fun camera(fragment: Fragment): Camera<ImageCameraWrapper, VideoCameraWrapper> {
        return AlbumCamera(fragment.activity)
    }

    /**
     * Select images.
     */
    @JvmStatic
    fun image(fragment: Fragment): Choice<ImageMultipleWrapper, ImageSingleWrapper> {
        return ImageChoice(fragment.activity)
    }

    /**
     * Select videos.
     */
    @JvmStatic
    fun video(fragment: Fragment): Choice<VideoMultipleWrapper, VideoSingleWrapper> {
        return VideoChoice(fragment.activity)
    }

    /**
     * Select images and videos.
     */
    @JvmStatic
    fun album(fragment: Fragment): Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
        return AlbumChoice(fragment.activity)
    }

    /**
     * Preview picture.
     */
    @JvmStatic
    fun gallery(fragment: Fragment): BasicGalleryWrapper<GalleryWrapper, String, String, String> {
        return GalleryWrapper(fragment.activity)
    }

    /**
     * Preview Album.
     */
    @JvmStatic
    fun galleryAlbum(fragment: Fragment): BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> {
        return GalleryAlbumWrapper(fragment.activity)
    }

    /**
     * Open the camera from the activity.
     */
    @JvmStatic
    fun camera(activity: FragmentActivity): Camera<ImageCameraWrapper, VideoCameraWrapper> {
        return AlbumCamera(activity)
    }

    /**
     * Select images.
     */
    @JvmStatic
    fun image(activity: FragmentActivity): Choice<ImageMultipleWrapper, ImageSingleWrapper> {
        return ImageChoice(activity)
    }

    /**
     * Select videos.
     */
    @JvmStatic
    fun video(activity: FragmentActivity): Choice<VideoMultipleWrapper, VideoSingleWrapper> {
        return VideoChoice(activity)
    }

    /**
     * Select images and videos.
     */
    @JvmStatic
    fun album(activity: FragmentActivity): Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
        return AlbumChoice(activity)
    }

    /**
     * Preview picture.
     */
    @JvmStatic
    fun gallery(activity: FragmentActivity): BasicGalleryWrapper<GalleryWrapper, String, String, String> {
        return GalleryWrapper(activity)
    }

    /**
     * Preview Album.
     */
    @JvmStatic
    fun galleryAlbum(activity: FragmentActivity): BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> {
        return GalleryAlbumWrapper(activity)
    }

}