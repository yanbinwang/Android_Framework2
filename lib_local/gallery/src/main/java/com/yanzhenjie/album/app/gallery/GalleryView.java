package com.yanzhenjie.album.app.gallery;

import static com.example.gallery.base.BaseActivity.setSupportMenuViewAsync;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.gallery.R;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.utils.SystemBar;

import java.util.List;

/**
 * 图片/视频 预览页面 View 层
 * 对应 MVP 中的 View，负责：UI展示、事件分发
 * 功能：大图预览、选中/取消、视频时长显示、状态栏沉浸
 */
public class GalleryView<Data> extends Contract.GalleryView<Data> implements View.OnClickListener {
    // 右上角完成按钮
    private MenuItem mCompleteMenu;
    // 上下文
    private final Activity mActivity;
    // 标题栏
    private final Toolbar mToolbar;
    // 预览 ViewPager
    private final ViewPager mViewPager;
    // 底部操作栏
    private final RelativeLayout mLayoutBottom;
    // 视频时长文字
    private final TextView mTvDuration;
    // 选择框
    private final CheckBox mCheckBox;
    // 顶层遮罩层（拦截点击事件）
    private final FrameLayout mLayoutLayer;

    /**
     * 构造方法：绑定控件
     */
    public GalleryView(Activity activity, Contract.GalleryPresenter presenter) {
        super(activity, presenter);
        // 绑定所有控件
        this.mActivity = activity;
        this.mToolbar = activity.findViewById(R.id.toolbar);
        this.mViewPager = activity.findViewById(R.id.view_pager);
        this.mLayoutBottom = activity.findViewById(R.id.layout_bottom);
        this.mTvDuration = activity.findViewById(R.id.tv_duration);
        this.mCheckBox = activity.findViewById(R.id.check_box);
        this.mLayoutLayer = activity.findViewById(R.id.layout_layer);
        // 设置选择框点击监听
        this.mCheckBox.setOnClickListener(this);
        // 遮罩层点击（拦截事件，不做处理）
        this.mLayoutLayer.setOnClickListener(this);
    }

    /**
     * 创建菜单（完成按钮）
     */
    @Override
    protected void onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu_gallery, menu);
        mCompleteMenu = menu.findItem(R.id.album_menu_finish);
    }

    /**
     * 菜单点击：完成选择
     */
    @Override
    protected void onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.album_menu_finish) {
            getPresenter().complete();
        }
    }

    /**
     * 初始化页面样式：状态栏、导航栏、选择框样式
     */
    @Override
    public void setupViews(Widget widget, boolean checkable) {
        // 沉浸式状态栏 + 导航栏
        SystemBar.invasionStatusBar(mActivity);
        SystemBar.invasionNavigationBar(mActivity);
        SystemBar.setStatusBarColor(mActivity, Color.TRANSPARENT);
        SystemBar.setNavigationBarColor(mActivity, getColor(R.color.albumSheetBottom));
        // 返回箭头
        setHomeAsUpIndicator(R.mipmap.album_ic_back_white);
        // 等 Toolbar 布局结束右侧强行撑满
        setSupportMenuViewAsync(mToolbar, widget.getStatusBarColor());
        // 如果不可选，隐藏选择按钮和完成按钮
        if (!checkable) {
            mCompleteMenu.setVisible(false);
            mCheckBox.setVisibility(View.GONE);
        } else {
            // 设置选择框样式
            ColorStateList itemSelector = widget.getMediaItemCheckSelector();
            mCheckBox.setBackgroundTintList(itemSelector);
            mCheckBox.setTextColor(itemSelector);
        }
        // 页面滑动监听
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getPresenter().onCurrentChanged(position);
            }
        });
    }

    /**
     * 绑定预览数据
     * 内部类实现 PreviewAdapter，加载图片
     */
    @Override
    public void bindData(List<Data> dataList) {
        PreviewAdapter<Data> adapter = new PreviewAdapter<>(getContext(), dataList) {
            @Override
            protected void loadPreview(ImageView imageView, Data item, int position) {
                // 加载图片：支持 String 路径 或 AlbumFile
                if (item instanceof String) {
                    Album.getAlbumConfig().getAlbumLoader().load(imageView, (String) item);
                } else if (item instanceof AlbumFile) {
                    Album.getAlbumConfig().getAlbumLoader().load(imageView, (AlbumFile) item);
                }
            }
        };
        // 点击 -> 通知 Presenter
        adapter.setItemClickListener(v -> getPresenter().clickItem(mViewPager.getCurrentItem()));
        // 长按 -> 通知 Presenter
        adapter.setItemLongClickListener(v -> getPresenter().longClickItem(mViewPager.getCurrentItem()));
        // 设置预加载数量
        if (adapter.getCount() > 3) {
            mViewPager.setOffscreenPageLimit(3);
        } else if (adapter.getCount() > 2) {
            mViewPager.setOffscreenPageLimit(2);
        }
        mViewPager.setAdapter(adapter);
    }

    /**
     * 切换到指定位置的图片
     */
    @Override
    public void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position);
    }

    /**
     * 显示/隐藏视频时长
     */
    @Override
    public void setDurationDisplay(boolean display) {
        mTvDuration.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置视频时长
     */
    @Override
    public void setDuration(String duration) {
        mTvDuration.setText(duration);
    }

    /**
     * 设置选择框状态
     */
    @Override
    public void setChecked(boolean checked) {
        mCheckBox.setChecked(checked);
    }

    /**
     * 显示/隐藏底部栏
     */
    @Override
    public void setBottomDisplay(boolean display) {
        mLayoutBottom.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示/隐藏遮罩层
     */
    @Override
    public void setLayerDisplay(boolean display) {
        mLayoutLayer.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置完成按钮文字
     */
    @Override
    public void setCompleteText(String text) {
        mCompleteMenu.setTitle(text);
    }

    /**
     * 点击事件：选择框
     */
    @Override
    public void onClick(View v) {
        if (v == mCheckBox) {
            getPresenter().onCheckedChanged();
        } else if (v == mLayoutLayer) {
            // 遮罩层只拦截事件，不做处理
        }
    }

}