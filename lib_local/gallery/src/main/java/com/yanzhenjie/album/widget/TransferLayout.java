package com.yanzhenjie.album.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 事件传递布局（FrameLayout 包装类）
 * 作用：把自身的点击事件 转发 给唯一的子View
 * 专门用于相册里的条目点击、预览点击等场景
 */
public class TransferLayout extends FrameLayout {

    public TransferLayout(@NonNull Context context) {
        super(context);
    }

    public TransferLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TransferLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 执行点击：如果只有1个子View，就让子View执行点击
     */
    @Override
    public boolean performClick() {
        if (getChildCount() == 1) {
            return getChildAt(0).performClick();
        }
        return super.performClick();
    }

    /**
     * 调用点击：同上，转发给唯一子View
     */
    @Override
    public boolean callOnClick() {
        if (getChildCount() == 1) {
            return getChildAt(0).performClick();
        }
        return super.performClick();
    }

}