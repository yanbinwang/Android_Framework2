package com.yanzhenjie.album.callback;

import android.widget.ImageView;

import com.yanzhenjie.album.model.AlbumFile;

/**
 * 相册图片加载器接口
 * 用于加载相册缩略图、预览图，必须由用户自定义实现（Glide/Picasso/Fresco/Coil）
 */
public interface AlbumLoader {

    /**
     * 默认空实现（无加载能力，必须自定义）
     */
    AlbumLoader DEFAULT = new AlbumLoader() {
        @Override
        public void load(ImageView imageView, AlbumFile albumFile) {
        }

        @Override
        public void load(ImageView imageView, String url) {
        }
    };

    /**
     * 加载相册文件（图片/视频）的缩略图 / 预览图
     *
     * @param imageView  显示图片的View
     * @param albumFile  相册文件实体（图片/视频）
     */
    void load(ImageView imageView, AlbumFile albumFile);

    /**
     * 根据路径加载图片（本地路径 / 网络地址）
     *
     * @param imageView  显示图片的View
     * @param url        文件路径（本地路径或远程URL）
     */
    void load(ImageView imageView, String url);

}