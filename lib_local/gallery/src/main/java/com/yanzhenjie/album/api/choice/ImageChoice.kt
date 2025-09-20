package com.yanzhenjie.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.ImageMultipleWrapper
import com.yanzhenjie.album.api.ImageSingleWrapper

/**
 * Created by YanZhenjie on 2017/8/16.
 */
class ImageChoice(private val mContext: Context) : Choice<ImageMultipleWrapper, ImageSingleWrapper> {

    override fun multipleChoice(): ImageMultipleWrapper {
        return ImageMultipleWrapper(mContext)
    }

    override fun singleChoice(): ImageSingleWrapper {
        return ImageSingleWrapper(mContext)
    }

}