package com.example.gallery.feature.album.api

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.app.album.AlbumActivity
import com.example.gallery.feature.album.model.AlbumFile

/**
 * 图片多选专用包装器
 * 继承自：BasicChoiceWrapper
 * 功能：只选图片 + 多选
 */
class ImageMultipleWrapper(context: Context) : BasicChoiceWrapper<ImageMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>>(context) {
    // 最大选择数量
    @IntRange(from = 1, to = Long.MAX_VALUE)
    private var mLimitCount = Int.MAX_VALUE

    /**
     * 设置已选中的列表
     */
    fun checkedList(checked: ArrayList<AlbumFile>): ImageMultipleWrapper {
        this.mChecked = checked
        return this
    }

    /**
     * 设置最多选多少张
     */
    fun selectCount(@IntRange(from = 1, to = Long.MAX_VALUE) count: Int): ImageMultipleWrapper {
        this.mLimitCount = count
        return this
    }

    /**
     * 启动图片多选页面
     */
    override fun start() {
        // 把过滤器、回调丢给 AlbumActivity
        AlbumActivity.Companion.sSizeFilter = mSizeFilter
        AlbumActivity.Companion.sMimeFilter = mMimeTypeFilter
        AlbumActivity.Companion.sResult = mResult
        AlbumActivity.Companion.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)
        // 功能 = 只选图片
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_IMAGE)
        // 模式 = 多选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE)
        // 列数、相机、数量、过滤...
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        mContext.startActivity(intent)
    }

}