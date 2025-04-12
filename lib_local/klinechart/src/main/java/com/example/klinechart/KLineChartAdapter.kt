package com.example.klinechart

import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSet
import com.example.framework.utils.function.value.safeSize

/**
 * 数据适配器
 * Created by tifezh on 2016/6/18.
 */
class KLineChartAdapter : BaseKLineChartAdapter() {
    private val datas = ArrayList<KLineEntity>()

    override fun getCount(): Int {
        return datas.safeSize
    }

    override fun getItem(position: Int): Any {
        return datas.safeGet(position) ?: Any()
    }

    override fun getDate(position: Int): String {
        return datas.safeGet(position)?.Date.orEmpty()
    }

    /**
     * 向头部添加数据
     */
    open fun addHeaderData(data: List<KLineEntity>?) {
        if (!data.isNullOrEmpty()) {
            datas.clear()
            datas.addAll(data)
        }
    }

    /**
     * 向尾部添加数据
     */
    open fun addFooterData(data: List<KLineEntity>?) {
        if (!data.isNullOrEmpty()) {
            datas.clear()
            datas.addAll(0, data)
        }
    }

    /**
     * 改变某个点的值
     *
     * @param position 索引值
     */
    open fun changeItem(position: Int, data: KLineEntity?) {
        data ?: return
        datas.safeSet(position, data)
        notifyDataSetChanged()
    }

    /**
     * 数据清除
     */
    open fun clearData() {
        datas.clear()
        notifyDataSetChanged()
    }

}