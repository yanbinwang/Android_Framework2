package com.example.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.base.utils.IndicatorGroup
import com.example.base.utils.function.view.setMediumBold
import com.example.common.R

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
@SuppressLint("StaticFieldLeak")
class TabLayoutFactory private constructor(var context: Context) : IndicatorGroup() {

    override fun onCreateCustomView(item: Any?, current: Boolean): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab, null)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        tvTitle.apply {
            setMediumBold()
            setData(item as String, if (current) R.color.blue_3d81f2 else R.color.grey_333333)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun with(context: Context?): TabLayoutFactory {
            return TabLayoutFactory(context!!)
        }
    }

}