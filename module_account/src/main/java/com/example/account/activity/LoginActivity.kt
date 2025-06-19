package com.example.account.activity

import android.os.Bundle
import android.util.LayoutDirection
import android.view.animation.AnimationUtils
import androidx.core.text.TextUtilsCompat
import com.example.account.R
import com.example.account.databinding.ActivityLoginBinding
import com.example.common.base.BaseActivity
import java.util.Locale

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    /**
     * 背景微动画
     */
    private fun startBgAnimation() {
        val animId = if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
            R.anim.set_login_translate_rtl
        } else {
            R.anim.set_login_translate
        }
        mBinding?.ivLoginBg?.startAnimation(AnimationUtils.loadAnimation(this, animId))
    }
}