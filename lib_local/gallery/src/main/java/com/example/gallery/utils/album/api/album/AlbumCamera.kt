package com.example.gallery.utils.album.api.album

import android.content.Context
import com.example.gallery.utils.album.api.camera.ImageCameraWrapper
import com.example.gallery.utils.album.api.camera.VideoCameraWrapper
import com.yanzhenjie.album.api.camera.Camera

class AlbumCamera(private val mContext: Context?) : Camera<ImageCameraWrapper, VideoCameraWrapper> {

    override fun image(): ImageCameraWrapper {
        return ImageCameraWrapper(mContext)
    }

    override fun video(): VideoCameraWrapper {
        return VideoCameraWrapper(mContext)
    }

}