package com.yanzhenjie.album.api

import android.content.Context
import android.content.Intent
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.app.gallery.GalleryActivity

/**
 * 路径预览包装器
 * 继承自：BasicGalleryWrapper
 * 功能：只预览【图片路径字符串】，不是 AlbumFile 打开的是：GalleryActivity
 */
class GalleryWrapper(context: Context) : BasicGalleryWrapper<GalleryWrapper, String, String, String>(context) {

    /**
     * 启动路径预览页面
     */
    override fun start() {
        // 给 GalleryActivity 设置静态回调
        GalleryActivity.sResult = mResult
        GalleryActivity.sCancel = mCancel
        GalleryActivity.sClick = mItemClick
        GalleryActivity.sLongClick = mItemLongClick
        // 跳转预览
        val intent = Intent(mContext, GalleryActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putStringArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)
        intent.putExtra(Album.KEY_INPUT_CURRENT_POSITION, mCurrentPosition)
        intent.putExtra(Album.KEY_INPUT_GALLERY_CHECKABLE, mCheckable)
        mContext.startActivity(intent)
    }

}