package com.example.common.widget.popup.select

import android.view.ViewGroup
import com.example.common.R
import com.example.common.databinding.ItemPopupSelectBinding
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click

/**
 * first->文案
 * second->下标
 */
class SelectItemHolder(parent: ViewGroup, triple: Pair<String, Int>) {
    internal val mBinding by lazy { ItemPopupSelectBinding.bind(parent.context.inflate(R.layout.item_popup_select)) }
    internal var onItemClick: ((item: String?, index: Int) -> Unit)? = { _, _ -> }

    init {
        mBinding.tvLabel.text = triple.first
        mBinding.root.click { onItemClick?.invoke(triple.first, triple.second) }
    }

}