package com.example.common.widget.xrecyclerview.refresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.common.R;
import com.lcodecore.tkrefreshlayout.IBottomView;

/**
 * Created by WangYanBin on 2020/9/17.
 * 自定义刷新-底部
 */
public class RefreshBottomView extends ViewGroup implements IBottomView {
    private ImageView ivLoading;

    public RefreshBottomView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            v.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            v.layout(0, 0, r, b);
        }
    }

    @Override
    public View getView() {
        View rootView = View.inflate(getContext(), R.layout.view_refresh_bottom, null);
        ivLoading = rootView.findViewById(R.id.iv_loading);
        return rootView;
    }

    @Override
    public void onPullingUp(float fraction, float maxBottomHeight, float bottomHeight) {
        ivLoading.setVisibility(VISIBLE);
    }

    @Override
    public void onPullReleasing(float fraction, float maxBottomHeight, float bottomHeight) {
        ivLoading.setVisibility(GONE);
    }

    @Override
    public void startAnim(float maxBottomHeight, float bottomHeight) {
        ivLoading.setVisibility(VISIBLE);
        ((AnimationDrawable) ivLoading.getDrawable()).start();
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void reset() {
        ivLoading.setVisibility(GONE);
    }

}