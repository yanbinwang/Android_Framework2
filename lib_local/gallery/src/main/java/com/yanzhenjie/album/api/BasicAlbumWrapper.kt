package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.Nullable;

import com.yanzhenjie.album.callback.Action;
import com.yanzhenjie.album.model.Widget;

/**
 * 顶级抽象父类
 * 功能：封装 相册/相机 公共能力，高度抽象、高度复用
 * 地位：整个库对外调用的统一入口基类
 * 泛型说明
 * @param Returner   返回自身类型（链式调用 .xxx().xxx()）
 * @param Result     成功回调类型
 * @param Cancel     取消回调类型
 * @param Checked    已选数据类型
 */
public abstract class BasicAlbumWrapper<Returner extends BasicAlbumWrapper, Result, Cancel, Checked> {
    // 成功回调
    protected Action<Result> mResult;
    // 取消回调
    protected Action<Cancel> mCancel;
    // 界面样式（主题、状态栏、导航栏、颜色等）
    protected Widget mWidget;
    // 已选中的文件
    protected Checked mChecked;
    // 上下文
    protected final Context mContext;

    /**
     * 构造方法：初始化默认主题
     */
    public BasicAlbumWrapper(Context context) {
        this.mContext = context;
        this.mWidget = Widget.getDefaultWidget(context);
    }

    /**
     * 设置成功回调
     * 返回 Returner 泛型 → 支持链式调用：.onResult(...).onCancel(...)
     */
    public final Returner onResult(Action<Result> result) {
        this.mResult = result;
        return (Returner) this;
    }

    /**
     * 设置取消回调
     */
    public final Returner onCancel(Action<Cancel> cancel) {
        this.mCancel = cancel;
        return (Returner) this;
    }

    /**
     * 设置自定义主题样式
     */
    public final Returner widget(@Nullable Widget widget) {
        this.mWidget = widget;
        return (Returner) this;
    }

    /**
     * 抽象方法：启动页面（相册/相机 各自实现）
     */
    public abstract void start();

}