package com.yanzhenjie.album.api

import android.content.Context
import android.content.Intent
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.app.gallery.GalleryAlbumActivity
import com.yanzhenjie.album.model.AlbumFile

/**
 * 相册预览大图包装器
 * 继承自：BasicGalleryWrapper（预览顶层抽象）
 * 功能：外部调用 → 打开大图预览页面（GalleryAlbumActivity）
 */
class GalleryAlbumWrapper(context: Context) : BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile>(context) {

    /**
     * 启动预览页面
     * 把所有配置传给 GalleryAlbumActivity
     */
    override fun start() {
        // 静态赋值，让预览页面接收回调
        GalleryAlbumActivity.sResult = mResult
        GalleryAlbumActivity.sCancel = mCancel
        GalleryAlbumActivity.sClick = mItemClick
        GalleryAlbumActivity.sLongClick = mItemLongClick
        // 跳转到预览页面
        val intent = Intent(mContext, GalleryAlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)
        intent.putExtra(Album.KEY_INPUT_CURRENT_POSITION, mCurrentPosition)
        intent.putExtra(Album.KEY_INPUT_GALLERY_CHECKABLE, mCheckable)
        mContext.startActivity(intent)
    }

}