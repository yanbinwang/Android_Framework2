package com.example.common.utils.helper;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.example.common.constant.Constants;
import com.example.common.model.UserModel;
import com.example.common.utils.analysis.GsonUtil;
import com.tencent.mmkv.MMKV;


/**
 * 公司使用的持久登陆采取每次请求时校验一次key
 * 如果key不存在或者达不到一定要求，就去重新请求key接口
 * 该工具类对key值和用户信息的一些字做了规整和管控，全局直接调用即可
 */
@SuppressLint("StaticFieldLeak")
public class AccountHelper {
    private static MMKV mmkv;

    static {
        mmkv = MMKV.defaultMMKV();
    }

    //修改是否登陆
    public static void setLogin(boolean isLogin) {
//        KeyBean keyBean = getKeyBean();
//        if (null != keyBean) {
//            if (isLogin) {
//                keyBean.setUid("1");
//            } else {
//                keyBean.setUid("0");
//            }
//            setKeyBean(keyBean);
//        }
    }

    //用户是否登陆
    public static boolean isLogin() {
        boolean isLogin = false;
//        KeyBean keyBean = getKeyBean();
//        if (null != keyBean) {
//            isLogin = "1".equals(keyBean.getUid());
//        }
        return isLogin;
    }

    //存储用户对象
    public static void setUserBean(UserModel bean) {
        if (null != bean) {
            mmkv.encode(Constants.USER_BEAN, GsonUtil.INSTANCE.objToJson(bean));
        }
    }

    //获取用户对象
    public static UserModel getUserBean() {
        UserModel userInfoBean = null;
        String userInfoJson = mmkv.decodeString(Constants.USER_BEAN);
        if (!TextUtils.isEmpty(userInfoJson)) {
            userInfoBean = GsonUtil.INSTANCE.jsonToObj(userInfoJson, UserModel.class);
        }
        return userInfoBean;
    }

    //获取手机号
    public static String getMobile() {
        String mobile = null;
        UserModel userModel = getUserBean();
        if (null != userModel) {
            mobile = userModel.getMobile();
        }
        return mobile;
    }

    //短信验证是否开启
    public static boolean isSmsCheck() {
        boolean sms_check = false;
        UserModel userModel = getUserBean();
        if (null != userModel) {
            sms_check = userModel.getSms_check();
        }
        return sms_check;
    }

    //谷歌验证是否开启
    public static boolean isGoogleCheck() {
        boolean google_check = false;
        UserModel userModel = getUserBean();
        if (null != userModel) {
            google_check = userModel.getGoogle_check();
        }
        return google_check;
    }

    //是否设置了资金密码
    public static boolean isHaveTradePass() {
        boolean have_trade_pass = false;
        UserModel userModel = getUserBean();
        if (null != userModel) {
            have_trade_pass = userModel.getHave_trade_pass();
        }
        return have_trade_pass;
    }

    //是否实名认证
    public static boolean isRealVerified() {
        boolean real_verified = false;
        UserModel userModel = getUserBean();
        if (null != userModel) {
            real_verified = userModel.getReal_verified();
        }
        return real_verified;
    }

    //设置实名认证状态(init:初始化,inreview:提交认证中,verified:已认证,refused:认证失败)
    public static void setCustomerStatus(String customer_status) {
        UserModel userModel = getUserBean();
        if (null != userModel) {
            userModel.setCustomer_status(customer_status);
        }
        setUserBean(userModel);
    }

    //实名认证状态
    public static String getCustomerStatus() {
        String customer_status = null;
        UserModel userModel = getUserBean();
        if (null != userModel) {
            customer_status = userModel.getCustomer_status();
        }
        return customer_status;
    }

    //用户注销操作（清除信息,清除用户凭证）
    public static void signOut() {
        mmkv.encode(Constants.USER_BEAN, "");
    }

}
