package com.alibaba.android.arouter.utils

/**
 * ARouter constants.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 9:38
 */
object Consts {
    const val SDK_NAME = "ARouter"
    const val TAG = "$SDK_NAME::"
    const val SEPARATOR = "$$"
    const val SUFFIX_ROOT = "Root"
    const val SUFFIX_INTERCEPTORS = "Interceptors"
    const val SUFFIX_PROVIDERS = "Providers"
    const val SUFFIX_AUTOWIRED = SEPARATOR + SDK_NAME + SEPARATOR + "Autowired"
    const val DOT = "."
    const val ROUTE_ROOT_PAKCAGE = "com.alibaba.android.arouter.routes"

    const val AROUTER_SP_CACHE_KEY = "SP_AROUTER_CACHE"
    const val AROUTER_SP_KEY_MAP = "ROUTER_MAP"

    const val LAST_VERSION_NAME = "LAST_VERSION_NAME"
    const val LAST_VERSION_CODE = "LAST_VERSION_CODE"
}