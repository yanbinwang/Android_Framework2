package com.example.common.widget.xrecyclerview.manager.card

import android.annotation.SuppressLint
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.base.utils.LogUtil
import com.example.common.base.binding.BaseAdapter
import kotlin.math.hypot

@SuppressLint("NotifyDataSetChanged")
class ItemTouchHelperCallback<T : BaseAdapter<*>>(var mAdapter: T) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN)
//        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)//只允許左右
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val index = viewHolder.absoluteAdapterPosition
        LogUtil.e("wyb", "index:${index},direction:${direction}")
        mAdapter.data.removeAt(index)
        mAdapter.notifyItemRemoved(index)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //计算移动距离
        val distance = hypot(dX.toDouble(), dY.toDouble()).toFloat()
        val maxDistance = recyclerView.width / 2f
        //比例
        var fraction = distance / maxDistance
        if (fraction > 1) fraction = 1f
        //为每个child执行动画
        val count = recyclerView.childCount
        for (i in 0 until count) {
            //获取的view从下层到上层
            val view = recyclerView.getChildAt(i)
            val level= CardConfig.SHOW_MAX_COUNT - i - 1
            //level范围（CardConfig.SHOW_MAX_COUNT-1）-0，每个child最大只移动一个CardConfig.TRANSLATION_Y和放大CardConfig.SCALE
            if (level == CardConfig.SHOW_MAX_COUNT - 1) { // 最下层的不动和最后第二层重叠
                view.translationX = CardConfig.TRANSLATION_X * (level - 1)
                view.scaleX = 1 - CardConfig.SCALE * (level - 1)
                view.scaleY = 1 - CardConfig.SCALE * (level - 1)
            } else if (level > 0) {
                view.translationX = level * CardConfig.TRANSLATION_X - fraction * CardConfig.TRANSLATION_X
                view.scaleX = 1 - level * CardConfig.SCALE + fraction * CardConfig.SCALE
                view.scaleY = 1 - level * CardConfig.SCALE + fraction * CardConfig.SCALE
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.3f
    }

}