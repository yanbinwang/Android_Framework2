package com.yanzhenjie.album.api.camera;

import android.content.Context;

import com.yanzhenjie.album.api.ImageCameraWrapper;
import com.yanzhenjie.album.api.VideoCameraWrapper;

/**
 * Created by YanZhenjie on 2017/8/18.
 */
public class AlbumCamera implements Camera<ImageCameraWrapper, VideoCameraWrapper> {
    private Context mContext;

    public AlbumCamera(Context context) {
        mContext = context;
    }

    @Override
    public ImageCameraWrapper image() {
        return new ImageCameraWrapper(mContext);
    }

    @Override
    public VideoCameraWrapper video() {
        return new VideoCameraWrapper(mContext);
    }

}