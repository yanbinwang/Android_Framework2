package com.example.common.base.page

import com.example.common.config.RouterPath
import com.example.common.utils.helper.AccountHelper
import com.example.common.utils.manager.AppManager
import com.example.framework.utils.logE
import com.therouter.TheRouter
import com.therouter.router.RouteItem
import com.therouter.router.interceptor.InterceptorCallback
import com.therouter.router.interceptor.RouterInterceptor

/**
 * 全局路由AOP拦截器
 * 全局唯一可拦截所有路由跳转，控制是否放行/中断
 * https://therouter.cn/docs/2022/08/28/01
 * 单纯的登录限制可以使用RouterReplaceInterceptor
 */
class PageInterceptor : RouterInterceptor {

    companion object {
        private const val TAG = "PageInterceptor"

        /**
         * 登录全局拦截器编号
         * @Route(path = RouterPath.MainActivity, params = [INTERCEPTOR_LOGIN, VALUE_NEED_LOGIN])
         * 注意clean让路由表能生成
         */
        const val INTERCEPTOR_LOGIN = "interceptor_login"

//        const val VALUE_NEED_LOGIN = "true"

        /**
         * 判断是否需要拦截当前路由。
         * @param routeItem 路由信息
         * @return true: 需要拦截, false: 不需要拦截
         */
        @JvmStatic
        fun shouldIntercept(routeItem: RouteItem?, onInterrupt: (Throwable) -> Unit = {}): Boolean {
            routeItem ?: return false
            // 跳过登录页本身的拦截（避免循环跳转）
            if (routeItem.path == RouterPath.LoginActivity) {
                "PageInterceptor ---> 跳过登录页本身的拦截".logE(TAG)
                return false
            }
            return try {
//                val extrasMap = routeItem.getExtras().toMap()
//                val needLogin  = extrasMap[INTERCEPTOR_LOGIN]
//                if (needLogin == VALUE_NEED_LOGIN) {
//                    !AccountHelper.isLogin()
//                } else {
//                    false
//                }
                /**
                 * 只校验登录的情况下,拿description比params更优雅
                 * 页面直接@Route(path = RouterPath.MainActivity, description = INTERCEPTOR_LOGIN)
                 * "needLogin;;needVIP" <-- 也可以多字段特殊处理
                 */
                val flags = routeItem.description.split(";;")
                // 是否登录拦截
                val needLogin = flags.contains(INTERCEPTOR_LOGIN)
                if (needLogin) {
                    "PageInterceptor ---> 开始执行登录检查".logE(TAG)
                    // 进入拦截校验,检测本地用户是否登录
                    val isLogin = AccountHelper.isLogin()
                    if (!isLogin) {
                        "PageInterceptor ---> 用户未登录且需要登录，跳转到登录页".logE(TAG)
                        TheRouter.build(RouterPath.LoginActivity).navigation(AppManager.currentActivity())
                    }
                    // 如果已经登录返回的则是false,正常走接下来的逻辑.true的话说明未登录,已经做了跳转处理
                    !isLogin
                } else {
                    false
                }
            } catch (e: Exception) {
                onInterrupt(e)
                false
            }
        }
    }

    override fun process(routeItem: RouteItem, callback: InterceptorCallback) {
        if (routeItem.getExtras().getBoolean(Extra.SKIP_INTERCEPT, false)) {
            callback.onContinue(routeItem)
        } else {
            val isIntercepted = try {
                shouldIntercept(routeItem) { throwable ->
                    "PageInterceptor ---> 路由配置异常: ${throwable.message}".logE(TAG)
                    throw IllegalArgumentException("路由参数 '${INTERCEPTOR_LOGIN}' 配置错误", throwable)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            if (!isIntercepted) {
                // 不需要拦截，继续执行原路由
                "PageInterceptor ---> 无需拦截，继续执行原路由".logE(TAG)
                callback.onContinue(routeItem)
            }
        }
    }

}