package com.example.gallery.utils

import android.widget.ImageView
import com.example.gallery.feature.album.callback.AlbumLoader
import com.example.gallery.feature.album.model.AlbumFile
import com.example.glide.ImageLoader

/**
 * author: wyb
 * date: 2017/8/29.
 * 相册使用glide图片加载库
 */
class GlideLoader : AlbumLoader {

    override fun load(imageView: ImageView?, albumFile: AlbumFile) {
        load(imageView, albumFile.path)
    }

    override fun load(imageView: ImageView?, url: String?) {
        ImageLoader.instance.loadImageFromUrl(imageView, url)
    }

}