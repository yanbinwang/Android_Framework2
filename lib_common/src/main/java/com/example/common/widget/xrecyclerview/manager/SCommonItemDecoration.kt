package com.example.common.widget.xrecyclerview.manager

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero

/**
 * 定义一个通用的 RecyclerView 间距装饰类，继承自系统的 ItemDecoration
 * @mPropMap 用于存储「不同 Item 类型」对应的「间距配置」
 */
@SuppressLint("WrongConstant")
class SCommonItemDecoration(private val mPropMap: SparseArray<ItemDecorationProps>?) : ItemDecoration() {

    /**
     * @outRect 输出参数：存储计算后的 Item 四周间距
     * @view 当前要计算间距的 Item 视图
     * @parent Item 所在的父容器（RecyclerView）
     * @state RecyclerView 当前状态（如 Item 总数变化等）
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // 获取当前 Item 在 RecyclerView 中的「真实位置」（注意：不是可见位置，是 Adapter 中的索引）
        val position = parent.getChildAdapterPosition(view)
        // 获取 RecyclerView 绑定的 Adapter（Adapter 存储 Item 数据和类型）
        val adapter = parent.adapter
        // 获取当前 Item 的「类型」（Adapter 中重写 getItemViewType 定义，用于区分不同布局的 Item）
        val itemType = adapter?.getItemViewType(position).orZero
        // 声明变量：存储当前 Item 类型对应的「间距配置」
        val props: ItemDecorationProps?
        // 从 mPropMap 中获取当前 ItemType 对应的间距配置
        if (mPropMap != null) {
            // 根据 ItemType 从稀疏数组中取值
            props = mPropMap[itemType]
        } else {
            // 若 mPropMap 为空，没有配置可使用，直接返回（不设置间距）
            return
        }
        // 若当前 ItemType 没有对应的间距配置，直接返回（不设置间距）
        if (props == null) {
            return
        }
        // 初始化默认参数（默认按「垂直线性布局」处理）
        var spanIndex = 0 // 当前 Item 所在的「列索引」（网格/瀑布流中有效，线性布局为 0）
        var spanSize = 1 // 当前 Item 占据的「列数」（如网格中跨2列的 Item，spanSize=2，默认1列）
        var spanCount = 1 // 布局的「总列数」（网格/瀑布流中有效，线性布局为1）
        var orientation = OrientationHelper.VERTICAL // 布局「方向」（垂直/水平）
        // 若布局管理器是「网格布局（GridLayoutManager）」
        if (parent.layoutManager is GridLayoutManager) {
            val lp = view.layoutParams as? GridLayoutManager.LayoutParams
            spanIndex = lp?.spanIndex.orZero
            spanSize = lp?.spanSize.orZero
            val layoutManager = parent.layoutManager as? GridLayoutManager
            spanCount = layoutManager?.spanCount.orZero
            orientation = layoutManager?.orientation.orZero
            // 若布局管理器是「瀑布流布局（StaggeredGridLayoutManager）」
        } else if (parent.layoutManager is StaggeredGridLayoutManager) {
            val lp = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
            spanIndex = lp?.spanIndex.orZero
            val layoutManager = parent.layoutManager as? StaggeredGridLayoutManager
            spanCount = layoutManager?.spanCount.orZero
            // 瀑布流中：若 Item 占满一行（isFullSpan=true），则 spanSize=总列数，否则为1
            spanSize = if (lp?.isFullSpan.orFalse) spanCount else 1
            orientation = layoutManager?.orientation.orZero
        }
        // 当前 Item 的「前一个 Item 位置」：若 position>0，前一个是 position-1；否则为-1（无前置 Item）
        val prePos = if (position > 0) position - 1 else -1
        // 当前 Item 的「后一个 Item 位置」：若 position < 总Item数-1，后一个是 position+1；否则为-1（无后置 Item）
        val nextPos = if (position < adapter?.itemCount.orZero - 1) position + 1 else -1
        // 当前 Item 的「上一行最后一个 Item 位置」（网格/瀑布流中计算首行用）
        val preRowPos = if (position > spanIndex) position - (1 + spanIndex) else -1
        // 当前 Item 的「下一行第一个 Item 位置」（网格/瀑布流中计算末行用）
        val nextRowPos = if (position < adapter?.itemCount.orZero - (spanCount - spanIndex)) position + (spanCount - spanIndex) else -1
        // 当前 Item 是否是「首行（垂直布局）」或「首列（水平布局）」
        val isFirstRowOrColumn = position == 0 || prePos == -1 || itemType != adapter?.getItemViewType(prePos) || preRowPos == -1 || itemType != adapter.getItemViewType(preRowPos)
        // 当前 Item 是否是「末行（垂直布局）」或「末列（水平布局）」
        val isLastRowOrColumn = position == adapter?.itemCount.orZero - 1 || nextPos == -1 || itemType != adapter?.getItemViewType(nextPos) || nextRowPos == -1 || itemType != adapter.getItemViewType(nextRowPos)
        // 初始化间距变量 -> Item 左上右下侧间距（默认0，即无间距）
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0
        // 布局方向为「水平方向」（Item 从左到右排列，如水平网格/水平线性布局）
        if (orientation == GridLayoutManager.VERTICAL) {
            if (props.hasVerticalEdge.orFalse) {
                left = props.verticalSpace.orZero * (spanCount - spanIndex) / spanCount
                right = props.verticalSpace.orZero * (spanIndex + (spanSize - 1) + 1) / spanCount
            } else {
                left = props.verticalSpace.orZero * spanIndex / spanCount
                right = props.verticalSpace.orZero * (spanCount - (spanIndex + spanSize - 1) - 1) / spanCount
            }
            if (isFirstRowOrColumn) {
                // hasHorizontalEdge：是否在「水平边缘」（上下两侧）加额外间距，是则顶部加间距
                if (props.hasHorizontalEdge.orFalse) top = props.horizontalSpace.orZero
            }
            if (isLastRowOrColumn) {
                // 若是末行且需要水平边缘间距，底部加间距；否则不加（避免末行底部多间距）
                if (props.hasHorizontalEdge.orFalse) bottom = props.horizontalSpace.orZero
            } else {
                bottom = props.horizontalSpace.orZero
            }
            // 布局方向为「水平方向」（Item 从左到右排列，如水平网格/水平线性布局）
        } else {
            if (props.hasHorizontalEdge.orFalse) {
                top = props.horizontalSpace.orZero * (spanCount - spanIndex) / spanCount
                bottom = props.horizontalSpace.orZero * (spanIndex + (spanSize - 1) + 1) / spanCount
            } else {
                top = props.horizontalSpace.orZero * spanIndex / spanCount
                bottom = props.horizontalSpace.orZero * (spanCount - (spanIndex + spanSize - 1) - 1) / spanCount
            }
            if (isFirstRowOrColumn) {
                if (props.hasVerticalEdge.orFalse) left = props.verticalSpace.orZero
            }
            if (isLastRowOrColumn) {
                if (props.hasVerticalEdge.orFalse) right = props.verticalSpace.orZero
            } else {
                right = props.verticalSpace.orZero
            }
        }
        // 将计算好的间距赋值给 outRect，RecyclerView 会根据此间距布局 Item
        outRect[left, top, right] = bottom
    }

    data class ItemDecorationProps(
        var horizontalSpace: Int? = null, // 水平方向间距（垂直布局中是「行间距」，水平布局中是「列间距」）
        var verticalSpace: Int? = null, // 垂直方向间距（垂直布局中是「列间距」，水平布局中是「行间距」）
        var hasHorizontalEdge: Boolean? = null, // 是否在「水平边缘」（上下两侧）加额外间距
        var hasVerticalEdge: Boolean? = null // 是否在「垂直边缘」（左右两侧）加额外间距
    )

}