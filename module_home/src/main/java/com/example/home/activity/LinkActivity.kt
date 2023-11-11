package com.example.home.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.example.common.BaseApplication
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra.PAYLOAD
import com.example.common.config.ARouterPath
import com.example.framework.utils.function.getIntent
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.startActivity
import com.example.home.R
import com.example.home.utils.LinkRouterUtil.handleDeepLink
import com.example.home.utils.LinkRouterUtil.handlePush
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
class LinkActivity : BaseActivity<ViewDataBinding>() {
    private val payload by lazy { intentString(PAYLOAD) }
    private var timeOutJob: Job? = null

    companion object {
        //push信息用的intent
        fun byPush(context: Context, vararg pairs: Pair<String, Any>): Intent {
            (context as? BaseActivity<*>)?.overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
            return context.getIntent(LinkActivity::class.java, PAYLOAD to "push", *pairs)
        }

        //正常启动
        fun start(context: Context, vararg pairs: Pair<String, Any>) {
            (context as? BaseActivity<*>)?.overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
            context.startActivity(LinkActivity::class.java, PAYLOAD to "normal", *pairs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        super.onCreate(savedInstanceState)
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

    override fun finish() {
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        super.finish()
    }

    private fun onLink() {
        BaseApplication.needOpenHome = true
        when (payload) {
            //推送消息
            "push" -> {
                if (!handlePush(this)) {
//                    JumpTo.home(this, 0)
                    //拉起首页
                    navigation(ARouterPath.MainActivity)
                }
                finish()
            }
            //其他情况统一走firebase处理
            else -> {
                handleDeepLink(this, { finish() }, { finish() })
            }
        }
    }

    private fun setTimeOut() {
        timeOutJob?.cancel()
        timeOutJob = launch {
            delay(3000)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeOutJob?.cancel()
    }

}