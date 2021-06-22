package com.example.common.imageloader.glide.callback.progress

/**
 *  Created by wangyanbin
 *
 */
interface OnProgressLoaderListener {

    fun onStart()

    fun onProgress(progress: Int = 0)

    fun onComplete()

}