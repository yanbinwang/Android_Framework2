package com.example.common.widget.textview.autofit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.SingleLineTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.example.base.utils.LogUtil;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.example.common.R;

import java.util.ArrayList;

/**
 * 自适应帮助类
 */
@SuppressLint("ObsoleteSdkInt")
public class AutofitHelper {
    private int mMaxLines;
    private float mTextSize;
    private float mPrecision;
    private float mMinTextSize;
    private float mMaxTextSize;
    private boolean mEnabled;
    private boolean mIsAutofitting;
    private ArrayList<OnTextSizeChangeListener> mListeners;

    private final TextView mTextView;
    private final TextPaint mPaint;
    private final TextWatcher mTextWatcher = new AutofitTextWatcher();
    private final View.OnLayoutChangeListener mOnLayoutChangeListener = new AutofitOnLayoutChangeListener();

    private static final boolean SPEW = false;
    private static final int DEFAULT_MIN_TEXT_SIZE = 8;
    private static final float DEFAULT_PRECISION = 0.5f;
    private static final String TAG = "AutoFitTextHelper";

    public static AutofitHelper create(TextView view) {
        return create(view, null, 0);
    }

    public static AutofitHelper create(TextView view, AttributeSet attrs) {
        return create(view, attrs, 0);
    }

    public static AutofitHelper create(TextView view, AttributeSet attrs, int defStyle) {
        AutofitHelper helper = new AutofitHelper(view);
        boolean sizeToFit = true;
        if (attrs != null) {
            Context context = view.getContext();
            int minTextSize = (int) helper.getMinTextSize();
            float precision = helper.getPrecision();
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutofitTextView, defStyle, 0);
            sizeToFit = ta.getBoolean(R.styleable.AutofitTextView_sizeToFit, true);
            minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize, minTextSize);
            precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision);
            ta.recycle();
            helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, minTextSize).setPrecision(precision);
        }
        helper.setEnabled(sizeToFit);
        return helper;
    }

    private static void autofit(TextView view, TextPaint paint, float minTextSize, float maxTextSize, int maxLines, float precision) {
        if (maxLines <= 0 || maxLines == Integer.MAX_VALUE) {
            return;
        }
        int targetWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        if (targetWidth <= 0) {
            return;
        }

        CharSequence text = view.getText();
        TransformationMethod method = view.getTransformationMethod();
        if (method != null) {
            text = method.getTransformation(text, view);
        }
        Context context = view.getContext();
        Resources r = Resources.getSystem();
        DisplayMetrics displayMetrics;
        float size = maxTextSize;
        float high = size;
        float low = 0;
        if (context != null) {
            r = context.getResources();
        }
        displayMetrics = r.getDisplayMetrics();
        paint.set(view.getPaint());
        paint.setTextSize(size);

        if ((maxLines == 1 && paint.measureText(text, 0, text.length()) > targetWidth) || getLineCount(text, paint, size, targetWidth, displayMetrics) > maxLines) {
            size = getAutofitTextSize(text, paint, targetWidth, maxLines, low, high, precision, displayMetrics);
        }
        if (size < minTextSize) {
            size = minTextSize;
        }
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    private static float getAutofitTextSize(CharSequence text, TextPaint paint, float targetWidth, int maxLines, float low, float high, float precision, DisplayMetrics displayMetrics) {
        float mid = (low + high) / 2.0f;
        int lineCount = 1;
        StaticLayout layout = null;
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, displayMetrics));
        if (maxLines != 1) {
            layout = new StaticLayout(text, paint, (int) targetWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
            lineCount = layout.getLineCount();
        }
        if (SPEW) LogUtil.d(TAG, "low=" + low + " high=" + high + " mid=" + mid + " target=" + targetWidth + " maxLines=" + maxLines + " lineCount=" + lineCount);
        if (lineCount > maxLines) {
            if ((high - low) < precision) {
                return low;
            }
            return getAutofitTextSize(text, paint, targetWidth, maxLines, low, mid, precision, displayMetrics);
        } else if (lineCount < maxLines) {
            return getAutofitTextSize(text, paint, targetWidth, maxLines, mid, high, precision, displayMetrics);
        } else {
            float maxLineWidth = 0;
            if (maxLines == 1) {
                maxLineWidth = paint.measureText(text, 0, text.length());
            } else {
                for (int i = 0; i < lineCount; i++) {
                    if (layout.getLineWidth(i) > maxLineWidth) {
                        maxLineWidth = layout.getLineWidth(i);
                    }
                }
            }
            if ((high - low) < precision) {
                return low;
            } else if (maxLineWidth > targetWidth) {
                return getAutofitTextSize(text, paint, targetWidth, maxLines, low, mid, precision, displayMetrics);
            } else if (maxLineWidth < targetWidth) {
                return getAutofitTextSize(text, paint, targetWidth, maxLines, mid, high, precision, displayMetrics);
            } else {
                return mid;
            }
        }
    }

    private static int getLineCount(CharSequence text, TextPaint paint, float size, float width, DisplayMetrics displayMetrics) {
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, displayMetrics));
        StaticLayout layout = new StaticLayout(text, paint, (int) width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        return layout.getLineCount();
    }

    private static int getMaxLines(TextView view) {
        int maxLines = -1;
        TransformationMethod method = view.getTransformationMethod();
        if (method instanceof SingleLineTransformationMethod) {
            maxLines = 1;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            maxLines = view.getMaxLines();
        }
        return maxLines;
    }

    private AutofitHelper(TextView view) {
        final Context context = view.getContext();
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        mTextView = view;
        mPaint = new TextPaint();
        setRawTextSize(view.getTextSize());
        mMaxLines = getMaxLines(view);
        mMinTextSize = scaledDensity * DEFAULT_MIN_TEXT_SIZE;
        mMaxTextSize = mTextSize;
        mPrecision = DEFAULT_PRECISION;
    }

    public AutofitHelper addOnTextSizeChangeListener(OnTextSizeChangeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
        return this;
    }

    public AutofitHelper removeOnTextSizeChangeListener(OnTextSizeChangeListener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
        return this;
    }

    public float getPrecision() {
        return mPrecision;
    }

    public AutofitHelper setPrecision(float precision) {
        if (mPrecision != precision) {
            mPrecision = precision;
            autofit();
        }
        return this;
    }

    public float getMinTextSize() {
        return mMinTextSize;
    }

    public AutofitHelper setMinTextSize(float size) {
        return setMinTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public AutofitHelper setMinTextSize(int unit, float size) {
        Context context = mTextView.getContext();
        Resources r = Resources.getSystem();
        if (context != null) {
            r = context.getResources();
        }
        setRawMinTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
        return this;
    }

    private void setRawMinTextSize(float size) {
        if (size != mMinTextSize) {
            mMinTextSize = size;
            autofit();
        }
    }

    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    public AutofitHelper setMaxTextSize(float size) {
        return setMaxTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public AutofitHelper setMaxTextSize(int unit, float size) {
        Context context = mTextView.getContext();
        Resources r = Resources.getSystem();
        if (context != null) {
            r = context.getResources();
        }
        setRawMaxTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
        return this;
    }

    private void setRawMaxTextSize(float size) {
        if (size != mMaxTextSize) {
            mMaxTextSize = size;
            autofit();
        }
    }

    public int getMaxLines() {
        return mMaxLines;
    }

    public AutofitHelper setMaxLines(int lines) {
        if (mMaxLines != lines) {
            mMaxLines = lines;
            autofit();
        }
        return this;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public AutofitHelper setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            if (enabled) {
                mTextView.addTextChangedListener(mTextWatcher);
                mTextView.addOnLayoutChangeListener(mOnLayoutChangeListener);
                autofit();
            } else {
                mTextView.removeTextChangedListener(mTextWatcher);
                mTextView.removeOnLayoutChangeListener(mOnLayoutChangeListener);
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            }
        }
        return this;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        if (mIsAutofitting) {
            return;
        }
        Context context = mTextView.getContext();
        Resources r = Resources.getSystem();
        if (context != null) {
            r = context.getResources();
        }
        setRawTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
    }

    private void setRawTextSize(float size) {
        if (mTextSize != size) {
            mTextSize = size;
        }
    }

    private void autofit() {
        float oldTextSize = mTextView.getTextSize();
        float textSize;
        mIsAutofitting = true;
        autofit(mTextView, mPaint, mMinTextSize, mMaxTextSize, mMaxLines, mPrecision);
        mIsAutofitting = false;
        textSize = mTextView.getTextSize();
        if (textSize != oldTextSize) {
            sendTextSizeChange(textSize, oldTextSize);
        }
    }

    private void sendTextSizeChange(float textSize, float oldTextSize) {
        if (mListeners == null) {
            return;
        }

        for (OnTextSizeChangeListener listener : mListeners) {
            listener.onTextSizeChange(textSize, oldTextSize);
        }
    }

    private class AutofitTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            autofit();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private class AutofitOnLayoutChangeListener implements View.OnLayoutChangeListener {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            autofit();
        }
    }

    public interface OnTextSizeChangeListener {

        void onTextSizeChange(float textSize, float oldTextSize);

    }

}