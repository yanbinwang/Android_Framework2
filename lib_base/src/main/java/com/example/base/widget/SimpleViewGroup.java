package com.example.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 自定义控件继承viewgroup需要清除边距，使用当前类做处理
 */
public abstract class SimpleViewGroup extends ViewGroup {

    public SimpleViewGroup(Context context) {
        super(context);
    }

    public SimpleViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SimpleViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(0, 0, r, b);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (onFinish()) drawView();
    }

    /**
     * 检测布局绘制
     */
    protected boolean onFinish() {
        return getChildCount() <= 0;
    }

    /**
     * 容器在new的时候不会走onFinishInflate方法，需要手动调取
     */
    public abstract void drawView();

}