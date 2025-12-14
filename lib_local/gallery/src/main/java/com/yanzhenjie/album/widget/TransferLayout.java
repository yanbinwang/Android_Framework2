package com.yanzhenjie.album.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by YanZhenjie on 2018/4/11.
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

    @Override
    public boolean performClick() {
        if (getChildCount() == 1) {
            return getChildAt(0).performClick();
        }
        return super.performClick();
    }

    @Override
    public boolean callOnClick() {
        if (getChildCount() == 1) {
            return getChildAt(0).performClick();
        }
        return super.performClick();
    }

}