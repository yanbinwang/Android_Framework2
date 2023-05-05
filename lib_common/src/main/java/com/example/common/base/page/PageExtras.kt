package com.example.common.base.page

/**
 * app内的跳转字段
 */
object Extras {
    /**
     * navigation(ARouterPath.LoginActivity, Extras.REQUEST_CODE to REQUEST_FINISH)
     * setOnActivityResultListener { if (it.resultCode == REQUEST_FINISH) finish() }
     */
    const val REQUEST_CODE = "requestCode" //页面跳转链接
    const val BUNDLE_BEAN = "bundleBean" //跳转对象
    const val BUNDLE_LIST = "bundleList" //跳转集合
    const val ID = "id" //请求id
    const val MOBILE = "mobile" //手机号
    const val VERIFY_SMS = "verifySms" //短信验证码
}

/**
 * app内跳转回调编码
 */
object RequestCode {
    //批量关闭回调编码
    const val REQUEST_FINISH = 10000
    //图片回调编码
    const val REQUEST_PHOTO = 10001
    //文件管理器回调编码
    const val REQUEST_MANAGER = 10002
    //指定文件回调编码
    const val REQUEST_FILE = 10003
}