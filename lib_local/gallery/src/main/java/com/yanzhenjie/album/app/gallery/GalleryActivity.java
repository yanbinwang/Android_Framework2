package com.yanzhenjie.album.app.gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.album.callback.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.callback.ItemAction;
import com.yanzhenjie.album.model.Widget;
import com.yanzhenjie.album.app.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;

/**
 * 纯图片路径预览
 * 功能：只预览图片路径（String），不处理 AlbumFile 与 GalleryAlbumActivity 逻辑一致，仅数据类型不同
 */
public class GalleryActivity extends BaseActivity implements Contract.GalleryPresenter {
    // 当前预览位置
    private int mCurrentPosition;
    // 是否可选中
    private boolean mCheckable;
    // 图片路径列表
    private ArrayList<String> mPathList;
    // 记录选中状态（路径 -> 是否选中）
    private Map<String, Boolean> mCheckedMap;
    // 主题样式
    private Widget mWidget;
    // MVP View 层
    private Contract.GalleryView<String> mView;
    // 外部回调监听
    public static ItemAction<String> sClick;
    public static ItemAction<String> sLongClick;
    public static Action<String> sCancel;
    public static Action<ArrayList<String>> sResult;

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取传递参数
        Bundle argument = getIntent().getExtras();
        if (null != argument) {
            mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
            mPathList = argument.getStringArrayList(Album.KEY_INPUT_CHECKED_LIST);
            mCurrentPosition = argument.getInt(Album.KEY_INPUT_CURRENT_POSITION);
            mCheckable = argument.getBoolean(Album.KEY_INPUT_GALLERY_CHECKABLE);
        } else {
            finish();
        }
        setContentView(R.layout.album_activity_gallery);
        // 导航栏
        initImmersionBar(false, false, R.color.albumGalleryPrimary);
        // 初始化 MVP
        mView = new GalleryView<>(this, this);
        mView.setupViews(mWidget, mCheckable);
        // 初始化选中状态：全部默认选中
        mCheckedMap = new HashMap<>();
        for (String path : mPathList) {
            mCheckedMap.put(path, true);
        }
        // 不可选时隐藏底部栏
        if (!mCheckable) {
            mView.setBottomDisplay(false);
        }
        mView.setLayerDisplay(false);
        mView.setDurationDisplay(false);
        // 绑定数据
        mView.bindData(mPathList);
        // 定位到当前位置
        if (mCurrentPosition == 0) {
            onCurrentChanged(mCurrentPosition);
        } else {
            mView.setCurrentItem(mCurrentPosition);
        }
        // 更新完成按钮文字
        setCheckedCount();
        // 返回按钮监听
        setOnBackPressedListener(() -> {
            if (sCancel != null) {
                sCancel.onAction("User canceled.");
            }
            finish();
            return Unit.INSTANCE;
        });
    }

    /**
     * 计算选中数量，更新按钮文字
     */
    private void setCheckedCount() {
        int checkedCount = 0;
        for (Map.Entry<String, Boolean> entry : mCheckedMap.entrySet()) {
            if (entry.getValue()) {
                checkedCount += 1;
            }
        }
        String completeText = getString(R.string.album_menu_finish);
        completeText += "(" + checkedCount + " / " + mPathList.size() + ")";
        mView.setCompleteText(completeText);
    }

    /**
     * 点击图片
     */
    @Override
    public void clickItem(int position) {
        if (sClick != null) {
            sClick.onAction(GalleryActivity.this, mPathList.get(mCurrentPosition));
        }
    }

    /**
     * 长按图片
     */
    @Override
    public void longClickItem(int position) {
        if (sLongClick != null) {
            sLongClick.onAction(GalleryActivity.this, mPathList.get(mCurrentPosition));
        }
    }

    /**
     * 滑动切换图片
     */
    @Override
    public void onCurrentChanged(int position) {
        mCurrentPosition = position;
        if (mCheckable) {
            mView.setChecked(Boolean.TRUE.equals(mCheckedMap.get(mPathList.get(position))));
        }
    }

    /**
     * 切换选中状态
     */
    @Override
    public void onCheckedChanged() {
        String path = mPathList.get(mCurrentPosition);
        mCheckedMap.put(path, Boolean.FALSE.equals(mCheckedMap.get(path)));
        setCheckedCount();
    }

    /**
     * 完成选择，返回结果
     */
    @Override
    public void complete() {
        if (sResult != null) {
            ArrayList<String> checkedList = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : mCheckedMap.entrySet()) {
                if (entry.getValue()) {
                    checkedList.add(entry.getKey());
                }
            }
            sResult.onAction(checkedList);
        }
        finish();
    }

    /**
     * 页面销毁，清空监听防止内存泄漏
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