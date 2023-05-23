package com.example.mvvm.widget.keyboard

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.mvvm.databinding.ItemVirtualKeyboardBinding

/**
 * @description 输入法适配器
 * @author yan
 */
class VirtualKeyboardAdapter : BaseQuickAdapter<Map<String, String>, ItemVirtualKeyboardBinding>() {

    override fun convert(holder: BaseViewDataBindingHolder, item: Map<String, String>?, payloads: MutableList<Any>?) {
        super.convert(holder, item, payloads)
        binding.apply {
            when (holder.absoluteAdapterPosition) {
                9 -> {
                    flDelete.gone()
                    tvKeyboard.apply {
                        visible()
                        text = ""
                    }
                }
                11 -> {
                    flDelete.visible()
                    tvKeyboard.gone()
                }
                else -> {
                    flDelete.gone()
                    tvKeyboard.apply {
                        visible()
                        text = if (null != item) item["name"] else ""
                    }
                }
            }
        }
    }

}