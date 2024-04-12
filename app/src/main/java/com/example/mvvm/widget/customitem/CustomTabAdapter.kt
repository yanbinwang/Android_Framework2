package com.example.mvvm.widget.customitem

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.utils.function.orNoData
import com.example.framework.utils.function.defTypeMipmap
import com.example.mvvm.databinding.ItemCustomTabBinding

/**
 * 按钮适配器
 */
class CustomTabAdapter : BaseQuickAdapter<Pair<String, String>, ItemCustomTabBinding>() {

    override fun onConvert(holder: BaseViewDataBindingHolder, item: Pair<String, String>?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        mBinding?.apply {
            ivTab.background = mContext?.defTypeMipmap(item?.first.orEmpty())
            tvTab.text = item?.second.orNoData()
        }
    }

}