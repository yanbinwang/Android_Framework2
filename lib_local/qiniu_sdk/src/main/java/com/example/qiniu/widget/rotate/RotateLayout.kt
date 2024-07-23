package com.example.qiniu.widget.rotate

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat

// A RotateLayout is designed to display a single item and provides the
// capabilities to rotate the item.
open class RotateLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs),
    Rotatable {
    private var mOrientation = 0
    protected var mChild: View? = null

    init {
        // The transparent background here is a workaround of the render issue
        // happened when the view is rotated as the device's orientation
        // changed. The view looks fine in landscape. After rotation, the view
        // is invisible.
        setBackgroundResource(android.R.color.transparent)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mChild = getChildAt(0)
        mChild?.pivotX = 0f
        mChild?.pivotY = 0f
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        when (mOrientation) {
            0, 180 -> mChild?.layout(0, 0, width, height)
            90, 270 -> mChild?.layout(0, 0, height, width)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var w = 0
        var h = 0
        when (mOrientation) {
            0, 180 -> {
                measureChild(mChild, widthSpec, heightSpec)
                w = mChild?.measuredWidth.orZero
                h = mChild?.measuredHeight.orZero
            }
            90, 270 -> {
                measureChild(mChild, heightSpec, widthSpec)
                w = mChild?.measuredHeight.orZero
                h = mChild?.measuredWidth.orZero
            }
        }
        setMeasuredDimension(w, h)
        when (mOrientation) {
            0 -> {
                mChild?.translationX = 0f
                mChild?.translationY = 0f
            }
            90 -> {
                mChild?.translationX = 0f
                mChild?.translationY = h.toSafeFloat()
            }
            180 -> {
                mChild?.translationX = w.toSafeFloat()
                mChild?.translationY = h.toSafeFloat()
            }
            270 -> {
                mChild?.translationX = w.toSafeFloat()
                mChild?.translationY = 0f
            }
        }
        mChild?.rotation = -mOrientation.toSafeFloat()
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    // Rotate the view counter-clockwise
    override fun setOrientation(orientation: Int, animation: Boolean) {
        val ori = orientation % 360
        if (mOrientation == ori) return
        mOrientation = ori
        requestLayout()
    }

}