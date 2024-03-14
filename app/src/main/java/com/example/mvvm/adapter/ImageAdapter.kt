package com.example.mvvm.adapter

import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.utils.function.color
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.mvvm.databinding.ItemImageBinding

/**
 * @description
 * @author
 */
class ImageAdapter : BaseQuickAdapter<Int, ItemImageBinding>() {
    override fun onConvert(holder: BaseViewDataBindingHolder, item: Int?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
//        holder.getItemView().layoutParams =
//            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        mBinding?.root?.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        val index = holder.absoluteAdapterPosition.mod(list().safeSize)
        val bean = list().safeGet(index)
        mBinding?.apply {
            viewTest.setBackgroundColor(color(bean.orZero))
        }
    }

    override fun getItemCount(): Int {
        return if (list().size < 2) list().safeSize else Int.MAX_VALUE
    }

}