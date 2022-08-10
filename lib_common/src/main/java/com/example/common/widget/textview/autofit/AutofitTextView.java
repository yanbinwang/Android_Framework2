package com.example.common.widget.textview.autofit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * 自适应文字大小的textview
 *
 * <***.***.***.AutofitTextView
 *      android:id="@+id/tv_reachedSalesAmount"
 *      android:layout_width="match_parent"
 *      android:layout_height="wrap_content"
 *      android:singleLine="true"
 *      android:text="0"
 *      android:textSize="30sp"
 *      app:sizeToFit="true" />
 */
@SuppressLint("AppCompatCustomView")
public class AutofitTextView extends TextView implements AutofitHelper.OnTextSizeChangeListener {
    private AutofitHelper mHelper;

    public AutofitTextView(Context context) {
        super(context);
        initialize(null, 0);
    }

    public AutofitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs, 0);
    }

    public AutofitTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(attrs, defStyle);
    }

    private void initialize(AttributeSet attrs, int defStyle) {
        mHelper = AutofitHelper.create(this, attrs, defStyle).addOnTextSizeChangeListener(this);
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        if (mHelper != null) {
            mHelper.setTextSize(unit, size);
        }
    }

    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        if (mHelper != null) {
            mHelper.setMaxLines(lines);
        }
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        if (mHelper != null) {
            mHelper.setMaxLines(maxLines);
        }
    }

    @Override
    public void onTextSizeChange(float textSize, float oldTextSize) {
    }

    public boolean isSizeToFit() {
        return mHelper.isEnabled();
    }

    public void setSizeToFit() {
        setSizeToFit(true);
    }

    public void setSizeToFit(boolean sizeToFit) {
        mHelper.setEnabled(sizeToFit);
    }

    public void setMinTextSize(int minSize) {
        mHelper.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, minSize);
    }

    public void setMaxTextSize(float size) {
        mHelper.setMaxTextSize(size);
    }

    public void setMaxTextSize(int unit, float size) {
        mHelper.setMaxTextSize(unit, size);
    }

    public void setMinTextSize(int unit, float minSize) {
        mHelper.setMinTextSize(unit, minSize);
    }

    public void setPrecision(float precision) {
        mHelper.setPrecision(precision);
    }

    public float getMinTextSize() {
        return mHelper.getMinTextSize();
    }

    public float getMaxTextSize() {
        return mHelper.getMaxTextSize();
    }

    public float getPrecision() {
        return mHelper.getPrecision();
    }

    public AutofitHelper getAutofitHelper() {
        return mHelper;
    }

}