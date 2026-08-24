package com.example.klinechart.adapter

import android.database.DataSetObservable
import android.database.DataSetObserver

/**
 * k线图的数据适配器
 */
abstract class BaseKLineChartAdapter : IAdapter {
    private val mDataSetObservable = DataSetObservable()

    override fun notifyDataSetChanged() {
        // notifyChanged() = 数据变了，View 重新绘制（正常刷新）
        // notifyInvalidated() = 数据集整体失效，View 会解除与 Adapter 的绑定，后续所有 getItem/getCount 调用都可能返回异常或空值
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged()
        } else {
            mDataSetObservable.notifyInvalidated()
        }
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.unregisterObserver(observer)
    }

}