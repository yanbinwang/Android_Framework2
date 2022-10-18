package com.example.common.widget.xrecyclerview.manager

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

/**
 * author: wyb
 * date: 2017/9/5.
 */
class FullyLinearLayoutManager : LinearLayoutManager {
    private val mMeasuredDimension = IntArray(2)

    constructor(context: Context?) : super(context)

    constructor(context: Context?, orientation: Int = 0, reverseLayout: Boolean = false) : super(context, orientation, reverseLayout)

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)
        var width = 0
        var height = 0
        for (i in 0 until itemCount) {
            measureScrapChild(recycler, i, View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED), mMeasuredDimension)
            if (orientation == HORIZONTAL) {
                width += mMeasuredDimension[0]
                if (i == 0) height = mMeasuredDimension[1]
            } else {
                height += mMeasuredDimension[1]
                if (i == 0) width = mMeasuredDimension[0]
            }
        }
        when (widthMode) {
            View.MeasureSpec.EXACTLY -> width = widthSize
            View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED -> {}
        }
        when (heightMode) {
            View.MeasureSpec.EXACTLY -> height = heightSize
            View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED -> {}
        }
        setMeasuredDimension(width, height)
    }

    private fun measureScrapChild(recycler: Recycler, position: Int, widthSpec: Int, heightSpec: Int, measuredDimension: IntArray) {
        try {
            val view = recycler.getViewForPosition(0) //fix 动态添加时报IndexOutOfBoundsException
            val p = view.layoutParams as RecyclerView.LayoutParams
            val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, paddingLeft + paddingRight, p.width)
            val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, paddingTop + paddingBottom, p.height)
            view.measure(childWidthSpec, childHeightSpec)
            measuredDimension[0] = view.measuredWidth + p.leftMargin + p.rightMargin
            measuredDimension[1] = view.measuredHeight + p.bottomMargin + p.topMargin
            recycler.recycleView(view)
        } catch (_: Exception) {
        }
    }

}