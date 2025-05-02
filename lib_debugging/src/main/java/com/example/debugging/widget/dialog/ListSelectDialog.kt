package com.example.debugging.widget.dialog

import android.content.Context
import com.example.common.base.BaseDialog
import com.example.debugging.BR
import com.example.debugging.adapter.ListSelectAdapter
import com.example.debugging.databinding.ViewDialogListSelectBinding
import com.example.framework.utils.function.value.orZero

class ListSelectDialog(context: Context) : BaseDialog<ViewDialogListSelectBinding>(context, 284) {
    var isNotify = true
    private var listener: ((t: String?, position: Int) -> Unit)? = null

    init {
        mBinding?.setVariable(BR.adapter, ListSelectAdapter())
    }

    fun setParams(list: List<String>) {
        mBinding?.adapter?.refresh(list)
        mBinding?.adapter?.setOnItemClickListener { t, position ->
            setSelected(position)
        }
    }

    private fun setSelected(position: Int) {
        val adapter = mBinding?.adapter
        adapter?.setSelected(position, isNotify)
        listener?.invoke(adapter?.item(position), position)
    }

    fun setOnItemClickListener(listener: (t: String?, position: Int) -> Unit) {
        this.listener = listener
    }

    /**
     * 未选择则传-1
     */
    fun getSelectedIndex(): Int {
        return mBinding?.adapter?.getSelected().orZero
    }

    /**
     * 未选择则传null
     */
    fun getSelectedItem(): String? {
        val adapter = mBinding?.adapter
        val selected = adapter?.getSelected() ?: -1
        return adapter?.item(selected)
    }

    /**
     * 重置选择
     */
    private fun resetSelected() {
        mBinding?.adapter?.setSelected(-1, false)
    }

}