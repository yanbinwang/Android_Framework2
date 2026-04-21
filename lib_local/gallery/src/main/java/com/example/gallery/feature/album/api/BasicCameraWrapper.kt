package com.example.gallery.feature.album.api

import android.content.Context
import com.example.gallery.feature.album.callback.Action

/**
 * 相机功能 顶层抽象父类
 * 地位和 BasicAlbumWrapper 平级 专门给拍照、录像 功能用的
 * 功能：统一相机的 路径、回调、启动
 */
abstract class BasicCameraWrapper<Returner : BasicCameraWrapper<Returner>>(val mContext: Context) {
    // 拍照/录像 保存的文件路径
    protected var mFilePath: String? = null
    // 成功回调（返回文件路径）
    protected var mResult: Action<String>? = null
    // 取消回调
    protected var mCancel: Action<String>? = null

    /**
     * 设置成功回调
     */
    fun onResult(result: Action<String>): Returner {
        this.mResult = result
        return this as Returner
    }

    /**
     * 设置取消回调
     */
    fun onCancel(cancel: Action<String>): Returner {
        this.mCancel = cancel
        return this as Returner
    }

    /**
     * 设置自定义保存路径
     */
    fun filePath(filePath: String?): Returner {
        this.mFilePath = filePath
        return this as Returner
    }

    /**
     * 抽象启动方法
     * 子类：拍照、录像 各自实现
     */
    abstract fun start()

}