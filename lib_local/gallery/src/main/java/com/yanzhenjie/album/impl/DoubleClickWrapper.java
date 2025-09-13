package com.yanzhenjie.album.impl;

import android.view.View;

/**
 * Created by YanZhenjie on 2018/4/20.
 */
public class DoubleClickWrapper implements View.OnClickListener {

    private final View.OnClickListener mOnClickListener;
    private long mFirstTime;

    public DoubleClickWrapper(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
        long now = System.currentTimeMillis();
        if (now - mFirstTime <= 500) {
            mOnClickListener.onClick(v);
        }
        mFirstTime = now;
    }

}