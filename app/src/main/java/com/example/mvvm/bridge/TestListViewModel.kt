package com.example.mvvm.bridge

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.mvvm.databinding.ItemTestBinding
import com.example.mvvm.model.TestListModel
import java.util.*

/**
 * Created by WangYanBin on 2020/6/4.
 */
class TestListViewModel : BaseViewModel<ItemTestBinding?>() {
    var dataList = MutableLiveData<List<TestListModel>>()

    fun getListData() {
        //模拟请求网络
        val list: MutableList<TestListModel> =
            ArrayList()
        for (i in 0..9) {
            list.add(TestListModel("标题$i", "描述$i", "头像$i"))
        }
        dataList.postValue(list) //将结果回调给P层
    }

}