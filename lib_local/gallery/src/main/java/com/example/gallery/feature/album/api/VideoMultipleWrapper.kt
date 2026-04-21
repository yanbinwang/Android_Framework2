package com.example.gallery.feature.album.api

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.app.album.AlbumActivity
import com.example.gallery.feature.album.callback.Filter
import com.example.gallery.feature.album.model.AlbumFile

/**
 * 视频多选专用包装类
 * 继承自：BasicChoiceVideoWrapper（视频专属父类）
 * 功能：只选视频 + 多选 + 视频时长过滤 + 相机录制参数
 */
class VideoMultipleWrapper(context: Context) : BasicChoiceVideoWrapper<VideoMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>>(context) {
    // 最大选择数量
    private var mLimitCount = Int.MAX_VALUE
    // 视频时长过滤器
    private var mDurationFilter: Filter<Long>? = null

    /**
     * 设置已选中的视频列表
     */
    fun checkedList(checked: ArrayList<AlbumFile>): VideoMultipleWrapper {
        this.mChecked = checked
        return this
    }

    /**
     * 设置已选中的视频列表
     */
    fun selectCount(@IntRange(from = 1, to = Long.MAX_VALUE) count: Int): VideoMultipleWrapper {
        this.mLimitCount = count
        return this
    }

    /**
     * 设置视频时长过滤（只显示符合时长的视频）
     */
    fun filterDuration(filter: Filter<Long>): VideoMultipleWrapper {
        this.mDurationFilter = filter
        return this
    }

    /**
     * 启动视频多选页面
     */
    override fun start() {
        // 传递过滤器和回调
        AlbumActivity.Companion.sSizeFilter = mSizeFilter
        AlbumActivity.Companion.sMimeFilter = mMimeTypeFilter
        AlbumActivity.Companion.sDurationFilter = mDurationFilter
        AlbumActivity.Companion.sResult = mResult
        AlbumActivity.Companion.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)
        // 功能 = 只选视频
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_VIDEO)
        // 模式 = 多选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE)
        // 其他配置
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        // 相机录制视频的参数（父类传来的）
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality)
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration)
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes)
        mContext.startActivity(intent)
    }

}