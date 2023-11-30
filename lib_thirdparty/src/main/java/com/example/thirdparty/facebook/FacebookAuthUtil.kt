package com.example.thirdparty.facebook

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.common.BaseApplication
import com.example.common.base.BaseActivity
import com.example.common.utils.function.toJsonString
import com.example.common.utils.function.toObj
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.second
import com.example.framework.utils.logE
import com.example.framework.utils.logWTF
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.R
import com.facebook.appevents.AppEventsLogger
import com.facebook.bolts.Task.Companion.delay
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * facebook三方登录
 */
class FacebookAuthUtil(private val activity: BaseActivity<*>) : LifecycleEventObserver {
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val loginManager by lazy { LoginManager.getInstance() }
    private var success: (account: FacebookInfoBean) -> Unit = {}
    private var cancel: () -> Unit = {}
    private var fail: () -> Unit = {}

    companion object {
        val permissions = listOf(
            "email", "user_likes", "user_status",
            "user_photos", "user_birthday", "public_profile", "user_friends"
        )

        val facebookLogger by lazy { AppEventsLogger.newLogger(BaseApplication.instance) }
    }

    init {
        activity.lifecycle.addObserver(this)
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val accessToken = result.accessToken
                requestData(accessToken)
            }

            override fun onCancel() {
                R.string.authCancel.shortToast()
                cancel()
            }

            override fun onError(error: FacebookException) {
                R.string.authError.shortToast()
                fail()
                error.logE
            }
        })
        disconnectFromFacebook()
    }


    fun signIn(success: (account: FacebookInfoBean) -> Unit, cancel: () -> Unit, fail: () -> Unit) {
        this.success = success
        this.cancel = cancel
        this.fail = fail
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken?.isExpired != false) {
            loginManager.logInWithReadPermissions(activity, permissions)
        } else {
            requestData(accessToken)
        }
        activity.setOnActivityResultListener { requestCode, resultCode, data ->
            val result = callbackManager.onActivityResult(requestCode, resultCode, data)
            activity.clearOnActivityResultListener()
            result
        }
    }

    private var reqStartTime = 0L
    private val reqTimeout = 5.second
    private var job: Job? = null
    private fun requestData(accessToken: AccessToken) {
        val request = GraphRequest
            .newMeRequest(accessToken) { json: JSONObject?, response: GraphResponse? ->
                //超时不作处理
                if (currentTimeNano - reqStartTime > reqTimeout) return@newMeRequest
                "Facebook:\n${json.toJsonString()}".logWTF
                //成功则取消计时Job
                job?.cancel()
                if (json == null) {
                    R.string.authError.shortToast()
                    fail()
                    return@newMeRequest
                }
//                success(gson.fromJson(json.toString(), FacebookInfoBean::class.java))
                val bean = json.toString().toObj(FacebookInfoBean::class.java)
                bean?.facebookToken = accessToken.token
                success(bean)
            }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,link,gender,birthday,email,picture")
        request.parameters = parameters
        job = GlobalScope.launch {
            reqStartTime = currentTimeNano
            request.executeAsync()
            delay(reqTimeout)
            fail()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {

    }

    /**
     * 退出Facebook
     */
    private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return
        }
        loginManager.logOut()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                job?.cancel()
                activity.clearOnActivityResultListener()
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }
}