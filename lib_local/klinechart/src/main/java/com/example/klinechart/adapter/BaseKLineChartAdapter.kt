package com.example.klinechart.adapter

import android.database.DataSetObservable
import android.database.DataSetObserver

/**
 * k线图的数据适配器
 */
abstract class BaseKLineChartAdapter : IAdapter {
    private val mDataSetObservable by lazy { DataSetObservable() }

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