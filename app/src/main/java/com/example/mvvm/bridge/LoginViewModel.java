package com.example.mvvm.bridge;

import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.activity.UserInfoActivity;
import com.example.mvvm.databinding.ActivityLoginBinding;
import com.example.mvvm.model.UserInfoModel;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class LoginViewModel extends BaseViewModel {
    public MutableLiveData<String> token = new MutableLiveData<>();//接口得到的token，泛型string也可替换为对象

    public void login(String account, String password) {
//        ActivityLoginBinding binding = getBinding();
        view.get().showDialog();
        Toast.makeText(context.get(), "当前执行了登录\n账号：" + account + "\n密码：" + password, Toast.LENGTH_SHORT).show();
        view.get().hideDialog();

        //do网络请求-将对象传递给下一个页面，或者直接当前页面处理，处理和mvc写法一致(失败的处理直接回调对应观察的数据,或者在baseviewmodel中处理)
        UserInfoModel model = new UserInfoModel("老王万寿无疆", 88, "bilibili");
        activity.get().startActivity(new Intent(context.get(), UserInfoActivity.class).putExtra("model", model));
        activity.get().finish();
    }

}
