package com.example.debugging.adapter

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.debugging.BR
import com.example.debugging.bean.RequestBean
import com.example.debugging.databinding.ItemLogBinding

/**
 * 日志显示适配器
 */
class LogAdapter : BaseQuickAdapter<RequestBean, ItemLogBinding>() {

    override fun onConvert(holder: BaseViewDataBindingHolder, item: RequestBean?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        mBinding?.setVariable(BR.bean, item)
    }

}