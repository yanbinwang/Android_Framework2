package com.example.common.widget.textview.autofit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.common.R;

import java.util.WeakHashMap;

/**
 * 自适应容器
 */
@SuppressLint("CustomViewStyleable")
public class AutofitLayout extends FrameLayout {
    private boolean mEnabled;
    private float mMinTextSize, mPrecision;
    private final WeakHashMap<View, AutofitHelper> mHelpers = new WeakHashMap<>();

    public AutofitLayout(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public AutofitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public AutofitLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs, defStyle);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyle) {
        boolean sizeToFit = true;
        int minTextSize = -1;
        float precision = -1;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutofitTextView, defStyle, 0);
            sizeToFit = ta.getBoolean(R.styleable.AutofitTextView_sizeToFit, true);
            minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize, minTextSize);
            precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision);
            ta.recycle();
        }
        mEnabled = sizeToFit;
        mMinTextSize = minTextSize;
        mPrecision = precision;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        TextView textView = (TextView) child;
        AutofitHelper helper = AutofitHelper.create(textView).setEnabled(mEnabled);
        if (mPrecision > 0) {
            helper.setPrecision(mPrecision);
        }
        if (mMinTextSize > 0) {
            helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, mMinTextSize);
        }
        mHelpers.put(textView, helper);
    }

    public AutofitHelper getAutofitHelper(TextView textView) {
        return mHelpers.get(textView);
    }

    public AutofitHelper getAutofitHelper(int index) {
        return mHelpers.get(getChildAt(index));
    }

}