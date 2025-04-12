package com.example.klinechart

import android.database.DataSetObservable
import android.database.DataSetObserver
import com.example.klinechart.base.IAdapter

/**
 * k线图的数据适配器
 * Created by tifezh on 2016/6/9.
 */
abstract class BaseKLineChartAdapter : IAdapter {
    private val mDataSetObservable = DataSetObservable()

    override fun notifyDataSetChanged() {
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