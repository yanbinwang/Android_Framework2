package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.camera.CameraActivity;

/**
 * 相机拍照专用包装类
 * 继承自：BasicCameraWrapper（相机顶层抽象类）
 * 功能：只做一件事 —— 打开相机拍照 → 返回图片路径
 */
@Deprecated()
public class ImageCameraWrapper extends BasicCameraWrapper<ImageCameraWrapper> {

    public ImageCameraWrapper(Context context) {
        super(context);
    }

    /**
     * 启动拍照页面（CameraActivity）
     */
    @Override
    public void start() {
        // 静态注入回调，让相机页面接收
        CameraActivity.sResult = mResult;
        CameraActivity.sCancel = mCancel;
        Intent intent = new Intent(mContext, CameraActivity.class);
        // 功能类型：拍照
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_IMAGE);
        // 保存路径
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath);
        mContext.startActivity(intent);
    }

}