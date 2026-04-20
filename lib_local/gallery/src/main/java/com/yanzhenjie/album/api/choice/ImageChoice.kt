package com.yanzhenjie.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.ImageMultipleWrapper
import com.yanzhenjie.album.api.ImageSingleWrapper

/**
 * 图片选择器总入口
 * 实现 Choice 接口，提供【图片多选 / 图片单选】
 */
class ImageChoice(private val context: Context) : Choice<ImageMultipleWrapper, ImageSingleWrapper> {

    /**
     * 图片多选
     */
    override fun multipleChoice(): ImageMultipleWrapper {
        return ImageMultipleWrapper(context)
    }

    /**
     * 图片单选
     */
    override fun singleChoice(): ImageSingleWrapper {
        return ImageSingleWrapper(context)
    }

}