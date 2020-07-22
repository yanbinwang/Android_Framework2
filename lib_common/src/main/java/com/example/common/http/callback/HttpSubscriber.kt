package com.example.common.http.callback

/**
 * Created by WangYanBin on 2020/7/17.
 * 请求通过拿取Observer对象再做处理
 */
abstract class HttpSubscriber<T> : HttpObserver<T>() {

    override fun onStart() {
    }

    override fun onNext(apiResponse: ApiResponse<T>?) {
        if (null != apiResponse) {
            val msg = apiResponse.msg
            val e = apiResponse.e
            if (0 == e) {
                onSuccess(apiResponse.data)
            } else {
//                //账号还没有登录，解密失败，重新获取
//                if (100005 == e || 100008 == e) {
//                    AccountHelper.signOut();
//                    RxBus.getInstance().post(new RxBusEvent(Constants.APP_USER_LOGIN_OUT));
//                    ARouter.getInstance().build(ARouterPath.LoginActivity).navigation();
//                }
//                //账号被锁定--进入账号锁定页（其余页面不关闭）
//                if (100002 == e) {
//                    ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation();
//                }
                onFailed(msg)
            }
        } else {
            onFailed("")
        }
        onComplete()
    }

    override fun onComplete() {
    }

    protected abstract fun onSuccess(data: T?)

    protected abstract fun onFailed(msg: String?)

}