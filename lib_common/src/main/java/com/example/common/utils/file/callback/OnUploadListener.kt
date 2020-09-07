package com.example.common.utils.file.callback

/**
 * author: wyb
 * date: 2019/1/19.
 * 图片上传回调
 */
interface OnUploadListener {

    fun onStart()

    fun onSuccess(ossUrl: List<String>?)

    fun onFailed(e: Throwable?)

    fun onComplete()

}
