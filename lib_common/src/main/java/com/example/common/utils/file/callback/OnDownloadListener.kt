package com.example.common.utils.file.callback

/**
 * Created by WangYanBin on 2020/6/1.
 */
interface OnDownloadListener {

    //下载开始
    fun onStart()

    //下载成功
    fun onSuccess(path: String?)

    //下载进度
    fun onLoading(progress: Int = 0)

    //下载失败
    fun onFailed(e: Throwable?)

    //下载完成
    fun onComplete()

}