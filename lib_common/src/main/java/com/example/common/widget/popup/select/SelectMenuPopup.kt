package com.example.common.widget.popup.select

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.BasePopupWindow
import com.example.common.databinding.ViewPopupSelectMenuBinding
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import java.lang.ref.WeakReference

/**
 * Created by wangyanbin
 * 顶部多选弹框
 * 支持左右侧，部分单位可该系
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectMenuPopup<T>(activity: FragmentActivity, var formatter: (T?) -> String?) : BasePopupWindow<ViewPopupSelectMenuBinding>(activity, hasLight = false) {
    private var lastMenuWidth = 0
    private var onCurrent: ((item: String?, index: Int) -> Unit)? = null

    /**
     * 假设弹出的上方的view是绘制好的
     *  mBinding?.llFilter?.doOnceAfterLayout {
     *    coin.setParams(listOf(string(R.string.unitINR), string(R.string.unitIDR)), mBinding?.llFilter?.measuredWidth.orZero)
     *  }
     */
    fun setParams(list: List<T>, menuWidth: Int = 0, horizontalMargin: Int = 15, verticalMargin: Int = 0, gravity: Int = Gravity.END) {
        lastMenuWidth = menuWidth
        setConfiguration(list, menuWidth, horizontalMargin, verticalMargin, gravity)
    }

    fun setParams(list: List<T>, view: WeakReference<View>, gravity: Int = Gravity.END) {
        if (0 == lastMenuWidth) {
            view.get()?.doOnceAfterLayout {
                setParams(list, menuWidth = it.measuredWidth, gravity = gravity)
            }
        } else {
            setParams(list, menuWidth = lastMenuWidth, gravity = gravity)
        }
    }

    private fun setConfiguration(list: List<T>, menuWidth: Int = 0, horizontalMargin: Int = 15, verticalMargin: Int = 0, gravity: Int = Gravity.END) {
        mBinding?.apply {
            // 确定箭头大小/位置
            setupArrow(viewArrow, horizontalMargin, verticalMargin, gravity)
            // 确定容器大小/位置
            setupContainer(llItem, list, menuWidth, horizontalMargin, verticalMargin, gravity)
        }
    }

    private fun setupArrow(arrowView: View, horizontalMargin: Int = 15, verticalMargin: Int = 0, gravity: Int = Gravity.END) {
        arrowView.layoutGravity = gravity
        arrowView.margin(
            start = if (gravity == Gravity.START) 10.pt + horizontalMargin.pt else 0,
            top = verticalMargin.pt,
            end = if (gravity == Gravity.END) 10.pt + horizontalMargin.pt else 0
        )
    }

    private fun setupContainer(container: ViewGroup, list: List<T>, menuWidth: Int = 0, horizontalMargin: Int = 15, verticalMargin: Int = 0, gravity: Int = Gravity.END) {
        container.apply {
            if (menuWidth != 0) size(menuWidth, WRAP_CONTENT)
            layoutGravity = gravity
            margin(
                start = if (gravity == Gravity.START) horizontalMargin.pt else 0,
                top = 10.pt + verticalMargin.pt,
                end = if (gravity == Gravity.END) horizontalMargin.pt else 0
            )
            removeAllViews()
            list.forEachIndexed { index, t ->
                // 获取根布局
                val root = SelectItemHolder(this, formatter(t), index).also {
                    it.onItemClick = { item, clickIndex ->
                        dismiss()
                        onCurrent?.invoke(item, clickIndex)
                    }
                }.mBinding.root
                // 添加布局进外层父布局
                addView(root)
                // 添加完成后设置大小
                root.size(height = 50.pt)
                // 绘制下划线
                if (list.safeSize - 1 > index) {
                    addDivider(this)
                }
            }
        }
    }

    private fun addDivider(container: ViewGroup) {
        val view = View(container.context)
        view.setBackgroundColor(color(R.color.bgLine))
        view.size(MATCH_PARENT, 1.pt)
        view.margin(start = 1.pt, end = 1.pt)
        container.addView(view)
    }

    fun setOnItemClickListener(onCurrent: ((item: String?, index: Int) -> Unit)) {
        this.onCurrent = onCurrent
    }

}