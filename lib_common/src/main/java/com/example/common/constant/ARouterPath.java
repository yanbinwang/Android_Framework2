package com.example.common.constant;

/**
 * author: wyb
 * date: 2018/9/17.
 * 阿里模块跳转配置
 */
public interface ARouterPath {
    //main模块
    String StartActivity = "/main/start";//启动页
    String MainActivity = "/main/main";//首页
    //homepage模块
    String UnlockIPActivity = "/homepage/unlockIP";//锁定ip页
    String WebViewActivity = "/homepage/webView";//全局网页
    String ScanActivity = "/homepage/scan";//全局扫码页
    //assets模块
    String AssetsDetailActivity = "/assets/assetsDetail";//钱包（资产）详情
    String RechargeActivity = "/assets/recharge";//充币页
    String CoinSearchActivity = "/assets/coinSearch";//选择货币页
    String CoinAddressActivity = "/assets/coinAddress";//选择币种地址页
    String CoinAddressSubmitActivity = "/assets/coinAddressSubmit";//添加货币地址页
    String WithdrawalActivity = "/assets/withdrawal";//提现页
    String RecordActivity = "/assets/record";//财务记录
    String RecordDetailActivity = "/assets/recordDetail";//财务记录详情
    String RedEnvelopesActivity = "/assets/redEnvelopes";//发红包
    String RedEnvelopesRecordActivity = "/assets/redEnvelopesRecord";//红包记录
    //account模块
    String LoginActivity = "/account/login";//登录页
    String RegisterActivity = "/account/register";//注册页
    String RegisterConfirmActivity = "/account/setLoginPwd";//注册页-确认（设置登录密码）
    String ForgetPasswordActivity = "/account/forgetPassword";//忘记密码
    String ForgetPasswordConfirmActivity = "/account/forgetPasswordConfirm";//忘记密码-确认
    String SettingActivity = "/account/setting";//设置页
    String SafeActivity = "/account/safe";//安全中心
    String PersonCenterActivity = "/account/personCenter";//个人中心
    String AuthenticationSubmitActivity = "/account/authenticationSubmit";//身份认证
    String AuthenticationConfirmActivity = "/account/authenticationConfirm";//身份认证-确认
    String ModifyMobileActivity = "/account/modifyMobile";//修改手机号
    String AddMailActivity = "/account/addMail";//添加邮箱
    String VerificationActivity = "/account/verification";//手机/谷歌验证
    String ModifyPasswordActivity = "/account/modifyPassword";//密码修改
    String ModifyCapitalActivity = "/account/modifyCapital";//修改资金密码
    String CapitalActivity = "/account/capitalActivity";//设置资金密码
    //transaction模块
    String EntrustActivity = "/transaction/entrust";//委托页
    String EntrustDetailActivity = "/transaction/entrustDetail";//委托详情页
    //quotation模块
    String QuotationEditActivity = "/quotation/quotationEdit";//行情编辑页
    String QuotationSearchActivity = "/quotation/QuotationSearch";//行情搜索页
}
