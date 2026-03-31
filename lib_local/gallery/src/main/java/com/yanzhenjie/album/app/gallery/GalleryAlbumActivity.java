package com.yanzhenjie.album.app.gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.ItemAction;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.utils.AlbumUtil;

import java.util.ArrayList;

import kotlin.Unit;

/**
 * 图片/视频 预览页
 * 对应 MVP 中的 Presenter 层
 * 职责：处理所有预览页的业务逻辑（选中、切换、完成、回调）
 */
public class GalleryAlbumActivity extends BaseActivity implements Contract.GalleryPresenter {
    // 当前预览的图片位置
    private int mCurrentPosition;
    // 是否可以选中（勾选）
    private boolean mCheckable;
    // 要预览的图片/视频列表
    private ArrayList<AlbumFile> mAlbumFiles;
    // MVP 的 View 层（负责UI）
    private Contract.GalleryView<AlbumFile> mView;
    // 外部设置的静态监听：点击、长按、取消、选择结果
    public static ItemAction<AlbumFile> sClick;
    public static ItemAction<AlbumFile> sLongClick;
    public static Action<String> sCancel;
    public static Action<ArrayList<AlbumFile>> sResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_gallery);
        // 绑定 MVP：自己是 Presenter，GalleryView 是 View
        mView = new GalleryView<>(this, this);
        // 获取上一页传过来的数据
        Bundle argument = getIntent().getExtras();
        if (null != argument) {
            mAlbumFiles = argument.getParcelableArrayList(Album.KEY_INPUT_CHECKED_LIST);
            mCurrentPosition = argument.getInt(Album.KEY_INPUT_CURRENT_POSITION);
            mCheckable = argument.getBoolean(Album.KEY_INPUT_GALLERY_CHECKABLE);
            // 初始化 View
            Widget mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
            mView.setupViews(mWidget, mCheckable);
            mView.bindData(mAlbumFiles);
        }
        // 显示当前预览位置
        if (mCurrentPosition == 0) {
            onCurrentChanged(mCurrentPosition);
        } else {
            mView.setCurrentItem(mCurrentPosition);
        }
        // 设置右上角完成按钮的文字（显示选中数量）
        setCheckedCount();
        // 返回按钮逻辑
        setOnBackPressedListener(() -> {
            if (sCancel != null) {
                sCancel.onAction("User canceled.");
            }
            finish();
            return Unit.INSTANCE;
        });
    }

    /**
     * 计算已选中数量，更新完成按钮文字
     */
    private void setCheckedCount() {
        int checkedCount = 0;
        for (AlbumFile albumFile : mAlbumFiles) {
            if (albumFile.isChecked()) {
                checkedCount += 1;
            }
        }
        String completeText = getString(R.string.album_menu_finish);
        completeText += "(" + checkedCount + " / " + mAlbumFiles.size() + ")";
        mView.setCompleteText(completeText);
    }

    /**
     * 点击图片
     */
    @Override
    public void clickItem(int position) {
        if (sClick != null) {
            sClick.onAction(GalleryAlbumActivity.this, mAlbumFiles.get(mCurrentPosition));
        }
    }

    /**
     * 长按图片
     */
    @Override
    public void longClickItem(int position) {
        if (sLongClick != null) {
            sLongClick.onAction(GalleryAlbumActivity.this, mAlbumFiles.get(mCurrentPosition));
        }
    }

    /**
     * 滑动切换图片时回调（核心逻辑）
     */
    @Override
    public void onCurrentChanged(int position) {
        mCurrentPosition = position;
        AlbumFile albumFile = mAlbumFiles.get(position);
        // 同步勾选状态
        if (mCheckable) {
            mView.setChecked(albumFile.isChecked());
        }
        // 如果是不可用的文件，显示遮罩
        mView.setLayerDisplay(albumFile.isDisable());
        // 如果是视频 → 显示时长
        if (albumFile.getMediaType() == AlbumFile.TYPE_VIDEO) {
            if (!mCheckable) {
                mView.setBottomDisplay(true);
            }
            mView.setDuration(AlbumUtil.convertDuration(albumFile.getDuration()));
            mView.setDurationDisplay(true);
            // 图片 → 隐藏视频时长
        } else {
            if (!mCheckable) {
                mView.setBottomDisplay(false);
            }
            mView.setDurationDisplay(false);
        }
    }

    /**
     * 点击勾选框，切换选中状态
     */
    @Override
    public void onCheckedChanged() {
        AlbumFile albumFile = mAlbumFiles.get(mCurrentPosition);
        // 切换状态
        albumFile.setChecked(!albumFile.isChecked());
        // 更新按钮文字
        setCheckedCount();
    }

    /**
     * 点击完成，返回选中结果
     */
    @Override
    public void complete() {
        if (sResult != null) {
            ArrayList<AlbumFile> checkedList = new ArrayList<>();
            for (AlbumFile albumFile : mAlbumFiles) {
                if (albumFile.isChecked()) {
                    checkedList.add(albumFile);
                }
            }
            sResult.onAction(checkedList);
        }
        finish();
    }

    /**
     * 页面销毁，清空所有静态监听（防止内存泄漏）
     */
    @Override
    public void finish() {
        sResult = null;
        sCancel = null;
        sClick = null;
        sLongClick = null;
        super.finish();
    }

}