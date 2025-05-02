package com.example.debugging.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.config.ServerConfig
import com.example.debugging.BR
import com.example.debugging.R
import com.example.debugging.adapter.LogAdapter
import com.example.debugging.databinding.ActivityLogBinding
import com.example.debugging.utils.DebuggingUtil.requestList
import com.example.framework.utils.function.view.clicks

/**
 * 测试用
 */
class LogActivity : BaseTitleActivity<ActivityLogBinding>(), OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleBuilder.setTitle("请求日志").setRight("清空日志"){
            mBinding?.adapter?.refresh(emptyList())
            requestList.clear()
        }
        mBinding?.setVariable(BR.adapter, LogAdapter())
        refreshUrl()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvServer, mBinding?.tvDefault)
        mBinding?.adapter?.setOnItemClickListener { t, _ ->
            startActivity(LogDetailActivity::class.java,Extra.BUNDLE_BEAN to t)
        }
    }

    override fun initData() {
        super.initData()
        mBinding?.adapter?.refresh(requestList)
    }

    /**
     * 显示当前服务器
     */
    private fun refreshUrl() {
        val test = ServerConfig.serverBean()
        mBinding?.tvServer?.text = test.getUrl()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_server -> {
//                ServerDialog(this, {
//                    tv_server.text = it
//                }).show()
            }
            R.id.tv_default -> {
                refreshUrl()
            }
        }
    }

}