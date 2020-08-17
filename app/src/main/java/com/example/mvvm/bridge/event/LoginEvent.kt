package com.example.mvvm.bridge.event

import android.text.TextWatcher
import android.view.View
import com.example.common.base.proxy.SimpleTextWatcher
import com.example.mvvm.R
import com.example.mvvm.activity.LoginActivity

/**
 * Created by WangYanBin on 2020/8/17.
 */
class LoginEvent : LoginActivity() {

    var textWatcher: TextWatcher = object : SimpleTextWatcher() {

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            binding.btnLogin.isEnabled = !isEmpty(
                getParameters(binding.etAccount),
                getParameters(binding.etPassword)
            )
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login ->
                viewModel.login(getParameters(binding?.etAccount)!!, getParameters(binding?.etPassword)!!)
//                viewModel.getData()
        }
    }

}