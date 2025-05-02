package com.example.debugging.adapter

import android.annotation.SuppressLint
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.debugging.BR
import com.example.debugging.R
import com.example.debugging.databinding.ItemListSelectBinding
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.visible

@SuppressLint("NotifyDataSetChanged")
class ListSelectAdapter : BaseQuickAdapter<String, ItemListSelectBinding>() {
    private var selected = -1

    override fun onConvert(holder: BaseViewDataBindingHolder, item: String?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        item ?: return
        mBinding?.apply {
            setVariable(BR.value, item)
            val position = holder.absoluteAdapterPosition
            tvItem.let {
                if (position == selected) {
                    it.bold(true)
                    it.textSize(R.dimen.textSize17)
                } else {
                    it.bold(false)
                    it.textSize(R.dimen.textSize15)
                }
            }
            if (position >= size() - 1) {
                viewLine.gone()
            } else {
                viewLine.visible()
            }
        }
    }

    fun setSelected(selected: Int, isNotify: Boolean = true) {
        this.selected = selected
        if (isNotify) notifyDataSetChanged()
    }

    fun getSelected(): Int {
        return selected
    }

}