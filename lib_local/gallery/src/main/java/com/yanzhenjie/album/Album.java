package com.yanzhenjie.album;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.fragment.app.Fragment;

import com.yanzhenjie.album.api.AlbumMultipleWrapper;
import com.yanzhenjie.album.api.AlbumSingleWrapper;
import com.yanzhenjie.album.api.BasicGalleryWrapper;
import com.yanzhenjie.album.api.GalleryAlbumWrapper;
import com.yanzhenjie.album.api.GalleryWrapper;
import com.yanzhenjie.album.api.ImageCameraWrapper;
import com.yanzhenjie.album.api.ImageMultipleWrapper;
import com.yanzhenjie.album.api.ImageSingleWrapper;
import com.yanzhenjie.album.api.VideoCameraWrapper;
import com.yanzhenjie.album.api.VideoMultipleWrapper;
import com.yanzhenjie.album.api.VideoSingleWrapper;
import com.yanzhenjie.album.api.camera.AlbumCamera;
import com.yanzhenjie.album.api.camera.Camera;
import com.yanzhenjie.album.api.choice.AlbumChoice;
import com.yanzhenjie.album.api.choice.Choice;
import com.yanzhenjie.album.api.choice.ImageChoice;
import com.yanzhenjie.album.api.choice.VideoChoice;
import com.yanzhenjie.album.model.AlbumConfig;
import com.yanzhenjie.album.model.AlbumFile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 相册库总入口（核心调度类）
 * 整个相册库唯一对外暴露的使用入口，提供：选择图片/视频、拍照、预览等所有功能
 */
public final class Album {
    // 全局通用 Key
    public static final String KEY_INPUT_WIDGET = "KEY_INPUT_WIDGET";
    public static final String KEY_INPUT_CHECKED_LIST = "KEY_INPUT_CHECKED_LIST";
    // 相册选择功能常量
    public static final String KEY_INPUT_FUNCTION = "KEY_INPUT_FUNCTION";
    public static final int FUNCTION_CHOICE_IMAGE = 0; // 仅选择图片
    public static final int FUNCTION_CHOICE_VIDEO = 1; // 仅选择视频
    public static final int FUNCTION_CHOICE_ALBUM = 2; // 选择图片+视频
    // 相机功能常量
    public static final int FUNCTION_CAMERA_IMAGE = 0; // 相机拍照
    public static final int FUNCTION_CAMERA_VIDEO = 1; // 相机录像
    // 选择模式常量
    public static final String KEY_INPUT_CHOICE_MODE = "KEY_INPUT_CHOICE_MODE";
    public static final int MODE_MULTIPLE = 1; // 多选模式
    public static final int MODE_SINGLE = 2; // 单选模式
    // 相册配置 Key
    public static final String KEY_INPUT_COLUMN_COUNT = "KEY_INPUT_COLUMN_COUNT"; // 列表列数
    public static final String KEY_INPUT_ALLOW_CAMERA = "KEY_INPUT_ALLOW_CAMERA"; // 是否显示相机入口
    public static final String KEY_INPUT_LIMIT_COUNT = "KEY_INPUT_LIMIT_COUNT"; // 最大选择数量
    // 预览功能 Key
    public static final String KEY_INPUT_CURRENT_POSITION = "KEY_INPUT_CURRENT_POSITION"; // 当前预览位置
    public static final String KEY_INPUT_GALLERY_CHECKABLE = "KEY_INPUT_GALLERY_CHECKABLE"; // 预览页是否可选择
    // 相机参数 Key
    public static final String KEY_INPUT_FILE_PATH = "KEY_INPUT_FILE_PATH"; // 拍照保存路径
    public static final String KEY_INPUT_CAMERA_QUALITY = "KEY_INPUT_CAMERA_QUALITY"; // 相机质量
    public static final String KEY_INPUT_CAMERA_DURATION = "KEY_INPUT_CAMERA_DURATION"; // 视频最大时长
    public static final String KEY_INPUT_CAMERA_BYTES = "KEY_INPUT_CAMERA_BYTES"; // 视频大小限制
    // 过滤参数 Key
    public static final String KEY_INPUT_FILTER_VISIBILITY = "KEY_INPUT_FILTER_VISIBILITY"; // 文件过滤开关

    // 类型注解（约束参数只能传指定常量）
    @IntDef({FUNCTION_CHOICE_IMAGE, FUNCTION_CHOICE_VIDEO, FUNCTION_CHOICE_ALBUM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChoiceFunction {
    }

    @IntDef({FUNCTION_CAMERA_IMAGE, FUNCTION_CAMERA_VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraFunction {
    }

    @IntDef({MODE_MULTIPLE, MODE_SINGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChoiceMode {
    }

    // 全局唯一的相册配置实例
    private static AlbumConfig sAlbumConfig;

    /**
     * 初始化相册库（全局只调用一次 -> Application）
     */
    public static void initialize(AlbumConfig albumConfig) {
        if (sAlbumConfig == null) {
            sAlbumConfig = albumConfig;
        } else {
            Log.w("Album", new IllegalStateException("Illegal operation, only allowed to configure once."));
        }
    }

    /**
     * 获取全局相册配置
     */
    public static AlbumConfig getAlbumConfig() {
        if (sAlbumConfig == null) {
            sAlbumConfig = AlbumConfig.newBuilder().build();
        }
        return sAlbumConfig;
    }

    /**
     * 打开相机
     */
    public static Camera<ImageCameraWrapper, VideoCameraWrapper> camera(Context context) {
        return new AlbumCamera(context);
    }

    /**
     * 选择图片
     */
    public static Choice<ImageMultipleWrapper, ImageSingleWrapper> image(Context context) {
        return new ImageChoice(context);
    }

    /**
     * 选择视频
     */
    public static Choice<VideoMultipleWrapper, VideoSingleWrapper> video(Context context) {
        return new VideoChoice(context);
    }

    /**
     * 选择图片+视频
     */
    public static Choice<AlbumMultipleWrapper, AlbumSingleWrapper> album(Context context) {
        return new AlbumChoice(context);
    }

    /**
     * 预览图片
     */
    public static GalleryWrapper gallery(Context context) {
        return new GalleryWrapper(context);
    }

    /**
     * 预览相册文件
     */
    public static GalleryAlbumWrapper galleryAlbum(Context context) {
        return new GalleryAlbumWrapper(context);
    }

    // -------------------- Activity 调用 --------------------
    public static Camera<ImageCameraWrapper, VideoCameraWrapper> camera(Activity activity) {
        return new AlbumCamera(activity);
    }

    public static Choice<ImageMultipleWrapper, ImageSingleWrapper> image(Activity activity) {
        return new ImageChoice(activity);
    }

    public static Choice<VideoMultipleWrapper, VideoSingleWrapper> video(Activity activity) {
        return new VideoChoice(activity);
    }

    public static Choice<AlbumMultipleWrapper, AlbumSingleWrapper> album(Activity activity) {
        return new AlbumChoice(activity);
    }

    public static BasicGalleryWrapper<GalleryWrapper, String, String, String> gallery(Activity activity) {
        return new GalleryWrapper(activity);
    }

    public static BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> galleryAlbum(Activity activity) {
        return new GalleryAlbumWrapper(activity);
    }

    // -------------------- AndroidX Fragment 调用 --------------------
    public static Camera<ImageCameraWrapper, VideoCameraWrapper> camera(Fragment fragment) {
        return new AlbumCamera(fragment.getActivity());
    }

    public static Choice<ImageMultipleWrapper, ImageSingleWrapper> image(Fragment fragment) {
        return new ImageChoice(fragment.getActivity());
    }

    public static Choice<VideoMultipleWrapper, VideoSingleWrapper> video(Fragment fragment) {
        return new VideoChoice(fragment.getActivity());
    }

    public static Choice<AlbumMultipleWrapper, AlbumSingleWrapper> album(Fragment fragment) {
        return new AlbumChoice(fragment.getActivity());
    }

    public static BasicGalleryWrapper<GalleryWrapper, String, String, String> gallery(Fragment fragment) {
        return new GalleryWrapper(fragment.getActivity());
    }

    public static BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> galleryAlbum(Fragment fragment) {
        return new GalleryAlbumWrapper(fragment.getActivity());
    }

    // -------------------- 系统原生 Fragment 调用 --------------------
    public static Camera<ImageCameraWrapper, VideoCameraWrapper> camera(android.app.Fragment fragment) {
        return new AlbumCamera(fragment.getContext());
    }

    public static Choice<ImageMultipleWrapper, ImageSingleWrapper> image(android.app.Fragment fragment) {
        return new ImageChoice(fragment.getContext());
    }

    public static Choice<VideoMultipleWrapper, VideoSingleWrapper> video(android.app.Fragment fragment) {
        return new VideoChoice(fragment.getContext());
    }

    public static Choice<AlbumMultipleWrapper, AlbumSingleWrapper> album(android.app.Fragment fragment) {
        return new AlbumChoice(fragment.getContext());
    }

    public static BasicGalleryWrapper<GalleryWrapper, String, String, String> gallery(android.app.Fragment fragment) {
        return new GalleryWrapper(fragment.getContext());
    }

    public static BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> galleryAlbum(android.app.Fragment fragment) {
        return new GalleryAlbumWrapper(fragment.getContext());
    }

}