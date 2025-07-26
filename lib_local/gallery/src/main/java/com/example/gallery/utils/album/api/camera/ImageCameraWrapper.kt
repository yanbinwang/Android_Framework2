package com.example.gallery.utils.album.api.camera

import android.content.Context
import android.content.Intent
import com.example.gallery.activity.CameraActivity
import com.example.gallery.utils.album.Album

class ImageCameraWrapper(context: Context?) : BasicCameraWrapper<ImageCameraWrapper>(context) {

    override fun start() {
        CameraActivity.sResult = mResult
        CameraActivity.sCancel = mCancel
        val intent = Intent(mContext, CameraActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_IMAGE)
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath)
        mContext.startActivity(intent)
    }

}