package com.yanzhenjie.album.app.album;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.app.gallery.GalleryView;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.model.Widget;
import com.yanzhenjie.album.utils.AlbumUtil;

import java.util.ArrayList;

import kotlin.Unit;

/**
 * 图片/视频 预览页
 * MVP 中的 Presenter
 * 负责：选择控制、数量限制、预览切换、完成返回
 */
public class GalleryActivity extends BaseActivity implements Contract.GalleryPresenter {
    // 功能类型：图片 / 视频 / 全部
    private int mFunction;
    // 最大可选数量
    private int mAllowSelectCount;
    // 主题样式
    private Widget mWidget;
    // MVP View 层
    private Contract.GalleryView<AlbumFile> mView;
    // 静态全局数据（跨页面传递）
    public static int sCheckedCount;                  // 已选数量
    public static int sCurrentPosition;               // 当前预览位置
    public static ArrayList<AlbumFile> sAlbumFiles;   // 预览列表
    public static Callback sCallback;                 // 预览回调

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取参数
        Bundle argument = getIntent().getExtras();
        if (null != argument) {
            mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
            mFunction = argument.getInt(Album.KEY_INPUT_FUNCTION);
            mAllowSelectCount = argument.getInt(Album.KEY_INPUT_LIMIT_COUNT);
        } else {
            finish();
        }
        setContentView(R.layout.album_activity_gallery);
        // 导航栏
        initImmersionBar(false, false, R.color.albumGalleryPrimary);
        // 绑定 MVP
        mView = new GalleryView<>(this, this);
        mView.setupViews(mWidget, true);
        mView.bindData(sAlbumFiles);
        // 定位到当前预览位置
        if (sCurrentPosition == 0) {
            onCurrentChanged(sCurrentPosition);
        } else {
            mView.setCurrentItem(sCurrentPosition);
        }
        // 设置右上角完成按钮文字
        setCheckedCount();
        // 返回逻辑
        setOnBackPressedListener(() -> {
            finish();
            return Unit.INSTANCE;
        });

    }

    /**
     * 更新完成按钮文字：已选 / 最大数量
     */
    private void setCheckedCount() {
        String completeText = getString(R.string.album_menu_finish);
        completeText += "(" + sCheckedCount + " / " + mAllowSelectCount + ")";
        mView.setCompleteText(completeText);
    }

    /**
     * 点击图片（这里空实现，没有使用）
     */
    @Override
    public void clickItem(int position) {
    }

    /**
     * 长按图片（这里空实现，没有使用）
     */
    @Override
    public void longClickItem(int position) {
    }

    /**
     * 滑动切换图片时更新 UI（核心）
     */
    @Override
    public void onCurrentChanged(int position) {
        sCurrentPosition = position;
        AlbumFile albumFile = sAlbumFiles.get(position);
        // 同步勾选状态
        mView.setChecked(albumFile.isChecked());
        // 不可用文件显示遮罩
        mView.setLayerDisplay(albumFile.isDisable());
        // 视频 → 显示时长
        if (albumFile.getMediaType() == AlbumFile.TYPE_VIDEO) {
            mView.setDuration(AlbumUtil.convertDuration(albumFile.getDuration()));
            mView.setDurationDisplay(true);
        } else {
            mView.setDurationDisplay(false);
        }
    }

    /**
     * 点击勾选框：切换选中状态（带数量限制）
     */
    @Override
    public void onCheckedChanged() {
        AlbumFile albumFile = sAlbumFiles.get(sCurrentPosition);
        // 取消选中
        if (albumFile.isChecked()) {
            albumFile.setChecked(false);
            sCallback.onPreviewChanged(albumFile);
            sCheckedCount--;
            // 选中
        } else {
            // 超过最大数量 → 提示
            if (sCheckedCount >= mAllowSelectCount) {
                int messageRes;
                switch (mFunction) {
                    case Album.FUNCTION_CHOICE_IMAGE: {
                        messageRes = R.string.album_check_image_limit;
                        break;
                    }
                    case Album.FUNCTION_CHOICE_VIDEO: {
                        messageRes = R.string.album_check_video_limit;
                        break;
                    }
                    case Album.FUNCTION_CHOICE_ALBUM: {
                        messageRes = R.string.album_check_album_limit;
                        break;
                    }
                    default: {
                        throw new AssertionError("This should not be the case.");
                    }
                }
                mView.toast(getString(messageRes, mAllowSelectCount));
                mView.setChecked(false);
                // 没超数量 → 选中
            } else {
                albumFile.setChecked(true);
                sCallback.onPreviewChanged(albumFile);
                sCheckedCount++;
            }
        }
        // 更新按钮文字
        setCheckedCount();
    }

    /**
     * 点击完成按钮（必须选至少一个）
     */
    @Override
    public void complete() {
        if (sCheckedCount == 0) {
            int messageRes;
            switch (mFunction) {
                case Album.FUNCTION_CHOICE_IMAGE: {
                    messageRes = R.string.album_check_image_little;
                    break;
                }
                case Album.FUNCTION_CHOICE_VIDEO: {
                    messageRes = R.string.album_check_video_little;
                    break;
                }
                case Album.FUNCTION_CHOICE_ALBUM: {
                    messageRes = R.string.album_check_album_little;
                    break;
                }
                default: {
                    throw new AssertionError("This should not be the case.");
                }
            }
            mView.toast(messageRes);
        } else {
            sCallback.onPreviewComplete();
            finish();
        }
    }

    /**
     * 页面销毁：清空静态变量 → 防止内存泄漏
     */
    @Override
    public void finish() {
        sAlbumFiles = null;
        sCheckedCount = 0;
        sCurrentPosition = 0;
        sCallback = null;
        super.finish();
    }

    /**
     * 预览回调接口
     */
    public interface Callback {

        /**
         * 完成选择
         */
        void onPreviewComplete();

        /**
         * 选中/取消
         */
        void onPreviewChanged(AlbumFile albumFile);

    }

}