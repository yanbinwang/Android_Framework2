package com.yanzhenjie.album.api.camera

import android.content.Context
import com.yanzhenjie.album.api.ImageCameraWrapper
import com.yanzhenjie.album.api.VideoCameraWrapper

/**
 * Created by YanZhenjie on 2017/8/18.
 */
class AlbumCamera(private val mContext: Context) : Camera<ImageCameraWrapper, VideoCameraWrapper> {

    override fun image(): ImageCameraWrapper {
        return ImageCameraWrapper(mContext)
    }

    override fun video(): VideoCameraWrapper {
        return VideoCameraWrapper(mContext)
    }

}