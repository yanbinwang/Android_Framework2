package com.example.common.http;

import androidx.lifecycle.Observer;

import com.example.common.model.BaseModel;

/**
 * Created by WangYanBin on 2020/6/8.
 */
public abstract class HttpSubscriber<T> implements Observer<BaseModel<T>> {

    @Override
    public void onChanged(BaseModel<T> baseModel) {
        if (null != baseModel) {
            String msg = baseModel.getMsg();
            int e = baseModel.getE();
            if (0 == e) {
                onSuccess(baseModel.getData());
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
