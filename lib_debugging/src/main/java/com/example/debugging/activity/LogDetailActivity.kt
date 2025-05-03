package com.example.debugging.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.utils.builder.copy
import com.example.debugging.BR
import com.example.debugging.R
import com.example.debugging.bean.RequestBean
import com.example.debugging.databinding.ActivityLogDetailBinding
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.view.clicks

/**
 * 测试用
 */
class LogDetailActivity : BaseTitleActivity<ActivityLogDetailBinding>(), OnClickListener {
    private val bean by lazy { intentParcelable<RequestBean>(Extra.BUNDLE_BEAN) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleBuilder.setTitle("日志详情")
        mBinding?.setVariable(BR.bean, bean)
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvUrl, mBinding?.tvHeader, mBinding?.tvParams, mBinding?.tvBody)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_url -> mBinding?.bean?.url?.copy("url")
            R.id.tv_header -> mBinding?.bean?.header.copy("header")
            R.id.tv_params -> mBinding?.bean?.params.copy("params")
            R.id.tv_body -> mBinding?.bean?.body.copy("body")
        }
    }

}