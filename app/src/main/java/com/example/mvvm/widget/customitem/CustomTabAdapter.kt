package com.example.mvvm.widget.customitem

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.utils.function.orNoData
import com.example.framework.utils.function.defTypeMipmap
import com.example.framework.utils.function.value.orFalse
import com.example.glide.ImageLoader
import com.example.mvvm.databinding.ItemCustomTabBinding

/**
 * 按钮适配器
 */
class CustomTabAdapter : BaseQuickAdapter<Triple<Boolean, String, String>, ItemCustomTabBinding>() {

    override fun onConvert(holder: BaseViewDataBindingHolder, item: Triple<Boolean, String, String>?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        mBinding?.apply {
            val localAsset = item?.first.orFalse
            val url = item?.second.orEmpty()
            val label = item?.third.orNoData()
            if (localAsset) {
                ImageLoader.instance.loadImageDrawableFromResource(ivTab, holder.itemView.context?.defTypeMipmap(url))
            } else {
                ImageLoader.instance.loadImageFromUrl(ivTab, url)
            }
            tvTab.text = label
        }
    }

}