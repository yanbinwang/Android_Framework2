package com.example.common.base.page

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.constant.ARouterPath
import com.example.common.constant.Constants
import com.example.framework.utils.LogUtil
import com.example.common.utils.helper.AccountHelper

/**
 * author:wyb
 * 阿里Aroite全局拦截器
 * (@Route(path = RouterPages.MUSIC_CLASS, extras = Constants.LOGIN_INTERCEPTOR_CODE))
 * 拦截器会在跳转之间执行，多个拦截器会按优先级顺序依次执行
 * priority: 优先级，越小，优先级越高
 * 两个拦截器的优先级一样，项目编译就会报错
 */
@Interceptor(priority = 1, name = "全局拦截器")
class PageInterceptor : IInterceptor {
    private val TAG = "ARouterInterceptor"

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        LogUtil.e(TAG, "ARouterInterceptor 开始执行")
        //给需要跳转的页面添加值为Constants.LOGIN_INTERCEPTOR_CODE的extra参数，用来标记是否需要用户先登录才可以访问该页面
        //先判断需不需要
        if (postcard.extra == Constants.LOGIN_INTERCEPTOR_CODE) {
            //判断用户的登录情况，可以把值保存在sp中
            if (AccountHelper.isLogin()) {
                callback.onContinue(postcard)
            } else { //没有登录,注意需要传入context
                ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
            }
        } else { //没有extra参数时则继续执行，不做拦截
            callback.onContinue(postcard)
        }
    }

    override fun init(context: Context) {
        LogUtil.e(TAG, "ARouterInterceptor 初始化")
    }

}
