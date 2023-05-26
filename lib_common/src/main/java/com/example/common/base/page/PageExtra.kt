package com.example.common.base.page

/**
 * app内的跳转字段
 */
object Extra {
    /**
     * navigation(ARouterPath.LoginActivity, Extras.REQUEST_CODE to REQUEST_FINISH)
     * setOnActivityResultListener { if (it.resultCode == REQUEST_FINISH) finish() }
     */
    //页面跳转链接
    const val REQUEST_CODE = "requestCode"
    const val RESULT_CODE = "resultCode"
    //跳转对象
    const val BUNDLE_BEAN = "bundleBean"
    //跳转集合
    const val BUNDLE_LIST = "bundleList"
    //透傳信息
    const val PAYLOAD = "payload"
    //请求id
    const val ID = "id"
    //手机号
    const val MOBILE = "mobile"
    //短信验证码
    const val VERIFY_SMS = "verifySms"
}

/**
 * app内跳转回调编码
 */
object RequestCode {
    //批量关闭回调编码
    const val REQUEST_FINISH = 10000
    //图片回调编码
    const val REQUEST_PHOTO = 10001
}