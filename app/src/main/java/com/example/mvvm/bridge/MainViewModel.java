package com.example.mvvm.bridge;

import android.content.Intent;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.activity.LoginActivity;
import com.example.mvvm.activity.TestListActivity;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class MainViewModel extends BaseViewModel {

    public void toLoginPage() {
        activity.get().startActivity(new Intent(context.get(), LoginActivity.class));
    }


    public void toList() {
        activity.get().startActivity(new Intent(context.get(), TestListActivity.class));
    }

}
