package com.example.common.constant;

/**
 * Created by wyb on 2016/6/28.
 * app内的跳转字段
 */
public interface Extras {
    String REQUEST_ID = "requestId";//请求id
    String BUNDLE_BEAN = "bundleBean";//跳转对象
    String REQUEST_CODE = "requestCode";//页面跳转链接
    String PAGE_FROM = "pageFrom";//来自何种页面
    String PAGE_TYPE = "pageType";//页面类型
    String WEB_URL = "webUrl";//网页链接
    String WEB_STATE = "webState";//网页是否是深色
    String WEB_COLOR = "webColor";//网页标题颜色
    String MOBILE = "mobile";//手机号
    String VERIFY_SMS = "verifySms";//短信验证码
}
