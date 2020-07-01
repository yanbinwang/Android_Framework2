package com.example.mvvm.bridge;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.databinding.ActivityLoginBinding;
import com.example.mvvm.model.UserInfoModel;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class LoginViewModel extends BaseViewModel<ActivityLoginBinding> {
    public MutableLiveData<UserInfoModel> userInfoModel = new MutableLiveData<>();//接口得到的用户对象，泛型string也可替换为对象

//    public void getData(){
//        for (int i = 0; i < 1000; i++) {
//            int position = i;
//            view.get().log("当前第" + position + "个请求开始！");
////            LiveDataBus.BusMutableLiveData x = new LiveDataBus.BusMutableLiveData();
//            BaseSubscribe.INSTANCE
//                    .getTestApi()
//                    //传入对应的生命周期避免内存泄漏
//                    .observe(getOwner(),new HttpSubscriber<Object>() {
//                        @Override
//                        protected void onSuccess(Object data) {
//
//                        }
//
//                        @Override
//                        protected void onFailed(String msg) {
//                        }
//
//                        @Override
//                        protected void onFinish() {
//                            view.get().log("当前第" + position + "个请求结束！");
//                        }
//                    });
//        }
//    }

    public void login(String account, String password) {
        view.get().showDialog();
        Toast.makeText(context.get(), "当前执行了登录\n账号：" + account + "\n密码：" + password, Toast.LENGTH_SHORT).show();
        view.get().hideDialog();

        //do网络请求-将对象传递给下一个页面，或者直接当前页面处理，处理和mvc写法一致(失败的处理直接回调对应观察的数据,或者在baseviewmodel中处理)
        UserInfoModel model = new UserInfoModel("老王万寿无疆", 88, "bilibili");
        userInfoModel.postValue(model);
    }

}
