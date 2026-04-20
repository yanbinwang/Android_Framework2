package com.yanzhenjie.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.AlbumMultipleWrapper
import com.yanzhenjie.album.api.AlbumSingleWrapper

/**
 * 相册混合选择入口（图片 + 视频 都能选）
 * 实现 Choice 接口，提供 多选 / 单选 两种能力
 */
class AlbumChoice(private val context: Context) : Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {

    /**
     * 打开 图片+视频 多选
     */
    override fun multipleChoice(): AlbumMultipleWrapper {
        return AlbumMultipleWrapper(context)
    }

    /**
     * 打开 图片+视频 单选
     */
    override fun singleChoice(): AlbumSingleWrapper {
        return AlbumSingleWrapper(context)
    }

}