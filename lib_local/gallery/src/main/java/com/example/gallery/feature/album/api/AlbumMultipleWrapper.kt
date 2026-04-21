package com.example.gallery.feature.album.api

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.app.album.AlbumActivity
import com.example.gallery.feature.album.callback.Filter
import com.example.gallery.feature.album.model.AlbumFile

/**
 * 图片 + 视频 **多选模式** 封装
 * 继承自：BasicChoiceAlbumWrapper（混合选择器父类）
 * 功能：对外提供链式调用，最终跳转到 AlbumActivity 多选页面
 */
class AlbumMultipleWrapper(context: Context) : BasicChoiceAlbumWrapper<AlbumMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>>(context) {
    // 最大选择数量，默认无限制
    private var mLimitCount = Int.MAX_VALUE
    // 视频时长过滤器
    private var mDurationFilter: Filter<Long>? = null

    /**
     * 设置已经选中的图片列表（用于编辑）
     */
    fun checkedList(checked: ArrayList<AlbumFile>): AlbumMultipleWrapper {
        this.mChecked = checked
        return this
    }

    /**
     * 设置最大可选数量
     */
    fun selectCount(@IntRange(from = 1, to = Long.MAX_VALUE) count: Int): AlbumMultipleWrapper {
        this.mLimitCount = count
        return this
    }

    /**
     * 过滤视频时长
     */
    fun filterDuration(filter: Filter<Long>): AlbumMultipleWrapper {
        this.mDurationFilter = filter
        return this
    }

    /**
     * 启动相册多选页面
     * 把所有配置通过 Intent 传给 AlbumActivity
     */
    override fun start() {
        // 把过滤器、回调 赋值给静态变量，让 AlbumActivity 可以拿到
        AlbumActivity.Companion.sSizeFilter = mSizeFilter
        AlbumActivity.Companion.sMimeFilter = mMimeTypeFilter
        AlbumActivity.Companion.sDurationFilter = mDurationFilter
        AlbumActivity.Companion.sResult = mResult
        AlbumActivity.Companion.sCancel = mCancel
        // 跳转到相册主页面
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)
        // 功能 = 混合选择（图片+视频）
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_ALBUM)
        // 模式 = 多选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE)
        // 列数
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        // 是否显示相机
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        // 最大选择数量
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount)
        // 过滤后是否显示
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        // 相机视频质量
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality)
        // 相机最大时长
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration)
        // 相机最大大小
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes)
        mContext.startActivity(intent)
    }

}