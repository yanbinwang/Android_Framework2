package com.example.common.widget.xrecyclerview.refresh.callback

/**
 * @description
 * @author
 */
enum class SwipeRefreshLayoutDirection(var value: Int) {
    TOP(0),//只有下拉刷新
    BOTTOM(1),//只有加载更多
    BOTH(2);//全都有

    companion object {
        @JvmStatic
        fun getFromInt(value: Int): SwipeRefreshLayoutDirection {
            for (direction in values()) {
                if (direction.value == value) {
                    return direction
                }
            }
            return BOTH
        }
    }

}