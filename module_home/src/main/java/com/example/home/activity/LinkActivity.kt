package com.example.home.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.BaseApplication
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.config.ARouterPath
import com.example.common.config.ARouterPath.MainActivity
import com.example.common.utils.ScreenUtil
import com.example.common.utils.manager.AppManager
import com.example.framework.utils.function.getIntent
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.value.second
import com.example.home.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @description 推送處理的activity
 * android:exported="true"
 * android:theme="@style/LinkTheme"
 * <style name="LinkTheme" parent="TransTheme">
 * <item name="android:windowFullscreen">true</item>
 * </style>
 * @author yan
 */
@Route(path = ARouterPath.LinkActivity)
class LinkActivity : BaseActivity<Nothing>() {
    private val source by lazy { intentString(Extra.SOURCE) }
    private var timeOutJob: Job? = null

    companion object {
        //push信息用的intent
        @JvmStatic
        fun byPush(context: Context, vararg pairs: Pair<String, Any>): Intent {
            (context as? BaseActivity<*>)?.overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
            return context.getIntent(LinkActivity::class.java, Extra.SOURCE to "push", *pairs)
        }

        //正常启动
        @JvmStatic
        fun start(context: Context, vararg pairs: Pair<String, Any>) {
            (context as? BaseActivity<*>)?.overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
            context.startActivity(LinkActivity::class.java, Extra.SOURCE to "normal", *pairs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        requestedOrientation = if (Build.VERSION.SDK_INT == 26) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        //預留3s的關閉時間
        setTimeOut()
        //處理推送透傳信息
        onLink()
    }

    override fun isBindingEnabled() = false

    override fun finish() {
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        super.finish()
    }

    private fun onLink() {
        //只要是推送，全局开启onFinish监听，拉起首页
        BaseApplication.needOpenHome = true
        when (source) {
            //推送消息
            "push" -> {
//                if (!handlePush(this)) {
//                    navigation(MainActivity)
//                }
                finish()
            }
            //其他情况统一走firebase处理
//            else -> handleDeepLink(this) { finish() }
            else -> finish()
        }
    }

    private fun setTimeOut() {
        timeOutJob?.cancel()
        timeOutJob = launch {
            delay(3.second)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeOutJob?.cancel()
    }

}