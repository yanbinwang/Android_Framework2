/*
 * Copyright 2018 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.album.app.album;

import static com.yanzhenjie.album.mvp.BaseActivity.setSupportToolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFolder;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.impl.DoubleClickWrapper;
import com.yanzhenjie.album.util.AlbumUtils;
import com.yanzhenjie.album.widget.ColorProgressBar;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;

/**
 * Created by YanZhenjie on 2018/4/7.
 */
@SuppressLint("NotifyDataSetChanged")
class AlbumView extends Contract.AlbumView implements View.OnClickListener {
    private Activity mActivity;
    private Toolbar mToolbar;
    private MenuItem mCompleteMenu;
    private TextView mTitle;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private AlbumAdapter mAdapter;
    private Button mBtnPreview;
    private Button mBtnSwitchFolder;
    private LinearLayout mLayoutLoading;
    private ColorProgressBar mProgressBar;

    public AlbumView(Activity activity, Contract.AlbumPresenter presenter) {
        super(activity, presenter);
        this.mActivity = activity;
        this.mToolbar = activity.findViewById(R.id.toolbar);
        setSupportToolbar(mToolbar);
        this.mTitle = activity.findViewById(R.id.tv_title);
        this.mRecyclerView = activity.findViewById(R.id.recycler_view);
        this.mBtnSwitchFolder = activity.findViewById(R.id.btn_switch_dir);
        this.mBtnPreview = activity.findViewById(R.id.btn_preview);
        this.mLayoutLoading = activity.findViewById(R.id.layout_loading);
        this.mProgressBar = activity.findViewById(R.id.progress_bar);
        this.mToolbar.setOnClickListener(new DoubleClickWrapper(this));
        this.mBtnSwitchFolder.setOnClickListener(this);
        this.mBtnPreview.setOnClickListener(this);
    }

    @Override
    protected void onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu_album, menu);
        mCompleteMenu = menu.findItem(R.id.album_menu_finish);
    }

    @Override
    protected void onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.album_menu_finish) {
            getPresenter().complete();
        }
    }

    @Override
    public void setupViews(Widget widget, int column, boolean hasCamera, int choiceMode) {
        int mStatusColor = widget.getStatusBarColor();
        mTitle.setText(widget.getTitle());
        mToolbar.setBackgroundColor(getColor(mStatusColor));
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            mTitle.setTextColor(getColor(R.color.textBlack));
            mProgressBar.setColorFilter(getColor(R.color.albumLoadingDark));
            Drawable navigationIcon = getDrawable(R.drawable.album_ic_back_white);
            AlbumUtils.setDrawableTint(navigationIcon, getColor(R.color.albumIconDark));
            setHomeAsUpIndicator(navigationIcon);
            Drawable completeIcon = mCompleteMenu.getIcon();
            assert completeIcon != null;
            AlbumUtils.setDrawableTint(completeIcon, getColor(R.color.albumIconDark));
            mCompleteMenu.setIcon(completeIcon);
        } else {
            mTitle.setTextColor(getColor(R.color.textWhite));
            mProgressBar.setColorFilter(getColor(widget.getStatusBarColor()));
            setHomeAsUpIndicator(R.drawable.album_ic_back_white);
        }
        if (choiceMode == Album.MODE_SINGLE) {
            mBtnPreview.setVisibility(View.GONE);
        }
        Configuration config = mActivity.getResources().getConfiguration();
        mLayoutManager = new GridLayoutManager(getContext(), column, getOrientation(config), false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        int dividerSize = getResources().getDimensionPixelSize(R.dimen.album_dp_4);
        mRecyclerView.addItemDecoration(new Api21ItemDivider(Color.TRANSPARENT, dividerSize, dividerSize));
        mAdapter = new AlbumAdapter(getContext(), hasCamera, choiceMode, widget.getMediaItemCheckSelector());
        mAdapter.setAddClickListener((view, position) -> getPresenter().clickCamera(view));
        mAdapter.setCheckedClickListener((button, position) -> getPresenter().tryCheckItem(button, position));
        mAdapter.setItemClickListener((view, position) -> getPresenter().tryPreviewItem(position));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setLoadingDisplay(boolean display) {
        mLayoutLoading.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int position = mLayoutManager.findFirstVisibleItemPosition();
        mLayoutManager.setOrientation(getOrientation(newConfig));
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.scrollToPosition(position);
    }

    @RecyclerView.Orientation
    private int getOrientation(Configuration config) {
        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                return LinearLayoutManager.VERTICAL;
            }
            case Configuration.ORIENTATION_LANDSCAPE: {
                return LinearLayoutManager.HORIZONTAL;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    @Override
    public void setCompleteDisplay(boolean display) {
        mCompleteMenu.setVisible(display);
    }

    @Override
    public void bindAlbumFolder(AlbumFolder albumFolder) {
        mBtnSwitchFolder.setText(albumFolder.getName());

        mAdapter.setAlbumFiles(albumFolder.getAlbumFiles());
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void notifyInsertItem(int position) {
        mAdapter.notifyItemInserted(position);
    }

    @Override
    public void notifyItem(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void setCheckedCount(int count) {
        mBtnPreview.setText(" (" + count + ")");
    }

    @Override
    public void onClick(View v) {
        if (v == mToolbar) {
            mRecyclerView.smoothScrollToPosition(0);
        } else if (v == mBtnSwitchFolder) {
            getPresenter().clickFolderSwitch();
        } else if (v == mBtnPreview) {
            getPresenter().tryPreviewChecked();
        }
    }

}