package com.example.thirdparty.track.bean

/**
 * 埋点枚举类
 */
enum class TrackEvent(val tag: String) {
    /**
     * 点击Banner的总次数，点击了哪个banner(banner点击数)
     */
    HOME_BANNER("Home_banner"),

    /**
     * 入口点击的次数(头像（左上角）)
     */
    HOME_PORTRAIT("Home_portrait"),

    /**
     * 点击获取验证码的次数(注册，获取验证码)
     */
    SMS_CODE("get_sms_code"),

    /**
     * 获取验证码，进行提交的次数(注册，确认下一步)
     */
    REGISTER_NEXT("register_next"),

    /**
     * 服务端注册成功之后，数据上报一次，所有都需要上报(所有注册成功)
     */
    REGISTER_SUCCESS("all_register_success"),

    /**
     * 通过邮箱登录成功的次数(邮箱登录成功)
     */
    LOGIN_EMAIL_SUCCESS("m_login_success"),

    /**
     * 通过邮箱注册成功的新用户数量(邮箱注册成功)
     */
    REGISTER_MAIL_SUCCESS("m_register_success"),

    /**
     * 用户提交实名认证的次数(实名认证提交)
     */
    CERTIFICATION("certification"),

    /**
     * 充值按钮点击次数，不同来源进行累加(点击充值按钮次数)
     */
    PAYMENT_CLICK("payment_click")
}