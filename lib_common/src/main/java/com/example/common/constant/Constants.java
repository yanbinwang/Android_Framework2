package com.example.common.constant;

import android.os.Environment;

/**
 * Created by wyb on 2017/3/7.
 * 配置文件，用于存放一些用得到的静态变量
 */
public class Constants {
    //app内的一些默认值
    public static int SCREEN_WIDTH;//手机宽度
    public static int SCREEN_HEIGHT;//手机高度
    public static int STATUS_BAR_HEIGHT;//导航栏高度
    public static String IP;//当前手机ip
    public static String MAC;//当前手机mac地址
    public static String DEVICE_ID;//当前手机设备id
    public static String APPLICATION_NAME;//当前应用名
    public static String APPLICATION_ID;//当前包名
    //app内接口的一些默认配置字段
    public static final int LOGIN_INTERCEPTOR_CODE = 1;//阿里路由登录全局拦截器编号
    public static final int PUSH_NOTIFY_ID = 0;//固定通知id
    public static final String PUSH_CHANNEL_ID = "shuniuyun";//推送渠道id
    public static final String PUSH_CHANNEL_NAME = "数牛金服";//推送渠道名
    public static final String LIMIT = "10";//取的页数
    public static final String WX_APP_ID = "wx92fdc4b6ab9647cd";//微信的appId
    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();//sd卡的根路径mnt/sdcard
    //MMKV存储字段
    public static final String KEY_USER_MODEL = "keyUserModel";//用户类json
    //系统广播
    public static final String APP_USER_LOGIN = "com.bitnew.tech.APP_USER_LOGIN";//用户登录
    public static final String APP_USER_LOGIN_OUT = "com.bitnew.tech.APP_USER_LOGIN_OUT";//用户注销
//    public static final String APP_USER_INFO_UPDATE = "com.bitnew.tech.APP_USER_INFO_UPDATE";//用户信息更新
//    public static final String APP_SHARE_SUCCESS = "com.bitnew.tech.APP_SHARE_SUCCESS";//分享成功
//    public static final String APP_SHARE_CANCEL = "com.bitnew.tech.APP_SHARE_CANCEL";//分享取消
//    public static final String APP_SHARE_FAILURE = "com.bitnew.tech.APP_SHARE_FAILURE";//分享失败
}