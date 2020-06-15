package com.example.common.http;

import androidx.lifecycle.Observer;

/**
 * Created by WangYanBin on 2020/6/8.
 */
public abstract class HttpSubscriber<T> implements Observer<ResponseBody<T>> {

    @Override
    public void onChanged(ResponseBody<T> responseBody) {
        if (null != responseBody) {
            String msg = responseBody.getMsg();
            int e = responseBody.getE();
            if (0 == e) {
                onSuccess(responseBody.getData());
            } else {
//                //账号还没有登录，解密失败，重新获取
//                if (100005 == e || 100008 == e) {
//                    AccountHelper.signOut();
//                    RxBus.Companion.getInstance().post(new RxBusEvent(Constants.APP_USER_LOGIN_OUT));
//                    ARouter.getInstance().build(ARouterPath.LoginActivity).navigation();
//                }
//                //账号被锁定--进入账号锁定页（其余页面不关闭）
//                if (100002 == e) {
//                    ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation();
//                }
                onFailed(msg);
            }
        } else {
            onFailed("");
        }
        onFinish();
    }

    protected abstract void onSuccess(T data);

    protected abstract void onFailed(String msg);

    protected abstract void onFinish();

}
