package com.example.mvvm.bridge

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/6/4.
 */
class TestListViewModel : BaseViewModel() {
    var dataList = MutableLiveData<MutableList<TestListModel>>()

    fun getListData() {
        //模拟请求网络
        var list: MutableList<TestListModel>? = dataList.value
        if (null != dataList.value && dataList.value!!.isNotEmpty()) {
            view?.get()?.showToast("有数据了，清空")
            list?.clear()
        }else{
            list = ArrayList()
            for (i in 0..9) {
                list.add(TestListModel("标题$i", "描述$i", "头像$i"))
            }
        }
        dataList.postValue(list) //将结果回调
    }

}