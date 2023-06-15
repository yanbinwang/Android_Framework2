package com.example.common.widget.popup.select

import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.common.base.PopupAnimType.TRANSLATE
import com.example.common.databinding.ViewPopupSelectLabelBinding
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.click

/**
 * Created by wangyanbin
 * 底部多选弹框
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectLabelPopup<T>(activity: FragmentActivity, var formatter: (T?) -> String?) : BasePopupWindow<ViewPopupSelectLabelBinding>(activity, popupAnimStyle = TRANSLATE) {
    private var onCurrent: ((item: String, index: Int) -> Unit)? = null

    fun setParams(list: List<T>) {
        binding.llItem.apply {
            removeAllViews()
            for (index in 0 until list.size.orZero) {
                addView(SelectItemHolder(binding.llItem, Pair(formatter(list.safeGet(index)).orEmpty(), index)).let {
                        it.onItemClick = { item, index ->
                            hidden()
                            onCurrent?.invoke(item.orEmpty(), index)
                        }
                        it.binding.root
                })
            }
        }
        binding.tvCancel.click { hidden() }
    }

    fun setOnItemClickListener(onCurrent: ((item: String, index: Int) -> Unit)) {
        this.onCurrent = onCurrent
    }

}