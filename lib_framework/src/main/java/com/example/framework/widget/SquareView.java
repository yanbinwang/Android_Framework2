package com.example.framework.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * author: wyb
 * date: 2017/8/29.
 * 嵌套的外层布局，使view的宽高一致
 */
public class SquareView extends RelativeLayout {
    public SquareView(Context context) {
        super(context);
    }

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //重写此方法后默认调用父类的onMeasure方法,分别将宽度测量空间与高度测量空间传入
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
