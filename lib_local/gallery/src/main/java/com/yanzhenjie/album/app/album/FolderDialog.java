package com.yanzhenjie.album.app.album;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.utils.ScreenUtilKt;
import com.example.gallery.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.gyf.immersionbar.ImmersionBar;
import com.yanzhenjie.album.model.AlbumFolder;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.callback.OnItemClickListener;

import java.util.List;

import kotlin.Unit;

/**
 * 文件夹选择弹窗（从底部弹出）
 * 功能：点击相册顶部文件夹名称 → 弹出此对话框切换文件夹
 */
public class FolderDialog extends BottomSheetDialog {
    // 当前选中的文件夹位置
    private int mCurrentPosition = 0;
    // 文件夹列表数据
    private final List<AlbumFolder> mAlbumFolders;
    // 条目点击回调
    private final OnItemClickListener mItemClickListener;

    /**
     * 构造方法：初始化弹窗、列表、适配器
     */
    public FolderDialog(Context context, Widget widget, List<AlbumFolder> albumFolders, OnItemClickListener itemClickListener) {
        super(context, R.style.Album_Dialog_Folder);
        // 加载布局
        setContentView(R.layout.album_dialog_floder);
        this.mAlbumFolders = albumFolders;
        this.mItemClickListener = itemClickListener;
        // 初始化RecyclerView
        RecyclerView recyclerView = getDelegate().findViewById(R.id.rv_content_list);
        if (null != recyclerView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            // 创建适配器
            FolderAdapter mFolderAdapter = new FolderAdapter(context, mAlbumFolders, widget.getBucketItemCheckSelector());
            // 条目点击事件
            mFolderAdapter.setItemClickListener((view, position) -> {
                // 如果点击的不是当前选中项
                if (mCurrentPosition != position) {
                    // 取消上一个选中状态
                    mAlbumFolders.get(mCurrentPosition).setChecked(false);
                    mFolderAdapter.notifyItemChanged(mCurrentPosition);
                    // 记录新位置并设置选中
                    mCurrentPosition = position;
                    mAlbumFolders.get(mCurrentPosition).setChecked(true);
                    mFolderAdapter.notifyItemChanged(mCurrentPosition);
                    // 回调外部
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(view, position);
                    }
                }
                dismiss();
            });
            recyclerView.setAdapter(mFolderAdapter);
        }
    }

    /**
     * 创建弹窗：设置宽高
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null) {
            // 获取屏幕宽高
            Display display = window.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            // 宽度取屏幕最小值，高度铺满
            int minSize = Math.min(metrics.widthPixels, metrics.heightPixels);
            window.setLayout(minSize, -1);
            // 导航栏控件
            ScreenUtilKt.setStatusBarLightMode(getWindow(), false, false);
            ScreenUtilKt.setNavigationBarLightMode(getWindow(), true, false);
            ScreenUtilKt.setNavigationBarDrawable(getWindow(), R.color.albumPage, windowInsetsCompat -> Unit.INSTANCE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                Activity activity = getOwnerActivity();
                if (null != activity) {
                    ImmersionBar.with(activity)
                            .reset()
                            .statusBarDarkFont(false, 0.2f)
                            .navigationBarDarkIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O, 0.2f)
                            .init();
                }
            }
        }
    }

}