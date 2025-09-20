package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.camera.CameraActivity;

/**
 * <p>Camera wrapper.</p>
 * Created by Yan Zhenjie on 2017/4/18.
 */
public class ImageCameraWrapper extends BasicCameraWrapper<ImageCameraWrapper> {

    public ImageCameraWrapper(Context context) {
        super(context);
    }

    public void start() {
        CameraActivity.sResult = mResult;
        CameraActivity.sCancel = mCancel;
        Intent intent = new Intent(mContext, CameraActivity.class);
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_IMAGE);
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath);
        mContext.startActivity(intent);
    }

}