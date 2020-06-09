package com.example.mvvm.bridge;

import android.content.Intent;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.activity.LoginActivity;
import com.example.mvvm.activity.TestListActivity;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class MainViewModel extends BaseViewModel {

    @Override
    public void initData() {
        super.initData();
        //        BaseSubscribe.INSTANCE
//                .download("cgdfgdf")
//                .observeForever(new Observer<ResponseBody>() {
//                    @Override
//                    public void onChanged(ResponseBody responseBody) {
//                        ResponseBody body =  responseBody;
//                    }
//                });

//        BaseSubscribe.INSTANCE
//                .getVerification("dsfsd",new HttpParams().append("dsadsa","sddas").getParams())
//                .observeForever(new HttpSubscriber<Object>() {
//                    @Override
//                    protected void onSuccess(Object data) {
//
//                    }
//
//                    @Override
//                    protected void onFailed(String msg) {
//
//                    }
//
//                    @Override
//                    protected void onFinish() {
//
//                    }
//                });
    }

    public void toLoginPage() {
        activity.get().startActivity(new Intent(context.get(), LoginActivity.class));
    }


    public void toList() {
        activity.get().startActivity(new Intent(context.get(), TestListActivity.class));
    }

}
