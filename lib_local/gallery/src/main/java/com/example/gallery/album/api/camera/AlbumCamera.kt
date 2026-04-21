package com.example.gallery.album.api.camera

import android.content.Context
import com.example.gallery.album.api.ImageCameraWrapper
import com.example.gallery.album.api.VideoCameraWrapper

/**
 * 相机总入口类
 * 实现 Camera 接口，提供【拍照】和【录像】两大功能
 */
class AlbumCamera(private val context: Context) : Camera<ImageCameraWrapper, VideoCameraWrapper> {

    /**
     * 打开相机：只拍照
     */
    override fun image(): ImageCameraWrapper {
        return ImageCameraWrapper(context)
    }

    /**
     * 打开相机：只录像
     */
    override fun video(): VideoCameraWrapper {
        return VideoCameraWrapper(context)
    }

}