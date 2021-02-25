package com.dataqin.common.utils.helper

import android.text.TextUtils
import com.dataqin.common.bus.LiveDataBus
import com.dataqin.common.bus.LiveDataEvent
import com.dataqin.common.constant.Constants
import com.dataqin.common.model.UserInfoModel
import com.dataqin.common.model.UserModel
import com.dataqin.common.utils.analysis.GsonUtil.jsonToObj
import com.dataqin.common.utils.analysis.GsonUtil.objToJson
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

    //存储用户对象
    @JvmStatic
    fun setUserModel(model: UserModel?) {
        if (null != model) {
            mmkv.encode(Constants.KEY_USER_MODEL, objToJson(model))
        }
    }

    //获取用户对象
    @JvmStatic
    fun getUserModel(): UserModel? {
        var model: UserModel? = null
        val json = mmkv.decodeString(Constants.KEY_USER_MODEL)
        if (!TextUtils.isEmpty(json)) {
            model = jsonToObj(json, UserModel::class.java)
        }
        return model
    }

    //用户是否登陆
    @JvmStatic
    fun isLogin(): Boolean {
        val model = getUserModel()
        if (null != model) {
            return !TextUtils.isEmpty(model.token)
        }
        return false
    }

    //用户认证状态1-未认证,2-已提交,3-认证完成,4-认证失败
    @JvmStatic
    fun getUserStatus(): String? {
        var userStatus: String? = null
        val model = getUserModel()
        if (null != model) {
            userStatus = model.userStatus
        }
        return userStatus
    }

    //(当前的用户状态为usermodel中存的值，并非是userinfomodel中存的，在set存userinfomodel中加载覆盖存的方法)
    @JvmStatic
    fun setUserStatus(userStatus: String?) {
        val model = getUserModel()
        if (null != model) {
            model.userStatus = userStatus
        }
        setUserModel(model)
    }

    @JvmStatic
    fun getUserId(): String? {
        var userId: String? = null
        val model = getUserModel()
        if (null != model) {
            userId = model.userId
        }
        return userId
    }

    @JvmStatic
    fun setUserId(userId: String?) {
        val model = getUserModel()
        if (null != model) {
            model.userId = userId
        }
        setUserModel(model)
    }

    @JvmStatic
    fun setStatus(status: String?) {
        val model = getUserModel()
        if (null != model) {
            model.status = status
        }
        setUserModel(model)
    }

    //用户token
    @JvmStatic
    fun getToken(): String? {
        var token: String? = null
        val model = getUserModel()
        if (null != model) {
            token = model.token
        }
        return token
    }

    //存储用户信息对象
    @JvmStatic
    fun setUserInfoModel(model: UserInfoModel?) {
        if (null != model) {
            setStatus(model.status)
            setUserStatus(model.userStatus)
            setUserId(model.userId)
            mmkv.encode(Constants.KEY_USER_INFO_MODEL, objToJson(model))
        }
    }

    //获取用户信息对象
    @JvmStatic
    fun getUserInfoModel(): UserInfoModel? {
        var model: UserInfoModel? = null
        val json = mmkv.decodeString(Constants.KEY_USER_INFO_MODEL)
        if (!TextUtils.isEmpty(json)) {
            model = jsonToObj(json, UserInfoModel::class.java)
        }
        return model
    }

    //获取手机号
    @JvmStatic
    fun getMobileReserve(): String? {
        var mobileReserve: String? = null
        val model = getUserInfoModel()
        if (null != model) {
            mobileReserve = model.mobileReserve
        }
        return mobileReserve
    }

    //区别公司2和个人1
    @JvmStatic
    fun getStatus(): String? {
        var status: String? = null
        val model = getUserModel()
        if (null != model) {
            status = model.status
        }
        return status
    }

    //获取积分
    @JvmStatic
    fun getPoints(): String {
        var points = "0"
        val model = getUserInfoModel()
        if (null != model) {
            points = if (TextUtils.isEmpty(model.points)) "0" else model.points!!
        }
        return points
    }

    //用户注销操作（清除信息,清除用户凭证）
    @JvmStatic
    fun signOut() {
        mmkv.encode(Constants.KEY_USER_MODEL, "")
        mmkv.encode(Constants.KEY_USER_INFO_MODEL, "")
        LiveDataBus.instance.post(LiveDataEvent(Constants.APP_USER_LOGIN_OUT))
    }

}