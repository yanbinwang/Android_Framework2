package com.example.common.widget.popup.select

import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.common.base.PopupAnimType.TRANSLATE
import com.example.common.databinding.ViewPopupSelectLabelBinding
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.margin

/**
 * Created by wangyanbin
 * 底部多选弹框
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectLabelPopup<T>(activity: FragmentActivity, var formatter: (T?) -> String?) : BasePopupWindow<ViewPopupSelectLabelBinding>(activity, popupAnimStyle = TRANSLATE) {
    private var onCurrent: ((item: String?, index: Int) -> Unit)? = null

    fun setParams(list: List<T>) {
        mBinding?.apply {
            llItem.apply {
                removeAllViews()
                list.forEachIndexed { index, t ->
                    // 获取根布局
                    val root = SelectItemHolder(llItem, formatter(t), index).also {
                        it.onItemClick = { item, index ->
                            hidden()
                            onCurrent?.invoke(item, index)
                        }
                    }.mBinding.root
                    // 添加布局进外层父布局
                    addView(root)
                    // 判断是否需要添加下划线
                    if (list.safeSize - 1 > index) {
                        root.margin(bottom = 1.pt)
                    }
                }
            }
            tvCancel.click {
                hidden()
            }
        }
    }

    fun setOnItemClickListener(onCurrent: ((item: String?, index: Int) -> Unit)) {
        this.onCurrent = onCurrent
    }

}