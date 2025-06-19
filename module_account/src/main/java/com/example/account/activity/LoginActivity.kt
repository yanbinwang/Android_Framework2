package com.example.account.activity

import android.os.Bundle
import android.util.LayoutDirection
import androidx.core.text.TextUtilsCompat
import com.example.account.R
import com.example.account.databinding.ActivityLoginBinding
import com.example.common.base.BaseActivity
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.startAnimation
import java.util.Locale

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        doOnDestroy {
            mBinding?.ivLoginBg?.clearAnimation()
        }
        //背景微动画
        mBinding?.ivLoginBg?.startAnimation(if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
            R.anim.set_login_translate_rtl
        } else {
            R.anim.set_login_translate
        })
    }

}