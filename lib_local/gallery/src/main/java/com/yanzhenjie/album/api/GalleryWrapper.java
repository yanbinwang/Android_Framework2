package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.gallery.GalleryActivity;

/**
 * <p>Gallery wrapper.</p>
 * Created by yanzhenjie on 17-3-29.
 */
public class GalleryWrapper extends BasicGalleryWrapper<GalleryWrapper, String, String, String> {

    public GalleryWrapper(Context context) {
        super(context);
    }

    @Override
    public void start() {
        GalleryActivity.sResult = mResult;
        GalleryActivity.sCancel = mCancel;
        GalleryActivity.sClick = mItemClick;
        GalleryActivity.sLongClick = mItemLongClick;
        Intent intent = new Intent(mContext, GalleryActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        intent.putStringArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked);
        intent.putExtra(Album.KEY_INPUT_CURRENT_POSITION, mCurrentPosition);
        intent.putExtra(Album.KEY_INPUT_GALLERY_CHECKABLE, mCheckable);
        mContext.startActivity(intent);
    }

}