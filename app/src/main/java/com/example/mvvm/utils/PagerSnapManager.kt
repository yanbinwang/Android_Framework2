package com.example.mvvm.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 仿抖音上下滑动
 */
class PagerSnapManager : LinearLayoutManager, RecyclerView.OnChildAttachStateChangeListener {
    private var mDrift = 0//判断是否上滑还是下滑
    private var onViewPagerListener: OnViewPagerListener? = null
    private val pagerSnapHelper by lazy { PagerSnapHelper() }//吸顶，吸底

    constructor(context: Context?) : super(context)

    constructor(context: Context?, orientation: Int = 0, reverseLayout: Boolean = false) : super(context, orientation,reverseLayout)

    override fun onAttachedToWindow(view: RecyclerView?) {
        view?.addOnChildAttachStateChangeListener(this)
        pagerSnapHelper.attachToRecyclerView(view)
        super.onAttachedToWindow(view)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        mDrift = dy
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    override fun onChildViewAttachedToWindow(view: View) {
        if (mDrift > 0) {
            //向上滑
            if (abs(mDrift) == view.height) onViewPagerListener?.onPageSelected(false, view)
        } else {
            //向下滑
            if (abs(mDrift) == view.height) onViewPagerListener?.onPageSelected(true, view)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        if (mDrift >= 0) {
            //向上滑
            onViewPagerListener?.onPageRelease(true, view)
        } else {
            //向下滑
            onViewPagerListener?.onPageRelease(false, view)
        }
    }

    override fun onScrollStateChanged(state: Int) {
        when (state) {
            //当前显示的item
            RecyclerView.SCROLL_STATE_IDLE -> {
                val snapView = pagerSnapHelper.findSnapView(this) ?: return
                onViewPagerListener?.onPageSelected(false, snapView)
            }
        }
        super.onScrollStateChanged(state)
    }

    fun setOnViewPagerListener(listener: OnViewPagerListener) {
        this.onViewPagerListener = listener
    }

    interface OnViewPagerListener {
        fun onPageRelease(isNest: Boolean, position: View)

        fun onPageSelected(isBottom: Boolean, position: View)
    }
}