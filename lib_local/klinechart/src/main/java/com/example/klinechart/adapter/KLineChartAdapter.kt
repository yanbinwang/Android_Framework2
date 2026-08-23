package com.example.klinechart.adapter

import com.example.klinechart.bean.KLineChartBean

/**
 * 数据适配器
 */
class KLineChartAdapter : BaseKLineChartAdapter() {
    private var data: MutableList<KLineChartBean> = ArrayList()

    /**
     * 数据总长度
     */
    override fun getCount(): Int {
        return data.size
    }

    /**
     * Any 返回类型，兼容 IChartDraw<T> 策略模式
     */
    override fun getItem(position: Int): Any {
        return data[position]
    }

    /**
     * 获取日期
     */
    override fun getDate(position: Int): String {
        return data[position].date
    }

    /**
     * 全量替换数据（初始化 / 切换周期 / 切换币种时调用）
     */
    fun setData(list: List<KLineChartBean>?) {
        list ?: return
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 头部添加数据
     */
    fun addHeaderData(list: List<KLineChartBean>?) {
        list ?: return
        data.addAll(0, list)
        notifyDataSetChanged()
    }

    /**
     * 尾部添加数据
     */
    fun addFooterData(list: List<KLineChartBean>?) {
        list ?: return
        // addAll 默认就是追加到尾部
        data.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 改变某个点的值
     * @param position 索引值
     */
    fun changeItem(position: Int, bean: KLineChartBean?) {
        bean ?: return
        if (position !in data.indices) return
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