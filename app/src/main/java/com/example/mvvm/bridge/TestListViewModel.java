package com.example.mvvm.bridge;

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
    private List<TestListModel> list = new ArrayList<>();

    @Override
    public void initData() {
        super.initData();
        ActivityTestListBinding binding = getBinding();
        for (int i = 0; i < 10; i++) {
            list.add(new TestListModel("标题" + i, "描述" + i, "头像" + i));
        }
//        BaseAdapter<TestListModel> adapter;
//        adapter = new BaseAdapter<>(R.layout.item_test, BR.model, list);

        TestListAdapter adapter = new TestListAdapter(list);

        binding.recTest.setLayoutManager(new LinearLayoutManager(context.get()));
        binding.recTest.setAdapter(adapter);
    }

}
