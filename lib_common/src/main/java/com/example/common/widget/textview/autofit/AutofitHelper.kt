package com.example.common.widget.textview.autofit

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.text.*
import android.text.method.SingleLineTransformationMethod
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.example.base.utils.LogUtil
import com.example.common.R

/**
 * 自适应帮助类
 */
@SuppressLint("ObsoleteSdkInt", "StaticFieldLeak")
object AutofitHelper {
    private var mEnabled = false
    private var mMaxLines = 0
    private var mTextSize = 0f
    private var mMinTextSize = 0f
    private var mMaxTextSize = 0f
    private var mPrecision = 0f
    private var mIsAutofitting = false
    private var mTextView: TextView? = null
    private var mPaint: TextPaint? = null
    private var mListeners: ArrayList<OnTextSizeChangeListener>? = null
    private val SPEW = false
    private val mTextWatcher by lazy { AutofitTextWatcher() }
    private val mOnLayoutChangeListener by lazy { AutofitOnLayoutChangeListener() }
    private val DEFAULT_MIN_TEXT_SIZE = 8
    private val DEFAULT_PRECISION = 0.5f
    private val TAG = "AutoFitTextHelper"

    fun create(view: TextView): AutofitHelper {
        return create(view, null, 0)
    }

    fun create(view: TextView, attrs: AttributeSet?): AutofitHelper {
        return create(view, attrs, 0)
    }

    fun create(view: TextView, attrs: AttributeSet?, defStyle: Int): AutofitHelper {
        val context = view.context
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        mTextView = view
        mPaint = TextPaint()
        setRawTextSize(view.textSize)
        mMaxLines = getMaxLines(view)
        mMinTextSize = scaledDensity * DEFAULT_MIN_TEXT_SIZE
        mMaxTextSize = mTextSize
        mPrecision = DEFAULT_PRECISION
        var sizeToFit = true
        if (attrs != null) {
            var minTextSize: Int = getMinTextSize().toInt()
            var precision: Float = getPrecision()
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AutofitTextView, defStyle, 0)
            sizeToFit = ta.getBoolean(R.styleable.AutofitTextView_sizeToFit, true)
            minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize, minTextSize)
            precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision)
            ta.recycle()
            setMinTextSize(TypedValue.COMPLEX_UNIT_PX, minTextSize.toFloat()).setPrecision(precision)
        }
        setEnabled(sizeToFit)
        return this
    }

    fun getPrecision() = mPrecision

    fun setPrecision(precision: Float): AutofitHelper {
        if (mPrecision != precision) {
            mPrecision = precision
            autofit()
        }
        return this
    }

    fun getMinTextSize() = mMinTextSize

    fun setMinTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, size: Float): AutofitHelper {
        val context = mTextView!!.context
        var r = Resources.getSystem()
        if (context != null) r = context.resources
        setRawMinTextSize(TypedValue.applyDimension(unit, size, r.displayMetrics))
        return this
    }

    fun getMaxTextSize() = mMaxTextSize

    fun setMaxTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, size: Float): AutofitHelper {
        val context = mTextView?.context
        var r = Resources.getSystem()
        if (context != null) r = context.resources
        setRawMaxTextSize(TypedValue.applyDimension(unit, size, r.displayMetrics))
        return this
    }

    fun getMaxLines() = mMaxLines

    fun setMaxLines(lines: Int): AutofitHelper {
        if (mMaxLines != lines) {
            mMaxLines = lines
            autofit()
        }
        return this
    }

    fun isEnabled() = mEnabled

    fun setEnabled(enabled: Boolean): AutofitHelper {
        if (mEnabled != enabled) {
            mEnabled = enabled
            if (enabled) {
                mTextView?.addTextChangedListener(mTextWatcher)
                mTextView?.addOnLayoutChangeListener(mOnLayoutChangeListener)
                autofit()
            } else {
                mTextView?.removeTextChangedListener(mTextWatcher)
                mTextView?.removeOnLayoutChangeListener(mOnLayoutChangeListener)
                mTextView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
            }
        }
        return this
    }

    fun getTextSize() = mTextSize

    fun setTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, size: Float) {
        if (mIsAutofitting) return
        val context = mTextView?.context
        var r = Resources.getSystem()
        if (context != null) r = context.resources
        setRawTextSize(TypedValue.applyDimension(unit, size, r.displayMetrics))
    }

    fun autofit() {
        val oldTextSize = mTextView!!.textSize
        mIsAutofitting = true
        autofit(mTextView!!, mPaint!!, mMinTextSize, mMaxTextSize, mMaxLines, mPrecision)
        mIsAutofitting = false
        val textSize: Float = mTextView!!.textSize
        if (textSize != oldTextSize) sendTextSizeChange(textSize, oldTextSize)
    }

    private fun sendTextSizeChange(textSize: Float, oldTextSize: Float) {
        if (mListeners == null) return
        for (listener in mListeners!!) {
            listener.onTextSizeChange(textSize, oldTextSize)
        }
    }

    private fun autofit(view: TextView, paint: TextPaint, minTextSize: Float, maxTextSize: Float, maxLines: Int, precision: Float) {
        if (maxLines <= 0 || maxLines == Int.MAX_VALUE) return
        val targetWidth = view.width - view.paddingLeft - view.paddingRight
        if (targetWidth <= 0) return
        var text = view.text
        val method = view.transformationMethod
        if (method != null) text = method.getTransformation(text, view)
        val context = view.context
        var r = Resources.getSystem()
        var size = maxTextSize
        val high = size
        val low = 0f
        if (context != null) r = context.resources
        val displayMetrics: DisplayMetrics = r.displayMetrics
        paint.set(view.paint)
        paint.textSize = size
        if (maxLines == 1 && paint.measureText(text, 0, text.length) > targetWidth || getLineCount(text, paint, size, targetWidth.toFloat(), displayMetrics) > maxLines) {
            size = getAutofitTextSize(text, paint, targetWidth.toFloat(), maxLines, low, high, precision, displayMetrics)
        }
        if (size < minTextSize) {
            size = minTextSize
        }
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    private fun getLineCount(text: CharSequence, paint: TextPaint, size: Float, width: Float, displayMetrics: DisplayMetrics): Int {
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, displayMetrics)
        val layout = StaticLayout(text, paint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true)
        return layout.lineCount
    }

    private fun getAutofitTextSize(text: CharSequence, paint: TextPaint, targetWidth: Float, maxLines: Int, low: Float, high: Float, precision: Float, displayMetrics: DisplayMetrics): Float {
        val mid = (low + high) / 2.0f
        var lineCount = 1
        var layout: StaticLayout? = null
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, displayMetrics)
        if (maxLines != 1) {
            layout = StaticLayout(text, paint, targetWidth.toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true)
            lineCount = layout.lineCount
        }
        if (SPEW) LogUtil.d(TAG, "low=$low high=$high mid=$mid target=$targetWidth maxLines=$maxLines lineCount=$lineCount")
        return if (lineCount > maxLines) {
            if (high - low < precision) low else getAutofitTextSize(text, paint, targetWidth, maxLines, low, mid, precision, displayMetrics)
        } else if (lineCount < maxLines) {
            getAutofitTextSize(text, paint, targetWidth, maxLines, mid, high, precision, displayMetrics)
        } else {
            var maxLineWidth = 0f
            if (maxLines == 1) {
                maxLineWidth = paint.measureText(text, 0, text.length)
            } else {
                for (i in 0 until lineCount) {
                    if (layout!!.getLineWidth(i) > maxLineWidth) {
                        maxLineWidth = layout.getLineWidth(i)
                    }
                }
            }
            if (high - low < precision) {
                low
            } else if (maxLineWidth > targetWidth) {
                getAutofitTextSize(text, paint, targetWidth, maxLines, low, mid, precision, displayMetrics)
            } else if (maxLineWidth < targetWidth) {
                getAutofitTextSize(text, paint, targetWidth, maxLines, mid, high, precision, displayMetrics)
            } else {
                mid
            }
        }
    }

    private fun getMaxLines(view: TextView): Int {
        var maxLines = -1
        val method = view.transformationMethod
        if (method is SingleLineTransformationMethod) {
            maxLines = 1
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            maxLines = view.maxLines
        }
        return maxLines
    }

    private fun setRawMinTextSize(size: Float) {
        if (size != mMinTextSize) {
            mMinTextSize = size
            autofit()
        }
    }

    private fun setRawMaxTextSize(size: Float) {
        if (size != mMaxTextSize) {
            mMaxTextSize = size
            autofit()
        }
    }

    private fun setRawTextSize(size: Float) {
        if (mTextSize != size) mTextSize = size
    }

    fun addOnTextSizeChangeListener(listener: OnTextSizeChangeListener): AutofitHelper {
        if (mListeners == null) mListeners = ArrayList()
        mListeners?.add(listener)
        return this
    }

    fun removeOnTextSizeChangeListener(listener: OnTextSizeChangeListener): AutofitHelper {
        mListeners?.remove(listener)
        return this
    }

    private class AutofitOnLayoutChangeListener : View.OnLayoutChangeListener {
        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            autofit()
        }
    }

    private class AutofitTextWatcher : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            autofit()
        }

        override fun afterTextChanged(editable: Editable) {}
    }

    interface OnTextSizeChangeListener {
        fun onTextSizeChange(textSize: Float, oldTextSize: Float)
    }

}