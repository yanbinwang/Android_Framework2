package com.example.common.widget

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.example.base.utils.function.inflate
import com.example.base.utils.function.view.setMediumBold
import com.example.base.widget.IndicatorGroup
import com.example.common.R
import com.example.common.utils.setParameter

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
@SuppressLint("StaticFieldLeak")
class IndicatorLayout private constructor() : IndicatorGroup<String>() {

    override fun onCreateCustomView(item: String?, current: Boolean): View {
        val view = context?.inflate(R.layout.item_tab)!!
//        val view = LayoutInflater.from(context).inflate(R.layout.item_tab, null)
        onBindCustomView(view, item, current)
        return view
    }

    override fun onBindCustomView(view: View, item: String?, current: Boolean) {
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        tvTitle.apply {
            setMediumBold()
            setParameter(item.orEmpty(), if (current) R.color.blue_3d81f2 else R.color.grey_333333)
        }
    }

    companion object {
        @JvmStatic
        val instance by lazy { IndicatorLayout() }
    }

}