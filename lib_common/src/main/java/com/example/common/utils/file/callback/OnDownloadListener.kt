package com.example.common.utils.file.callback

/**
 * Created by WangYanBin on 2020/6/1.
 */
interface OnDownloadListener {

    //下载成功
    fun onDownloadSuccess(path: String?)

    //下载进度
    fun onDownloading(progress: Int = 0)

    //下载失败
    fun onDownloadFailed(e: Throwable?)

}