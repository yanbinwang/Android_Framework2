package com.example.gallery.album.api

import android.content.Context
import android.content.Intent
import com.example.gallery.album.Album
import com.example.gallery.album.app.album.AlbumActivity
import com.example.gallery.album.model.AlbumFile

/**
 * 图片单选专用包装类
 * 继承自：BasicChoiceWrapper
 * 功能：打开相册 → 只选图片 → 只能选 1 张 → 直接返回
 */
class ImageSingleWrapper(context: Context) : BasicChoiceWrapper<ImageSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile>(context) {

    /**
     * 启动图片单选页面
     */
    override fun start() {
        // 把过滤、回调交给 AlbumActivity
        AlbumActivity.sSizeFilter = mSizeFilter
        AlbumActivity.sMimeFilter = mMimeTypeFilter
        AlbumActivity.sResult = mResult
        AlbumActivity.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        // 功能 = 纯图片选择
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_IMAGE)
        // 模式 = 单选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_SINGLE)
        // 列数、相机、最大数量=1、过滤显示
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, 1)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        mContext.startActivity(intent)
    }

}