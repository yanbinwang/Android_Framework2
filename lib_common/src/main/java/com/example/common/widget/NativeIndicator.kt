package com.example.common.widget

import android.widget.TextView
import com.example.common.R
import com.example.common.databinding.ItemTabBinding
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.textSize
import com.google.android.material.tabs.TabLayout

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
class NativeIndicator constructor(tab: TabLayout, tabTitle: List<String>) : TabLayoutBuilder<String, ItemTabBinding>(tab, tabTitle) {
    private var listener: ((binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) -> Unit)? = null//如需自定義，重寫此監聽

    companion object {
        /**
         * 全局默认样式
         */
        fun TextView?.setTabTheme(text: String?, selected: Boolean, colorRes: Pair<Int, Int> = R.color.tabSelected to R.color.tabUnselected, sizeRes: Pair<Int, Int> = R.dimen.textSize16 to R.dimen.textSize15) {
            setArguments(text.orEmpty(), if (selected) colorRes.first.orZero else colorRes.second.orZero,)
            textSize(if (selected) sizeRes.first.orZero else sizeRes.second.orZero)
            bold(selected)
        }
    }

    override fun getBindView() = ItemTabBinding.bind(mContext.inflate(R.layout.item_tab))

    override fun onBindView(mBinding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) {
        if(null == listener) {
            mBinding?.tvTitle.setTabTheme(item, selected)
//            binding?.tvTitle.apply {
//                setArguments(item.orEmpty(), if (selected) R.color.appTheme else R.color.textHint)
//                textSize(if (selected) 16.pt else 14.pt)
//                bold(selected)
//            }
        } else {
            listener?.invoke(mBinding, item, selected, index)
        }
    }

    /**
     * 重写此方法表示部分标题字体字号样式等需要使用非默认配置
     * 需在调用bind（）方法前调取，一旦绑定就会执行，此时监听没赋值，控件会显示不出
     */
    fun setRedrawListener(listener: ((mBinding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) -> Unit)) {
        this.listener = listener
    }

}