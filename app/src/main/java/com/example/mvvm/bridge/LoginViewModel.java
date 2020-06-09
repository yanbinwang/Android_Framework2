package com.example.mvvm.bridge;

import android.content.Intent;
import android.widget.Toast;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.activity.UserInfoActivity;
import com.example.mvvm.databinding.ActivityLoginBinding;
import com.example.mvvm.model.UserInfoModel;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class LoginViewModel extends BaseViewModel {

    public void login() {
        ActivityLoginBinding binding = getBinding();
        String account = getViewValue(binding.etAccount);
        String password = getViewValue(binding.etPassword);
        Toast.makeText(context.get(), "当前执行了登录\n账号：" + account + "\n密码：" + password, Toast.LENGTH_SHORT).show();
        //do网络请求-将对象传递给下一个页面，或者直接当前页面处理，处理和mvc写法一致

        UserInfoModel model = new UserInfoModel("老王万寿无疆", 88, "bilibili");

        activity.get().startActivity(new Intent(context.get(), UserInfoActivity.class).putExtra("model", model));
        activity.get().finish();
    }

}
