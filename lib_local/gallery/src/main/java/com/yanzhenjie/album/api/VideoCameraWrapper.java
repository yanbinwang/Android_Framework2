package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.camera.CameraActivity;

/**
 * 视频录制专用包装类
 * 继承自：BasicCameraWrapper
 * 功能：打开相机 → 录制视频 → 支持质量、时长、大小限制
 */
public class VideoCameraWrapper extends BasicCameraWrapper<VideoCameraWrapper> {
    // 视频质量：0低质量 1高质量（默认1）
    private int mQuality = 1;
    // 最大录制时长（秒）
    private long mLimitDuration = Integer.MAX_VALUE;
    // 最大文件大小（字节）
    private long mLimitBytes = Integer.MAX_VALUE;

    public VideoCameraWrapper(Context context) {
        super(context);
    }

    /**
     * 设置视频录制质量
     * @param quality 0 / 1
     */
    public VideoCameraWrapper quality(@IntRange(from = 0, to = 1) int quality) {
        this.mQuality = quality;
        return this;
    }

    /**
     * 设置最大录制时长（秒）
     */
    public VideoCameraWrapper limitDuration(@IntRange(from = 1) long duration) {
        this.mLimitDuration = duration;
        return this;
    }

    /**
     * 设置视频最大文件大小
     */
    public VideoCameraWrapper limitBytes(@IntRange(from = 1) long bytes) {
        this.mLimitBytes = bytes;
        return this;
    }

    /**
     * 启动相机录像页面
     */
    @Override
    public void start() {
        // 静态注入回调
        CameraActivity.sResult = mResult;
        CameraActivity.sCancel = mCancel;
        Intent intent = new Intent(mContext, CameraActivity.class);
        // 功能 = 录像
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_VIDEO);
        // 保存路径
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath);
        // 视频质量、时长、大小
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality);
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration);
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes);
        mContext.startActivity(intent);
    }

}