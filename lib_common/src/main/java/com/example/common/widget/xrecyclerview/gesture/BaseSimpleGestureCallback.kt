package com.example.common.widget.xrecyclerview.gesture

import androidx.recyclerview.widget.RecyclerView

abstract class BaseSimpleGestureCallback(private var mDefaultSwipeDirs: Int, private var mDefaultDragDirs: Int) : BaseGestureCallback() {

    fun setDefaultSwipeDirs(defaultSwipeDirs: Int) {
        mDefaultSwipeDirs = defaultSwipeDirs
    }

    fun setDefaultDragDirs(defaultDragDirs: Int) {
        mDefaultDragDirs = defaultDragDirs
    }

    fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return mDefaultSwipeDirs
    }

    fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return mDefaultDragDirs
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(getDragDirs(recyclerView, viewHolder), getSwipeDirs(recyclerView, viewHolder))
    }

}