package com.dataqin.mvvm.bridge

import androidx.lifecycle.MutableLiveData
import com.dataqin.common.base.bridge.BaseViewModel
import com.dataqin.mvvm.R
import com.dataqin.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/6/4.
 */
class TestListViewModel : BaseViewModel() {
    val dataListData by lazy { MutableLiveData<MutableList<TestListModel>>() }

    fun getListData() {
        //模拟请求网络
        if (null != dataListData.value && dataListData.value!!.isNotEmpty()) {
            getView()?.showToast("有数据了，清空")
            dataListData.value?.clear()
        } else {
            dataListData.value = ArrayList()
            for (i in 0..9) {
                dataListData.value?.add(TestListModel("标题$i", "描述$i", R.mipmap.ic_launcher_round))
            }
        }
        dataListData.postValue(dataListData.value) //将结果回调
    }

}