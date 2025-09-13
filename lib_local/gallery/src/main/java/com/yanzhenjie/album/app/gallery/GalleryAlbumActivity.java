package com.yanzhenjie.album.app.gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.ItemAction;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.mvp.BaseActivity;
import com.yanzhenjie.album.util.AlbumUtils;

import java.util.ArrayList;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public class GalleryAlbumActivity extends BaseActivity implements Contract.GalleryPresenter {
    private int mCurrentPosition;
    private boolean mCheckable;
    private ArrayList<AlbumFile> mAlbumFiles;
    private Widget mWidget;
    private Contract.GalleryView<AlbumFile> mView;
    public static ItemAction<AlbumFile> sClick;
    public static ItemAction<AlbumFile> sLongClick;
    public static Action<String> sCancel;
    public static Action<ArrayList<AlbumFile>> sResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_gallery);
        mView = new GalleryView<>(this, this);
        Bundle argument = getIntent().getExtras();
        assert argument != null;
        mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
        mAlbumFiles = argument.getParcelableArrayList(Album.KEY_INPUT_CHECKED_LIST);
        mCurrentPosition = argument.getInt(Album.KEY_INPUT_CURRENT_POSITION);
        mCheckable = argument.getBoolean(Album.KEY_INPUT_GALLERY_CHECKABLE);
        mView.setTitle("");
        mView.setupViews(mWidget, mCheckable);
        mView.bindData(mAlbumFiles);
        if (mCurrentPosition == 0) {
            onCurrentChanged(mCurrentPosition);
        } else {
            mView.setCurrentItem(mCurrentPosition);
        }
        setCheckedCount();
    }

    private void setCheckedCount() {
        int checkedCount = 0;
        for (AlbumFile albumFile : mAlbumFiles) {
            if (albumFile.isChecked()) checkedCount += 1;
        }
        String completeText = getString(R.string.album_menu_finish);
        completeText += "(" + checkedCount + " / " + mAlbumFiles.size() + ")";
        mView.setCompleteText(completeText);
    }

    @Override
    public void clickItem(int position) {
        if (sClick != null) {
            sClick.onAction(GalleryAlbumActivity.this, mAlbumFiles.get(mCurrentPosition));
        }
    }

    @Override
    public void longClickItem(int position) {
        if (sLongClick != null) {
            sLongClick.onAction(GalleryAlbumActivity.this, mAlbumFiles.get(mCurrentPosition));
        }
    }

    @Override
    public void onCurrentChanged(int position) {
        mCurrentPosition = position;
        mView.setSubTitle(position + 1 + " / " + mAlbumFiles.size());
        AlbumFile albumFile = mAlbumFiles.get(position);
        if (mCheckable) mView.setChecked(albumFile.isChecked());
        mView.setLayerDisplay(albumFile.isDisable());
        if (albumFile.getMediaType() == AlbumFile.TYPE_VIDEO) {
            if (!mCheckable) mView.setBottomDisplay(true);
            mView.setDuration(AlbumUtils.convertDuration(albumFile.getDuration()));
            mView.setDurationDisplay(true);
        } else {
            if (!mCheckable) mView.setBottomDisplay(false);
            mView.setDurationDisplay(false);
        }
    }

    @Override
    public void onCheckedChanged() {
        AlbumFile albumFile = mAlbumFiles.get(mCurrentPosition);
        albumFile.setChecked(!albumFile.isChecked());
        setCheckedCount();
    }

    @Override
    public void complete() {
        if (sResult != null) {
            ArrayList<AlbumFile> checkedList = new ArrayList<>();
            for (AlbumFile albumFile : mAlbumFiles) {
                if (albumFile.isChecked()) checkedList.add(albumFile);
            }
            sResult.onAction(checkedList);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (sCancel != null) sCancel.onAction("User canceled.");
        finish();
    }

    @Override
    public void finish() {
        sResult = null;
        sCancel = null;
        sClick = null;
        sLongClick = null;
        super.finish();
    }

}