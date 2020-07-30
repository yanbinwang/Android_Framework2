package com.example.common.http.callback

/**
 * Created by WangYanBin on 2020/7/17.
 * 请求通过拿取Observer对象再做处理，对上一级的3个回调再处理，
 * onNext分为了成功失败，剥除了外层的嵌套判断，用于项目内部服务器请求回调
 * onSuccess->请求成功，直接回调对象
 * onFailed->请求失败，获取失败原因
 */
abstract class HttpSubscriber<T> : HttpObserver<ApiResponse<T>?>() {

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
    }

    override fun onComplete() {
    }

    protected abstract fun onSuccess(data: T?)

    protected abstract fun onFailed(msg: String?)

}