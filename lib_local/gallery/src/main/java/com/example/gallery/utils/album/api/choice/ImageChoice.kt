package com.example.gallery.utils.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.choice.Choice

class ImageChoice(private val mContext: Context?) : Choice<ImageMultipleWrapper, ImageSingleWrapper> {

    override fun multipleChoice(): ImageMultipleWrapper {
        return ImageMultipleWrapper(mContext)
    }

    override fun singleChoice(): ImageSingleWrapper {
        return ImageSingleWrapper(mContext)
    }

}