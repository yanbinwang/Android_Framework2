package com.example.common.utils.helper

import android.text.TextUtils
import com.example.common.constant.Constants
import com.example.common.model.UserModel
import com.example.common.utils.analysis.GsonUtil.jsonToObj
import com.example.common.utils.analysis.GsonUtil.objToJson
import com.tencent.mmkv.MMKV

/**
 * Created by WangYanBin on 2020/8/11.
 * 公司使用的持久登陆采取每次请求时校验一次key
 * 如果key不存在或者达不到一定要求，就去重新请求key接口
 * 该工具类对key值和用户信息的一些字做了规整和管控，全局直接调用即可
 */
object AccountHelper {
    private val mmkv by lazy {
        MMKV.defaultMMKV()
    }

    //修改是否登陆
    @JvmStatic
    fun setLogin(isLogin: Boolean) {
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
    @JvmStatic
    fun isLogin(): Boolean {
//        KeyBean keyBean = getKeyBean();
//        if (null != keyBean) {
//            isLogin = "1".equals(keyBean.getUid());
//        }
        return false
    }

    //存储用户对象
    @JvmStatic
    fun setUserBean(bean: UserModel?) {
        if (null != bean) {
            mmkv.encode(Constants.KEY_USER_MODEL, objToJson(bean))
        }
    }

    //获取用户对象
    @JvmStatic
    fun getUserBean(): UserModel? {
        var userInfoBean: UserModel? = null
        val userInfoJson = mmkv.decodeString(Constants.KEY_USER_MODEL)
        if (!TextUtils.isEmpty(userInfoJson)) {
            userInfoBean = jsonToObj(userInfoJson, UserModel::class.java)
        }
        return userInfoBean
    }

    //获取手机号
    @JvmStatic
    fun getMobile(): String? {
        var mobile: String? = null
        val userModel = getUserBean()
        if (null != userModel) {
            mobile = userModel.mobile
        }
        return mobile
    }

    //是否实名认证
    @JvmStatic
    fun isRealVerified(): Boolean {
        var real_verified = false
        val userModel = getUserBean()
        if (null != userModel) {
            real_verified = userModel.real_verified!!
        }
        return real_verified
    }

    //设置实名认证状态(init:初始化,inreview:提交认证中,verified:已认证,refused:认证失败)
    @JvmStatic
    fun setCustomerStatus(customer_status: String?) {
        val userModel = getUserBean()
        if (null != userModel) {
            userModel.customer_status = customer_status
        }
        setUserBean(userModel)
    }

    //实名认证状态
    @JvmStatic
    fun getCustomerStatus(): String? {
        var customer_status: String? = null
        val userModel = getUserBean()
        if (null != userModel) {
            customer_status = userModel.customer_status
        }
        return customer_status
    }

    //用户注销操作（清除信息,清除用户凭证）
    @JvmStatic
    fun signOut() {
        mmkv.encode(Constants.KEY_USER_MODEL, "")
    }

}