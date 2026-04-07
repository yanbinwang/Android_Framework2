package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.Nullable;

import com.yanzhenjie.album.callback.Action;

/**
 * 相机功能 顶层抽象父类
 * 地位：和 BasicAlbumWrapper 平级！
 * 专门给：拍照、录像 功能用的
 * 功能：统一相机的 路径、回调、启动
 */
@Deprecated()
public abstract class BasicCameraWrapper<Returner extends BasicCameraWrapper> {
    // 拍照/录像 保存的文件路径
    protected String mFilePath;
    // 上下文
    protected Context mContext;
    // 成功回调（返回文件路径）
    protected Action<String> mResult;
    // 取消回调
    protected Action<String> mCancel;

    public BasicCameraWrapper(Context context) {
        this.mContext = context;
    }

    /**
     * 设置成功回调
     */
    public final Returner onResult(Action<String> result) {
        this.mResult = result;
        return (Returner) this;
    }

    /**
     * 设置取消回调
     */
    public final Returner onCancel(Action<String> cancel) {
        this.mCancel = cancel;
        return (Returner) this;
    }

    /**
     * 设置自定义保存路径
     */
    public Returner filePath(@Nullable String filePath) {
        this.mFilePath = filePath;
        return (Returner) this;
    }

    /**
     * 抽象启动方法
     * 子类：拍照、录像 各自实现
     */
    public abstract void start();

}