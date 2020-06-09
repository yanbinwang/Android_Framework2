package com.example.common.imageloader.glide.callback

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.common.R
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.AlbumLoader

/**
 * author: wyb
 * date: 2017/8/29.
 * 相册使用glide图片加载库
 */
class GlideAlbumLoader : AlbumLoader {

    override fun load(imageView: ImageView, albumFile: AlbumFile) {
        load(imageView, albumFile.path)
    }

    override fun load(imageView: ImageView, url: String) {
        Glide.with(imageView.context).load(url).placeholder(R.drawable.shape_loading_normal).error(R.drawable.shape_loading_normal).dontAnimate().into(imageView)
    }

}