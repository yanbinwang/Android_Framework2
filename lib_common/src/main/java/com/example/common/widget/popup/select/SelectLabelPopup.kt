package com.example.common.widget.popup.select

import com.example.common.base.BaseBottomSheetDialogFragment
import com.example.common.databinding.ViewPopupSelectLabelBinding
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size

/**
 * Created by wangyanbin
 * 底部多选弹框
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectLabelPopup<T>(private var list: List<T>, var formatter: (T?) -> String?) : BaseBottomSheetDialogFragment<ViewPopupSelectLabelBinding>() {
    private var listener: ((item: String?, index: Int) -> Unit)? = null

    companion object {

        /**
         * 不添加默认数据的构建
         */
        fun create(list: List<String>? = emptyList()): SelectLabelPopup<String> {
            return SelectLabelPopup(list.orEmpty()) { it }
        }

        fun createByI18(list: List<Int>? = emptyList()): SelectLabelPopup<Int> {
            return SelectLabelPopup(list.orEmpty()) { "" }
        }

    }

    override fun initEvent() {
        super.initEvent()
        mBinding?.tvCancel.click {
            dismiss()
        }
    }

    override fun initData() {
        super.initData()
        mBinding?.llItem?.apply {
            removeAllViews()
            list.forEachIndexed { index, t ->
                // 获取根布局 (只对Int做特殊处理)
                val root = SelectItemHolder(this, if (t is Int) t else formatter(t), index).also {
                    it.onItemClick = { item, index ->
                        dismiss()
                        listener?.invoke(item, index)
                    }
                }.getRoot()
                // 添加布局进外层父布局
                addView(root)
                // 添加完成后设置大小
                root.size(height = 50.pt)
                // 判断是否需要添加下划线
                if (list.safeSize - 1 > index) {
                    root.margin(bottom = 1.pt)
                }
            }
        }
    }

    /**
     * 刷新内部布局
     */
    fun setParams(data: List<T>) {
        list = data
        initData()
    }

    /**
     * 设置监听
     */
    fun setOnItemClickListener(listener: ((item: String?, index: Int) -> Unit)) {
        this.listener = listener
    }

    /**
     * 获取数据
     */
    fun getData(): List<T> {
        return list
    }

}