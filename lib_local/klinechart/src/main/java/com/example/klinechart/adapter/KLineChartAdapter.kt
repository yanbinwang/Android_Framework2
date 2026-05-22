package com.example.klinechart.adapter

import com.example.klinechart.bean.KLineChartBean

/**
 * 数据适配器
 */
class KLineChartAdapter : BaseKLineChartAdapter() {
    private val data by lazy { ArrayList<KLineChartBean>() }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getDate(position: Int): String {
        return data[position].mDate
    }

    /**
     * 头部添加数据
     */
    fun addHeaderData(list: MutableList<KLineChartBean>?) {
        list ?: return
        data.clear()
        data.addAll(list)
    }

    /**
     * 尾部添加数据
     */
    fun addFooterData(list: MutableList<KLineChartBean>?) {
        list ?: return
        data.clear()
        data.addAll(0, list)
    }

    /**
     * 改变某个点的值
     *
     * @param position 索引值
     */
    fun changeItem(position: Int, bean: KLineChartBean?) {
        bean ?: return
        data[position] = bean
        notifyDataSetChanged()
    }

    /**
     * 数据清除
     */
    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }

}