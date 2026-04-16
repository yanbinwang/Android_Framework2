package com.yanzhenjie.album.api

import android.content.Context
import androidx.annotation.IntRange
import com.yanzhenjie.album.callback.Filter

/**
 * 选择器通用封装
 * 继承自：BasicAlbumWrapper
 * 功能：专门封装 图片/视频/相册选择 的公共配置（相机、网格列数、文件过滤）
 * 所有选择器功能（图片、视频、全部）都继承这个类
 */
abstract class BasicChoiceWrapper<Returner : BasicChoiceWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicAlbumWrapper<Returner, Result, Cancel, Checked>(context) {
    // 列表网格列数，默认 2 列
    @JvmField
    protected var mColumnCount = 2
    // 是否显示拍照按钮，默认开启
    @JvmField
    protected var mHasCamera = true
    // 过滤后的文件是否显示（置灰）
    @JvmField
    protected var mFilterVisibility = true
    // 文件大小过滤器
    @JvmField
    protected var mSizeFilter: Filter<Long>? = null
    // 文件类型（MimeType）过滤器
    @JvmField
    protected var mMimeTypeFilter: Filter<String>? = null

    /**
     * 设置是否显示拍照入口
     */
    fun camera(hasCamera: Boolean): Returner {
        this.mHasCamera = hasCamera
        return this as Returner
    }

    /**
     * 设置列表列数（2~4列）
     */
    fun columnCount(@IntRange(from = 2, to = 4) count: Int): Returner {
        this.mColumnCount = count
        return this as Returner
    }

    /**
     * 设置文件大小过滤
     */
    fun filterSize(filter: Filter<Long>): Returner {
        this.mSizeFilter = filter
        return this as Returner
    }

    /**
     * 设置文件类型过滤
     */
    fun filterMimeType(filter: Filter<String>): Returner {
        this.mMimeTypeFilter = filter
        return this as Returner
    }

    /**
     * 设置：过滤掉的文件是否显示（只是置灰，不允许选）
     */
    fun afterFilterVisibility(visibility: Boolean): Returner {
        this.mFilterVisibility = visibility
        return this as Returner
    }

}