package com.example.common.widget.popup.select

import android.view.Gravity
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.common.databinding.ViewPopupSelectMenuBinding
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size

/**
 * Created by wangyanbin
 * 顶部多选弹框
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectMenuPopup<T>(activity: FragmentActivity, var formatter: (T?) -> String?) : BasePopupWindow<ViewPopupSelectMenuBinding>(activity, light = false) {
    private var onCurrent: ((item: String, index: Int) -> Unit)? = null

    fun setParams(list: List<T>, gravity: Int = Gravity.END, itemWidth: Int = 0, margin: Int = 15.pt) {
        mBinding?.apply {
            viewArrow.layoutGravity = gravity
            viewArrow.margin(start = if (gravity == Gravity.START) 10.pt + margin else 0, end = if (gravity == Gravity.END) 10.pt + margin else 0)
            llItem.apply {
                if (0 != itemWidth) size(width = itemWidth)
                layoutGravity = gravity
                margin(start = if (gravity == Gravity.START) margin else 0, end = if (gravity == Gravity.END) margin else 0)
                removeAllViews()
                for (index in 0 until list.size.orZero) {
                    addView(SelectItemHolder(llItem, Pair(formatter(list.safeGet(index)).orEmpty(), index)).let {
                        it.onItemClick = { item, index ->
                            hidden()
                            onCurrent?.invoke(item.orEmpty(), index)
                        }
                        it.mBinding.root
                    })
                }
            }
        }
    }

    fun setOnItemClickListener(onCurrent: ((item: String, index: Int) -> Unit)) {
        this.onCurrent = onCurrent
    }

}