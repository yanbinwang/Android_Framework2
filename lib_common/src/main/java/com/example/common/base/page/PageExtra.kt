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
    //请求id-应用内所有详情页需要id的都采用该字段
    const val ID = "id"
    //前一次首页选中下标-如果app被系统回收，首页获取历史选中下标采用该字段
    const val TAB_INDEX = "tabIndex"
//    //手机号
//    const val MOBILE = "mobile"
//    //短信验证码
//    const val VERIFY_SMS = "verifySms"
}

/**
 * app内跳转回调编码
 */
object RequestCode {
    //批量关闭回调编码
    const val REQUEST_FINISH = 10000
    //图片回调编码
    const val REQUEST_PHOTO = 10001
    //拍照回调编码
    const val REQUEST_IMAGE = 10002
    //录像回调编码
    const val REQUEST_VIDEO = 10003
}