package com.example.thirdparty.album

import android.widget.ImageView
import com.example.glide.ImageLoader
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.AlbumLoader

/**
 * author: wyb
 * date: 2017/8/29.
 * 相册使用glide图片加载库
 */
class GlideLoader : AlbumLoader {

    override fun load(imageView: ImageView, albumFile: AlbumFile) {
        load(imageView, albumFile.path)
    }

    override fun load(imageView: ImageView, url: String) {
        ImageLoader.instance.display(imageView, url)
//        Glide.with(imageView.context).load(url).placeholder(R.drawable.shape_glide_bg).error(R.drawable.shape_glide_bg).dontAnimate().into(imageView)
    }

}