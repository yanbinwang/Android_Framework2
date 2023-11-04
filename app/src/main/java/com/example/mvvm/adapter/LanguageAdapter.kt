package com.example.mvvm.adapter

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.bean.ServerLanguage
import com.example.common.utils.i18n.LanguageUtil
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.mvvm.databinding.ItemLanguageBinding

class LanguageAdapter : BaseQuickAdapter<ServerLanguage, ItemLanguageBinding>() {

    override fun convert(holder: BaseViewDataBindingHolder, item: ServerLanguage?, payloads: MutableList<Any>?) {
        super.convert(holder, item, payloads)
        binding.apply {
            tvLanguage.text = item?.name
            if (item?.language == LanguageUtil.getLanguage()) {
                ivSelected.visible()
            } else {
                ivSelected.gone()
            }
        }
    }

}