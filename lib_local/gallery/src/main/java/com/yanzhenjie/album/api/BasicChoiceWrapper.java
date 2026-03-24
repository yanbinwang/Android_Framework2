package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.Filter;

/**
 * 选择器通用封装
 * 继承自：BasicAlbumWrapper（顶层万能模板）
 * 功能：专门封装 图片/视频/相册选择 的公共配置（相机、网格列数、文件过滤）
 * 所有选择器功能（图片、视频、全部）都继承这个类
 */
public abstract class BasicChoiceWrapper<Returner extends BasicChoiceWrapper, Result, Cancel, Checked> extends BasicAlbumWrapper<Returner, Result, Cancel, Checked> {
    // 列表网格列数，默认 2 列
    protected int mColumnCount = 2;
    // 是否显示拍照按钮，默认开启
    protected boolean mHasCamera = true;
    // 过滤后的文件是否显示（置灰）
    protected boolean mFilterVisibility = true;
    // 文件大小过滤器
    protected Filter<Long> mSizeFilter;
    // 文件类型（MimeType）过滤器
    protected Filter<String> mMimeTypeFilter;

    public BasicChoiceWrapper(Context context) {
        super(context);
    }

    /**
     * 设置是否显示拍照入口
     */
    public Returner camera(boolean hasCamera) {
        this.mHasCamera = hasCamera;
        return (Returner) this;
    }

    /**
     * 设置列表列数（2~4列）
     */
    public Returner columnCount(@IntRange(from = 2, to = 4) int count) {
        this.mColumnCount = count;
        return (Returner) this;
    }

    /**
     * 设置文件大小过滤
     */
    public Returner filterSize(Filter<Long> filter) {
        this.mSizeFilter = filter;
        return (Returner) this;
    }

    /**
     * 设置文件类型过滤
     */
    public Returner filterMimeType(Filter<String> filter) {
        this.mMimeTypeFilter = filter;
        return (Returner) this;
    }

    /**
     * 设置：过滤掉的文件是否显示（只是置灰，不允许选）
     */
    public Returner afterFilterVisibility(boolean visibility) {
        this.mFilterVisibility = visibility;
        return (Returner) this;
    }

}