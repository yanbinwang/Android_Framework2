package com.example.common.widget.textview.autofit

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView

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
class AutofitTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr), AutofitHelper.OnTextSizeChangeListener {
    private val mHelper by lazy { AutofitHelper.create(this, attrs, defStyleAttr).addOnTextSizeChangeListener(this) }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        mHelper.setTextSize(unit, size)
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        mHelper.setMaxLines(lines)
    }

    override fun setMaxLines(maxLines: Int) {
        super.setMaxLines(maxLines)
        mHelper.setMaxLines(maxLines)
    }

    override fun onTextSizeChange(textSize: Float, oldTextSize: Float) {}

    fun isSizeToFit() = mHelper.isEnabled()

    fun setSizeToFit(sizeToFit: Boolean = true) = mHelper.setEnabled(sizeToFit)

    fun setMinTextSize(minSize: Int) = mHelper.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, minSize.toFloat())

    fun setMaxTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, size: Float) = mHelper.setMaxTextSize(unit, size)

    fun setMinTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, minSize: Float) = mHelper.setMinTextSize(unit, minSize)

    fun setPrecision(precision: Float) = mHelper.setPrecision(precision)

    fun getMinTextSize() = mHelper.getMinTextSize()

    fun getMaxTextSize() = mHelper.getMaxTextSize()

    fun getPrecision() = mHelper.getPrecision()

    fun getAutofitHelper() = mHelper

}