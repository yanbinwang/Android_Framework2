package com.example.mvvm.bridge;

import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.adapter.TestListAdapter;
import com.example.mvvm.databinding.ActivityTestListBinding;
import com.example.mvvm.model.TestListModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangYanBin on 2020/6/4.
 */
public class TestListViewModel extends BaseViewModel {
    public MutableLiveData<List<TestListModel>> dataList = new MutableLiveData<>();

    public void getListData() {
//        ActivityTestListBinding binding = getBinding();
        List<TestListModel> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new TestListModel("标题" + i, "描述" + i, "头像" + i));
        }
        dataList.postValue(list);//将结果回调给P层
//        //绑定适配器
//        TestListAdapter adapter = new TestListAdapter(list);
//        binding.setAdapter(adapter);
//        dataList.postValue(list);
//        binding.recTest.setAdapter(adapter);
    }

}
