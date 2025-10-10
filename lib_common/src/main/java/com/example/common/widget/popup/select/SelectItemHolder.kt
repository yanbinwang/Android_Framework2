package com.example.common.widget.popup.select

import android.view.ViewGroup
import com.example.common.R
import com.example.common.databinding.ItemPopupSelectBinding
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click

/**
 * item->文案
 * index->下标
 */
class SelectItemHolder(parent: ViewGroup, item: String?, index: Int) {
    internal val mBinding by lazy { ItemPopupSelectBinding.bind(parent.context.inflate(R.layout.item_popup_select)) }
    internal var onItemClick: ((item: String?, index: Int) -> Unit)? = { _, _ -> }

    init {
        mBinding.tvLabel.text = item
        mBinding.root.click {
            onItemClick?.invoke(item, index)
        }
    }

}