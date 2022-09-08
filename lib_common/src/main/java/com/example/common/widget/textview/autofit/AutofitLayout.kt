package com.example.common.widget.textview.autofit

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.example.common.R
import java.util.*

/**
 * 自适应容器
 */
@SuppressLint("CustomViewStyleable")
class AutofitLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var mEnabled = false
    private var mMinTextSize = 0f
    private var mPrecision = 0f
    private var mHelpers = WeakHashMap<View, AutofitHelper>()

    init {
        var sizeToFit = true
        var minTextSize = -1
        var precision = -1f
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AutofitTextView, defStyleAttr, 0)
            sizeToFit = ta.getBoolean(R.styleable.AutofitTextView_sizeToFit, true)
            minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize, minTextSize)
            precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision)
            ta.recycle()
        }
        mEnabled = sizeToFit
        mMinTextSize = minTextSize.toFloat()
        mPrecision = precision
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        val textView = child as TextView
        val helper = AutofitHelper.create(textView).setEnabled(mEnabled)
        if (mPrecision > 0) helper.setPrecision(mPrecision)
        if (mMinTextSize > 0) helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, mMinTextSize)
        mHelpers[textView] = helper
    }

    fun getAutofitHelper(textView: TextView?) = mHelpers[textView]

    fun getAutofitHelper(index: Int) = mHelpers[getChildAt(index)]

}