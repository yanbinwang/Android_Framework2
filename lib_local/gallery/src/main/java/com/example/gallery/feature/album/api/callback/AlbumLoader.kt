package com.example.gallery.feature.album.api.callback

import android.widget.ImageView
import com.example.gallery.feature.album.bean.AlbumFile

/**
 * 相册图片加载器接口
 * 用于加载相册缩略图、预览图，必须由用户自定义实现（Glide/Picasso/Fresco/Coil）
 */
interface AlbumLoader {

    companion object {
        /**
         * 默认空实现（无加载能力，必须自定义）
         */
        val DEFAULT = object : AlbumLoader {
            override fun load(imageView: ImageView?, albumFile: AlbumFile) {
            }

            override fun load(imageView: ImageView?, url: String?) {
            }
        }
    }

    /**
     * 加载相册文件（图片/视频）的缩略图 / 预览图
     *
     * @param imageView  显示图片的View
     * @param albumFile  相册文件实体（图片/视频）
     */
    fun load(imageView: ImageView?, albumFile: AlbumFile)

    /**
     * 根据路径加载图片（本地路径 / 网络地址）
     *
     * @param imageView  显示图片的View
     * @param url        文件路径（本地路径或远程URL）
     */
    fun load(imageView: ImageView?, url: String?)

}