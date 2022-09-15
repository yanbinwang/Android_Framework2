package com.example.common.widget.xrecyclerview.manager.card

import androidx.recyclerview.widget.RecyclerView

class CardLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        //缓存
        detachAndScrapAttachedViews(recycler)
        //获取所有item(包括不可见的)个数
        val count = itemCount
        for (index in 0 until count) {
            //从缓存中获取view
            val view = recycler.getViewForPosition(index)
            //添加到recyclerView
            addView(view)
            //测量一下view
            measureChild(view, 0, 0)
            //居中摆放，getDecoratedMeasuredWidth方法是获取带分割线的宽度，比直接使用view.getWidth()精确
            val realWidth = getDecoratedMeasuredWidth(view)
            val realHeight = getDecoratedMeasuredHeight(view)
            val widthPadding = ((width - realWidth) / 2f).toInt()
            val heightPadding = ((height - realHeight) / 2f).toInt()
            //摆放child
            layoutDecorated(view, widthPadding, heightPadding, widthPadding + realWidth, heightPadding + realHeight)
            //根据索引，来位移和缩放child
            var level = count - index - 1
            //level范围（SHOW_MAX_COUNT-1）- 0
            // 最下层的不动和最后第二层重叠
            if (level == CardConfig.SHOW_MAX_COUNT - 1) level--
            view.translationX = level * CardConfig.TRANSLATION_X
            view.scaleX = 1 - level * CardConfig.SCALE
            view.scaleY = 1 - level * CardConfig.SCALE
        }
    }

}