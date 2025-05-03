package com.example.debugging.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseDialog
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.debugging.R
import com.example.debugging.databinding.ViewDialogServerInsertBinding
import com.example.debugging.utils.ServerUtil.addServer
import com.example.debugging.utils.ServerUtil.serverData
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.OnMultiTextWatcher
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.text

/**
 * 插入服务器
 */
@SuppressLint("SetTextI18n")
class ServerInsertDialog(context: Context) : BaseDialog<ViewDialogServerInsertBinding>(context), OnClickListener, OnMultiTextWatcher, EditTextImpl {

    init {
        clicks(mBinding?.tvScheme, mBinding?.tvCancel, mBinding?.tvSure)
        textWatchers(mBinding?.etServer, mBinding?.etPort, mBinding?.etPath)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        super.onTextChanged(s, start, before, count)
        changed()
    }

    override fun shown(flag: Boolean) {
        super.shown(flag)
        val data = serverData()
        val serverType = data.first
        val serverList = data.second
        val serverBean = serverList.safeGet(serverType)
        mBinding?.tvScheme?.text = if (serverBean?.https.orFalse) "https://" else "http://"
        mBinding?.etServer?.setText(serverBean?.server.orEmpty())
        mBinding?.etPort?.setText(serverBean?.port.orZero.toString())
        mBinding?.etPath?.setText(serverBean?.path.orEmpty())
        changed()
    }

    private fun changed() {
        val builder = StringBuilder()
        builder.apply {
            append(mBinding?.tvScheme.text())
            append(mBinding?.etServer.text())
            val port = mBinding?.etPort.text()
            if (port.isNotEmpty() && port != "0") {
                append(":")
                append(port)
            }
            if (mBinding?.etPath.text().isNotEmpty()) {
                append("/")
                append(mBinding?.etPath.text())
            }
            append("/")
        }
        mBinding?.tvContent?.text = builder.toString()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_scheme -> {
                when (mBinding?.tvScheme.text()) {
                    "http://" -> mBinding?.tvScheme?.text = "https://"
                    else -> mBinding?.tvScheme?.text = "http://"
                }
                changed()
            }
            R.id.tv_cancel -> dismiss()
            R.id.tv_sure -> {
                val port = if (mBinding?.etPort.text().isEmpty()) 0 else mBinding?.etPort.text().toSafeInt()
                val https = when (mBinding?.tvScheme.text()) {
                    "https://" -> true
                    else -> false
                }
                addServer(mBinding?.etServer.text(), port, mBinding?.etPath.text(), "自定义", https)
                dismiss()
            }
        }
    }

}