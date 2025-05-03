package com.example.debugging.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.config.ServerConfig
import com.example.common.utils.builder.shortToast
import com.example.debugging.BR
import com.example.debugging.R
import com.example.debugging.adapter.LogAdapter
import com.example.debugging.databinding.ActivityLogBinding
import com.example.debugging.utils.ServerUtil.requestList
import com.example.debugging.utils.ServerUtil.resetServer
import com.example.debugging.widget.dialog.ServerChangeDialog
import com.example.debugging.widget.dialog.ServerInsertDialog
import com.example.framework.utils.function.view.clicks

/**
 * 测试用
 */
class LogActivity : BaseTitleActivity<ActivityLogBinding>(), OnClickListener {
    private val insert by lazy { ServerInsertDialog(this) }
    private val change by lazy { ServerChangeDialog(this) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleBuilder.setTitle("请求日志").setRight("清空日志") {
            mBinding?.adapter?.refresh(emptyList())
            requestList.get().clear()
        }
        mBinding?.setVariable(BR.adapter, LogAdapter())
        refreshUrl()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvServer, mBinding?.tvInsert, mBinding?.tvRefresh, mBinding?.tvReset)
        change.setDialogListener {
            mBinding?.tvServer?.text = it?.getUrl()
        }
        mBinding?.adapter?.setOnItemClickListener { t, _ ->
            startActivity(LogDetailActivity::class.java, Extra.BUNDLE_BEAN to t)
        }
    }

    override fun initData() {
        super.initData()
        mBinding?.adapter?.refresh(requestList.get())
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
            //切换当前配置的请求地址
            R.id.tv_server -> {
                change.shown()
            }
            //添加自定义的请求地址
            R.id.tv_insert -> {
                insert.shown()
            }
            //刷新列表->当前页面的数据并不是实时的，频繁获取损耗性能开销，改为手动刷新
            R.id.tv_refresh -> {
                if (mBinding?.adapter?.list() != requestList.get()) {
                    "刷新成功".shortToast()
                } else {
                    "未收到新的请求".shortToast()
                }
            }
            //还原为最初的几个配置的请求地址
            R.id.tv_reset -> {
                resetServer()
                refreshUrl()
                "复位成功".shortToast()
            }
        }
    }

}