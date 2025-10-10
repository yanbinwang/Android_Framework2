package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.app.gallery.GalleryAlbumActivity;

/**
 * <p>Gallery wrapper.</p>
 * Created by yanzhenjie on 17-3-29.
 */
public class GalleryAlbumWrapper extends BasicGalleryWrapper<GalleryAlbumWrapper, AlbumFile, String, AlbumFile> {

    public GalleryAlbumWrapper(Context context) {
        super(context);
    }

    @Override
    public void start() {
        GalleryAlbumActivity.sResult = mResult;
        GalleryAlbumActivity.sCancel = mCancel;
        GalleryAlbumActivity.sClick = mItemClick;
        GalleryAlbumActivity.sLongClick = mItemLongClick;
        Intent intent = new Intent(mContext, GalleryAlbumActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked);
        intent.putExtra(Album.KEY_INPUT_CURRENT_POSITION, mCurrentPosition);
        intent.putExtra(Album.KEY_INPUT_GALLERY_CHECKABLE, mCheckable);
        mContext.startActivity(intent);
    }

}