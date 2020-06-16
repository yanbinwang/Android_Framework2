package com.example.mvvm.bridge;

import android.view.View;

import com.example.common.base.bridge.BaseViewModel;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.R;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class MainViewModel extends BaseViewModel implements View.OnClickListener {

    public void toTestRequest(){
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                view.get().navigation(ARouterPath.LoginActivity);
                break;
            case R.id.btn_list:
                view.get().navigation(ARouterPath.TestListActivity);
                break;
        }
    }
}
