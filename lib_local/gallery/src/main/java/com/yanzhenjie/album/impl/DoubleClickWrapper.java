package com.yanzhenjie.album.impl;

import android.view.View;

/**
 * 双击事件包装类
 * 作用：将普通点击事件包装成【双击才触发】的逻辑
 * 两次点击间隔 ≤ 500ms 才会回调真正的点击事件
 */
public class DoubleClickWrapper implements View.OnClickListener {
    // 第一次点击的时间戳
    private long mFirstTime;
    // 真正的双击事件监听器
    private final View.OnClickListener mOnClickListener;

    /**
     * 构造方法
     * @param onClickListener 双击成功后要触发的点击事件
     */
    public DoubleClickWrapper(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    /**
     * 点击拦截处理
     * 逻辑：
     * 1) 记录当前点击时间
     * 2) 如果与上次点击间隔 ≤ 500ms → 判定为双击 → 触发回调
     * 3) 更新上次点击时间
     */
    @Override
    public void onClick(View v) {
        long now = System.currentTimeMillis();
        // 两次点击间隔小于等于500毫秒，视为双击
        if (now - mFirstTime <= 500) {
            mOnClickListener.onClick(v);
        }
        // 更新第一次点击时间
        mFirstTime = now;
    }

}