package com.example.gallery.feature.album.api

import android.content.Context
import android.content.Intent
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.app.album.AlbumActivity
import com.example.gallery.feature.album.api.callback.Filter
import com.example.gallery.feature.album.bean.AlbumFile

/**
 * 视频单选专用包装类
 * 继承自：BasicChoiceVideoWrapper
 * 功能：只选视频 + 单选（只能选1个）
 */
class VideoSingleWrapper(context: Context) : BasicChoiceVideoWrapper<VideoSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile>(context) {
    // 视频时长过滤器
    private var mDurationFilter: Filter<Long>? = null

    /**
     * 过滤视频时长
     */
    fun filterDuration(filter: Filter<Long>): VideoSingleWrapper {
        this.mDurationFilter = filter
        return this
    }

    /**
     * 启动视频单选页面
     */
    override fun start() {
        // 传递过滤器 + 回调
        AlbumActivity.sSizeFilter = mSizeFilter
        AlbumActivity.sMimeFilter = mMimeTypeFilter
        AlbumActivity.sDurationFilter = mDurationFilter
        AlbumActivity.sResult = mResult
        AlbumActivity.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        // 功能 = 只选视频
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_VIDEO)
        // 模式 = 单选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_SINGLE)
        // 列数、相机、数量=1、过滤、视频参数...
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, 1)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality)
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration)
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes)
        mContext.startActivity(intent)
    }

}