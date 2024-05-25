package com.example.mvvm.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * 仿抖音上下滑动
 */
class VideoSnapManager : LinearLayoutManager, RecyclerView.OnChildAttachStateChangeListener {
    private var mDrift = 0//判断是否上滑还是下滑
    private val pagerSnap: PagerSnapHelper? = null
    private var listener: OnViewPagerListener? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, orientation: Int = 0, reverseLayout: Boolean = false) : super(context, orientation, reverseLayout)

    override fun onAttachedToWindow(view: RecyclerView?) {
        view?.addOnChildAttachStateChangeListener(this)
        super.onAttachedToWindow(view)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        mDrift = dy
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val position = getPosition(view)
        if (mDrift > 0) {
            //向上滑
            listener?.onPageSelected(false, view, position)
        } else {
            //向下滑
            listener?.onPageSelected(true, view, position)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        val position = getPosition(view)
        if (mDrift >= 0) {
            //向上滑
            listener?.onPageRelease(true, view, position)
        } else {
            //向下滑
            listener?.onPageRelease(false, view, position)
        }
    }

    fun setOnViewPagerListener(listener: OnViewPagerListener) {
        this.listener = listener
    }

    interface OnViewPagerListener {
        fun onPageRelease(isNest: Boolean, itemView: View, position: Int)

        fun onPageSelected(isBottom: Boolean, itemView: View, position: Int)
    }
}