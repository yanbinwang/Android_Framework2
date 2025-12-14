package com.yanzhenjie.album;

import android.widget.ImageView;

/**
 * <p>Used to load the preview, it should be customized.</p>
 * Created by Yan Zhenjie on 2017/3/31.
 */
public interface AlbumLoader {

    AlbumLoader DEFAULT = new AlbumLoader() {
        @Override
        public void load(ImageView imageView, AlbumFile albumFile) {
        }

        @Override
        public void load(ImageView imageView, String url) {
        }
    };

    /**
     * Load a preview of the album file.
     *
     * @param imageView {@link ImageView}.
     * @param albumFile the media object may be a picture or video.
     */
    void load(ImageView imageView, AlbumFile albumFile);

    /**
     * Load thumbnails of pictures or videos, either local file or remote file.
     *
     * @param imageView {@link ImageView}.
     * @param url       The url of the file, local path or remote path.
     */
    void load(ImageView imageView, String url);

}