package com.example.account.activity

import android.os.Bundle
import android.util.LayoutDirection
import androidx.core.text.TextUtilsCompat
import com.example.account.R
import com.example.account.databinding.ActivityLoginBinding
import com.example.common.base.BaseActivity
import com.example.framework.utils.function.view.startAnimation
import java.util.Locale

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //背景微动画
        mBinding?.ivLoginBg?.startAnimation(if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
            R.anim.set_login_translate_rtl
        } else {
            R.anim.set_login_translate
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        //activity/fragment中必须清除,防止关闭页面动画还在执行造成内存泄漏
        mBinding?.ivLoginBg?.clearAnimation()
    }

}