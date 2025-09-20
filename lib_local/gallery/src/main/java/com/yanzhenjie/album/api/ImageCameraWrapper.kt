package com.yanzhenjie.album.api

import android.content.Context
import android.content.Intent
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.app.camera.CameraActivity

/**
 * <p>Camera wrapper.</p>
 * Created by Yan Zhenjie on 2017/4/18.
 */
class ImageCameraWrapper(context: Context) : BasicCameraWrapper<ImageCameraWrapper>(context) {

    override fun start() {
        CameraActivity.sResult = mResult
        CameraActivity.sCancel = mCancel
        val intent = Intent(mContext, CameraActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_IMAGE)
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath)
        mContext?.startActivity(intent)
    }

}