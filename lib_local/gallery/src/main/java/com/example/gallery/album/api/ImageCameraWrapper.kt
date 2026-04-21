package com.example.gallery.album.api

import android.content.Context
import android.content.Intent
import com.example.gallery.album.Album
import com.example.gallery.album.app.camera.CameraActivity

/**
 * 相机拍照专用包装类
 * 继承自：BasicCameraWrapper（相机顶层抽象类）
 * 功能：只做一件事 —— 打开相机拍照 → 返回图片路径
 */
class ImageCameraWrapper(context: Context) : BasicCameraWrapper<ImageCameraWrapper>(context) {

    /**
     * 启动拍照页面（CameraActivity）
     */
    override fun start() {
        // 静态注入回调，让相机页面接收
        CameraActivity.sResult = mResult
        CameraActivity.sCancel = mCancel
        val intent = Intent(mContext, CameraActivity::class.java)
        // 功能类型：拍照
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CAMERA_IMAGE)
        // 保存路径
        intent.putExtra(Album.KEY_INPUT_FILE_PATH, mFilePath)
        mContext.startActivity(intent)
    }

}