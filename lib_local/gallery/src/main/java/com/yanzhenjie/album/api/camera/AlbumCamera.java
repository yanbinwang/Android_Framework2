package com.yanzhenjie.album.api.camera;

import android.content.Context;

import com.yanzhenjie.album.api.ImageCameraWrapper;
import com.yanzhenjie.album.api.VideoCameraWrapper;

/**
 * 相机总入口类
 * 实现 Camera 接口，提供【拍照】和【录像】两大功能
 */
public class AlbumCamera implements Camera<ImageCameraWrapper, VideoCameraWrapper> {
    private final Context mContext;

    public AlbumCamera(Context context) {
        mContext = context;
    }

    /**
     * 打开相机：只拍照
     */
    @Override
    public ImageCameraWrapper image() {
        return new ImageCameraWrapper(mContext);
    }

    /**
     * 打开相机：只录像
     */
    @Override
    public VideoCameraWrapper video() {
        return new VideoCameraWrapper(mContext);
    }

}