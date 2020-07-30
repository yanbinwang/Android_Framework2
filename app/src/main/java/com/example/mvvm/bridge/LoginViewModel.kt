package com.example.mvvm.bridge

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.http.callback.HttpSubscriber
import com.example.common.subscribe.BaseSubscribe.getTestApi
import com.example.mvvm.model.UserInfoModel

/**
 * Created by WangYanBin on 2020/6/3.
 */
class LoginViewModel : BaseViewModel() {
    var userInfoLiveData = MutableLiveData<UserInfoModel>() //接口得到的用户对象，泛型string也可替换为对象

    fun getData() {
        for (i in 0..999) {
            addDisposable(getTestApi(), object : HttpSubscriber<Any>() {

                override fun onSuccess(data: Any?) {
                }

                override fun onFailed(msg: String?) {
                }

                override fun onComplete() {
                    super.onComplete()
                    getView().log("当前第" + i + "个请求结束！")
                }
            })
        }

//        BaseSubscribe
//            .getTestApi()
//            .observe(this,object : HttpSubscriber<Any>(){
//
//                override fun onStart() {
//                    super.onStart()
//                    getView().showDialog()
//                }
//
//                override fun onSuccess(data: Any?) {
//                }
//
//                override fun onFailed(msg: String?) {
//                }
//
////                override fun onComplete() {
////                    super.onComplete()
////                    getView().hideDialog()
////                }
//
//            })
    }

//        public void getData(){
//            for (int i = 0; i < 1000; i++) {
//                int position = i;
//                view.get().log("当前第" + position + "个请求开始！");
//    //            LiveDataBus.BusMutableLiveData x = new LiveDataBus.BusMutableLiveData();
//                BaseSubscribe.INSTANCE
//                        .getTestApi()
//                        //传入对应的生命周期避免内存泄漏
//                        .observe(getOwner(),new HttpSubscriber<Object>() {
//                            @Override
//                            protected void onSuccess(Object data) {
//
//                            }
//
//                            @Override
//                            protected void onFailed(String msg) {
//                            }
//
//                            @Override
//                            protected void onFinish() {
//                                view.get().log("当前第" + position + "个请求结束！");
//                            }
//                        });
//            }
//        }

    fun login(account: String, password: String) {
        getView().showDialog()
        getView().showToast("当前执行了登录\n账号：$account\n密码：$password")
        getView().hideDialog()

        //do网络请求-将对象传递给下一个页面，或者直接当前页面处理，处理和mvc写法一致(失败的处理直接回调对应观察的数据,或者在baseviewmodel中处理)
        val model = UserInfoModel("老王万寿无疆", 88, "bilibili")
        userInfoLiveData.postValue(model)
    }

}