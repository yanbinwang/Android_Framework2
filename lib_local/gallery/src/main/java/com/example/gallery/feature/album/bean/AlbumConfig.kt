package com.example.gallery.feature.album.bean

import com.example.gallery.feature.album.api.callback.AlbumLoader

/**
 * 相册全局配置类
 * 用于统一配置相册的图片加载器（AlbumLoader），采用 Builder 构建模式
 */
data class AlbumConfig(
    var albumLoader: AlbumLoader // 图片加载器（Glide/Picasso等）
) {

    companion object {
        /**
         * 获取构建器实例
         */
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    /**
     * 私有构造，通过 Builder 创建实例
     */
    constructor(builder: Builder) : this(builder.mLoader ?: AlbumLoader.DEFAULT)

    /**
     * 类构建器
     */
    class Builder {
        var mLoader: AlbumLoader? = null

        /**
         * 设置自定义图片加载器
         */
        fun setAlbumLoader(loader: AlbumLoader): Builder {
            this.mLoader = loader
            return this
        }

        /**
         * 构建最终配置对象
         */
        fun build(): AlbumConfig {
            return AlbumConfig(this)
        }
    }

}