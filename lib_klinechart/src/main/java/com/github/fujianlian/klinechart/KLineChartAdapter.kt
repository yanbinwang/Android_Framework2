package com.github.fujianlian.klinechart

import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.findIndexOf
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import java.util.*

/**
 * 数据适配器
 * Created by tifezh on 2016/6/18.
 */
class KLineChartAdapter : BaseKLineChartAdapter() {
    private val dataList: MutableList<KLineEntity> = ArrayList()
    override val count: Int
        get() = dataList.size

    override fun getItem(position: Int): Any? {
        return try {
            when {
                position < 0 -> {
                    dataList[0]
                }
                position > dataList.size - 1 -> {
                    dataList[dataList.size - 1]
                }
                else -> {
                    dataList[position]
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getPositionFromTime(time: Long): Int? {
        return try {
            when {
                dataList.safeGet(0)?.DateL.orZero > time -> {
                    null
                }
                dataList.safeGet(count - 1)?.DateL.orZero < time -> {
                    count - 1
                }
                else -> {
                    dataList.findIndexOf {
                        it.DateL.orZero > time
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getDate(position: Int): String? {
        return when {
            position < 0 -> {
                dataList[0].date
            }
            position > dataList.size - 1 -> {
                dataList[dataList.size - 1].date
            }
            else -> {
                dataList[position].date
            }
        }
    }

    override fun getDateL(position: Int): String {
        return EN_YMDHMS.convert(dataList[position].DateL.orZero)
    }

    /**
     * 向头部添加数据
     */
    fun addHeaderData(data: List<KLineEntity>?) {
        if (!data.isNullOrEmpty()) {
            dataList.clear()
            dataList.addAll(data)
        }
    }

    /**
     * 向尾部添加数据
     */
    fun addFooterData(data: List<KLineEntity>?) {
        if (data != null && data.isNotEmpty()) {
            dataList.clear()
            dataList.addAll(0, data)
        }
    }

    /**
     * 改变某个点的值
     *
     * @param position 索引值
     */
    fun changeItem(position: Int, data: KLineEntity) {
        dataList[position] = data
        notifyDataSetChanged()
    }

    /**
     * 数据清除
     */
    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }
}