package com.yanzhenjie.album.app.album;

import static com.example.gallery.base.BaseActivity.setSupportMenuViewAsync;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.framework.utils.function.view.FunctionsViewKt;
import com.example.gallery.R;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.model.AlbumFolder;
import com.yanzhenjie.album.model.Widget;
import com.yanzhenjie.album.utils.AlbumUtil;
import com.yanzhenjie.album.utils.DoubleClickWrapper;
import com.yanzhenjie.album.widget.ColorProgressBar;
import com.yanzhenjie.album.widget.recyclerview.divider.ItemDivider;

/**
 * 相册主页面 View 层
 * 功能：负责所有 UI 展示、事件点击、列表刷新、主题切换
 * MVP 中的 V 层
 */
@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class AlbumView extends Contract.AlbumView implements View.OnClickListener {
    // 相册列表适配器
    private AlbumAdapter mAdapter;
    // 网格布局管理器
    private GridLayoutManager mLayoutManager;
    // 右上角完成菜单
    private MenuItem mCompleteMenu;
//    // 宿主 Activity
//    private final Activity mActivity;
    // 标题栏
    private final Toolbar mToolbar;
    // 标题文字
    private final TextView mTitle;
    // 相册列表
    private final RecyclerView mRecyclerView;
    // 预览按钮
    private final Button mBtnPreview;
    // 切换文件夹按钮
    private final Button mBtnSwitchFolder;
    // 加载中布局
    private final LinearLayout mLayoutLoading;
    // 加载进度条
    private final ColorProgressBar mProgressBar;

    /**
     * 构造方法：绑定控件 + 设置点击事件
     */
    public AlbumView(Activity activity, Contract.AlbumPresenter presenter) {
        super(activity, presenter);
//        this.mActivity = activity;
        // 绑定所有控件
        this.mToolbar = activity.findViewById(R.id.toolbar);
        this.mTitle = activity.findViewById(R.id.tv_title);
        this.mRecyclerView = activity.findViewById(R.id.recycler_view);
        this.mBtnSwitchFolder = activity.findViewById(R.id.btn_switch_dir);
        this.mBtnPreview = activity.findViewById(R.id.btn_preview);
        this.mLayoutLoading = activity.findViewById(R.id.layout_loading);
        this.mProgressBar = activity.findViewById(R.id.progress_bar);
        // 点击标题栏 → 回到顶部
        this.mToolbar.setOnClickListener(new DoubleClickWrapper(this));
        // 切换文件夹
        this.mBtnSwitchFolder.setOnClickListener(this);
        // 预览已选图片
        this.mBtnPreview.setOnClickListener(this);
    }

    /**
     * 创建右上角菜单（完成按钮）
     */
    @Override
    protected void onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu_album, menu);
        mCompleteMenu = menu.findItem(R.id.album_menu_finish);
    }

    /**
     * 菜单点击：完成选择
     */
    @Override
    protected void onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.album_menu_finish) {
            getPresenter().complete();
        }
    }

    /**
     * 初始化页面样式：主题、颜色、列表、适配器
     */
    @Override
    public void setupViews(Widget widget, int column, boolean hasCamera, int choiceMode) {
        // 设置返回箭头
        Drawable navigationIcon = getDrawable(R.mipmap.gallery_ic_back);
        // 浅色 / 深色主题 -> 影响图标
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            mTitle.setTextColor(getColor(R.color.galleryFontDark));
            // 暗色返回 / 完成
            AlbumUtil.setDrawableTint(navigationIcon, getColor(R.color.galleryIconDark));
            Drawable completeIcon = mCompleteMenu.getIcon();
            if (null != completeIcon) {
                AlbumUtil.setDrawableTint(completeIcon, getColor(R.color.galleryIconDark));
                mCompleteMenu.setIcon(completeIcon);
            }
            mProgressBar.setColorFilter(getColor(R.color.albumLoading));
        } else {
            mTitle.setTextColor(getColor(R.color.galleryFontLight));
            mProgressBar.setColorFilter(getColor(widget.getStatusBarColor()));
        }
        // 设置返回按钮
        setHomeAsUpIndicator(navigationIcon);
        // 标题同步状态栏颜色
        mToolbar.setBackgroundColor(getColor(widget.getStatusBarColor()));
        mTitle.setText(widget.getTitle());
        // 单选模式隐藏预览按钮
        if (choiceMode == Album.MODE_SINGLE) {
            mBtnPreview.setVisibility(View.GONE);
        } else {
            mBtnPreview.setVisibility(View.VISIBLE);
            // 多选等 Toolbar 布局结束右侧强行撑满
            setSupportMenuViewAsync(mToolbar, widget.getStatusBarColor());
        }
        // 配置网格布局（横竖屏切换）
        mLayoutManager = new GridLayoutManager(getContext(), column, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // 设置列表间隔
        int dividerSize = getResources().getDimensionPixelSize(R.dimen.album_dp_4);
        mRecyclerView.addItemDecoration(new ItemDivider(Color.TRANSPARENT, dividerSize, dividerSize));
        // 初始化适配器
        mAdapter = new AlbumAdapter(getContext(), hasCamera, choiceMode, widget.getMediaItemCheckSelector());
        // 点击拍照
        mAdapter.setAddClickListener((view, position) -> getPresenter().clickCamera(view));
        // 点击选择框
        mAdapter.setCheckedClickListener((button, position) -> getPresenter().tryCheckItem(button, position));
        // 点击预览图片
        mAdapter.setItemClickListener((view, position) -> getPresenter().tryPreviewItem(position));
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 显示/隐藏加载动画
     */
    @Override
    public void setLoadingDisplay(boolean display) {
//        mLayoutLoading.setVisibility(display ? View.VISIBLE : View.GONE);
        if (display) {
            mLayoutLoading.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
        } else {
            FunctionsViewKt.fade(mLayoutLoading, 500, true);
            mProgressBar.setIndeterminate(false);
        }
    }

    /**
     * 显示/隐藏右上角完成按钮
     */
    @Override
    public void setCompleteDisplay(boolean display) {
        mCompleteMenu.setVisible(display);
    }

    /**
     * 绑定文件夹数据：刷新列表
     */
    @Override
    public void bindAlbumFolder(AlbumFolder albumFolder) {
        mBtnSwitchFolder.setText(albumFolder.getName());
        mAdapter.setAlbumFiles(albumFolder.getAlbumFiles());
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    /**
     * 插入条目（拍照后添加图片）
     */
    @Override
    public void notifyInsertItem(int position) {
        mAdapter.notifyItemInserted(position);
    }

    /**
     * 刷新单个条目
     */
    @Override
    public void notifyItem(int position) {
        mAdapter.notifyItemChanged(position);
    }

    /**
     * 更新预览按钮上的选中数量 (5)
     */
    @Override
    public void setCheckedCount(int count) {
        mBtnPreview.setText(" (" + count + ")");
    }

    /**
     * 点击事件
     */
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