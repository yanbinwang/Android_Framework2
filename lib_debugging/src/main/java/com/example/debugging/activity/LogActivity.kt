package com.example.debugging.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseTitleActivity
import com.example.common.utils.function.orNoData
import com.example.debugging.BR
import com.example.debugging.R
import com.example.debugging.adapter.LogAdapter
import com.example.debugging.bean.ExtraInput
import com.example.debugging.databinding.ActivityLogBinding
import com.example.debugging.widget.dialog.InputDialog
import com.example.debugging.widget.dialog.ListSelectDialog
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.clicks

/**
 * 测试用
 */
class LogActivity : BaseTitleActivity<ActivityLogBinding>(), OnClickListener {
    private val select by lazy {
        ListSelectDialog(this).apply {
            isNotify = false
        }
    }
    private val input by lazy { InputDialog(this) }

    companion object {
        private val extraInputList by lazy { arrayListOf(ExtraInput("清空日志", type = 1)) }

        @JvmStatic
        fun addExtraInput(extraInput: ExtraInput) {
            extraInputList.add(extraInput)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleBuilder.setTitle("请求日志")
        if (extraInputList.isNotEmpty()) {
            if (extraInputList.safeSize == 1) {
                titleBuilder.setRight(extraInputList.first().describe.orNoData()) {
                    showInputDialog(extraInputList.safeGet(0))
                }
            } else {
                titleBuilder.setRight("更多") {
                    select.show()
                }
            }
        }
        mBinding?.setVariable(BR.adapter, LogAdapter())

    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvServer, mBinding?.tvDefault)
        mBinding?.adapter?.setOnItemClickListener { t, position ->
//            startActivity()
        }
        select.setOnItemClickListener { t, position ->
            t?.let {
                showInputDialog(extraInputList.safeGet(position))
            }
        }
    }

    override fun initData() {
        super.initData()
        select.setParams(extraInputList.toNewList { it.describe })
    }

    private fun showInputDialog(extra: ExtraInput?) {
        extra ?: return
        if (extra.type == 1) {
            mBinding?.adapter?.refresh(emptyList())
//            BaseData.requestList.clear()
        } else {
            input.apply {
                setDefaultText(extra.nowValue?.invoke())
                setOnItemClickListener({
                    extra.onInput?.invoke(it?.text.toString())
                    true
                }, {
                    extra.defaultValue?.invoke().orEmpty()
                })
            }.show()
        }
    }

    /**
     * 显示当前服务器
     */
    private fun refreshUrl() {
//        val test = SERVER_TEST ?: serverBean()
//        tv_server.text = test.getUrl()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_server -> {
//                ServerDialog(this, {
//                    tv_server.text = it
//                }).show()
            }

            R.id.tv_default -> {
//                ::SERVER_TEST.del()
//                refreshUrl()
            }
        }
    }

}