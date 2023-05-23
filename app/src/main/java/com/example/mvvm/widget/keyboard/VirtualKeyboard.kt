package com.example.mvvm.widget.keyboard

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import com.example.common.utils.function.pt
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration.ItemDecorationProps
import com.example.framework.utils.function.inflate
import com.example.framework.widget.BaseViewGroup
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewVirtualKeyboardBinding

/**
 * @description 虚拟键盘
 * @author yan
 */
class VirtualKeyboard @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewVirtualKeyboardBinding.bind(context.inflate(R.layout.view_virtual_keyboard)) }
    private val adapter by lazy { VirtualKeyboardAdapter() }
    private val valueList by lazy { ArrayList<Map<String, String>>() }

    init {
        //初始化按钮上应该显示的数字
        for (i in 1..12) {
            val map = HashMap<String, String>()
            if (i < 10) {
                map["name"] = i.toString()
            } else if (i == 10) {
                map["name"] = "."
            } else if (i == 11) {
                map["name"] = 0.toString()
            } else if (i == 12) {
                map["name"] = ""
            }
            valueList.add(map)
        }
        adapter.refresh(valueList)
        binding.rvKeyboard.adapter = adapter
        val propMap = SparseArray<ItemDecorationProps>()
        val prop1 = ItemDecorationProps(1.pt, 1.pt, true, true)
        propMap.put(0, prop1)
        binding.rvKeyboard.addItemDecoration(SCommonItemDecoration(propMap))
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    /**
     * 获取当前数据信息
     *
     * @return
     */
    fun getValueList(): ArrayList<Map<String, String>> {
        return valueList
    }

    /**
     * 获取适配器
     */
    fun getAdapter(): VirtualKeyboardAdapter? {
        return adapter
    }

}