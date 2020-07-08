package com.example.mvvm.bridge

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.mvvm.R
import com.example.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/6/4.
 */
class TestListViewModel : BaseViewModel() {
    var dataList = MutableLiveData<MutableList<TestListModel>>()

    fun getListData() {
//        var refresh = false
//        if(refresh){
//            dataList.value=ArrayList()
//        }else{
//            dataList.value?.addAll()
//        }

        //模拟请求网络
        var list: MutableList<TestListModel>? = dataList.value
        if (null != dataList.value && dataList.value!!.isNotEmpty()) {
            getView().showToast("有数据了，清空")
            list?.clear()
        } else {
            list = ArrayList()
            for (i in 0..9) {
                list.add(TestListModel("标题$i", "描述$i", R.mipmap.ic_launcher_round))
            }
        }
        dataList.postValue(list) //将结果回调
    }

}