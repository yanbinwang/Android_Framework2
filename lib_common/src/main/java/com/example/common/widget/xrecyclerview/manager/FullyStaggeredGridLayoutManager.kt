package com.example.common.widget.xrecyclerview.manager

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.framework.utils.function.value.orZero

/**
 * author: wyb
 * date: 2017/9/5.
 */
class FullyStaggeredGridLayoutManager(spanCount: Int, orientation: Int) : StaggeredGridLayoutManager(spanCount, orientation) {
    // 尺寸的数组，[0]是宽，[1]是高
    private val measuredDimension by lazy { IntArray(2) }

    override fun onMeasure(recycler: Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        // 宽的mode+size
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        // 高的mode + size
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)
        // 自身宽高的初始值
        var width = 0
        var height = 0
        // item的数目
        val count = itemCount
        // item的列数
        val span = spanCount
        // 根据行数或列数来创建数组
        // 用来比较同行/列那个item罪宽/高
        val dimension = IntArray(span)
        for (i in 0 until count) {
            measureScrapChild(recycler, i, View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED), measuredDimension)
            // 如果是竖直的列表，计算item的高，否则计算宽度
            if (orientation == VERTICAL) {
                dimension[findMinIndex(dimension)] += measuredDimension[1]
            } else {
                dimension[findMinIndex(dimension)] += measuredDimension[0]
            }
        }
        if (orientation == VERTICAL) {
            height = findMax(dimension)
        } else {
            width = findMax(dimension)
        }
        when (widthMode) {
            // 当控件宽是match_parent时，宽度就是父控件的宽度
            View.MeasureSpec.EXACTLY -> width = widthSize
            View.MeasureSpec.AT_MOST -> {}
            View.MeasureSpec.UNSPECIFIED -> {}
        }
        when (heightMode) {
            // 当控件高是match_parent时，高度就是父控件的高度
            View.MeasureSpec.EXACTLY -> height = heightSize
            View.MeasureSpec.AT_MOST -> {}
            View.MeasureSpec.UNSPECIFIED -> {}
        }
        //设置测量尺寸
        setMeasuredDimension(width, height)
    }

    private fun measureScrapChild(recycler: Recycler, position: Int, widthSpec: Int, heightSpec: Int, measuredDimension: IntArray) {
        // 挨个遍历所有item
        if (position < itemCount) {
            try {
                // fix 动态添加时报IndexOutOfBoundsException
                val view = recycler.getViewForPosition(position)
                val lp = view.layoutParams as? RecyclerView.LayoutParams
                val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, paddingLeft + paddingRight, lp?.width.orZero)
                val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, paddingTop + paddingBottom, lp?.height.orZero)
                // 子view进行测量，然后可以通过getMeasuredWidth()获得测量的宽，高类似
                view.measure(childWidthSpec, childHeightSpec)
                // 将item的宽高放入数组中
                measuredDimension[0] = view.measuredWidth + lp?.leftMargin.orZero + lp?.rightMargin.orZero
                measuredDimension[1] = view.measuredHeight + lp?.topMargin.orZero + lp?.bottomMargin.orZero
                recycler.recycleView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun findMax(array: IntArray): Int {
        var max = array[0]
        for (value in array) {
            if (value > max) {
                max = value
            }
        }
        return max
    }

    /**
     * 得到最数组中最小元素的下标
     */
    private fun findMinIndex(array: IntArray): Int {
        var index = 0
        var min = array[0]
        for (i in array.indices) {
            if (array[i] < min) {
                min = array[i]
                index = i
            }
        }
        return index
    }

}