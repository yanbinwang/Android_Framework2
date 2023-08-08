package com.github.fujianlian.klinechart

import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSet

/**
 * 数据适配器
 * Created by tifezh on 2016/6/18.
 */
class KLineChartAdapter : BaseKLineChartAdapter() {
    private val datas by lazy { ArrayList<KLineEntity>() }

    override fun getCount(): Int {
        return datas.size
    }

    override fun getItem(position: Int): Any? {
        return datas.safeGet(position)
    }

    override fun getDate(position: Int): String? {
        return datas.safeGet(position)?.Date
    }

    /**
     * 向头部添加数据
     */
    fun addHeaderData(data: List<KLineEntity>?) {
        if (!data.isNullOrEmpty()) {
            datas.clear()
            datas.addAll(data)
        }
    }

    /**
     * 向尾部添加数据
     */
    fun addFooterData(data: List<KLineEntity>?) {
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
    fun changeItem(position: Int, data: KLineEntity?) {
        data ?: return
        datas.safeSet(position, data)
        notifyDataSetChanged()
    }

    /**
     * 数据清除
     */
    fun clearData() {
        datas.clear()
        notifyDataSetChanged()
    }
}