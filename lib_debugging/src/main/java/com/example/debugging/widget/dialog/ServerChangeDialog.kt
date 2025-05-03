package com.example.debugging.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RadioButton
import com.example.common.base.BaseDialog
import com.example.common.bean.ServerBean
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.debugging.R
import com.example.debugging.databinding.ViewDialogServerChangeBinding
import com.example.debugging.databinding.ViewDialogServerInsertBinding
import com.example.debugging.utils.ServerUtil.addServer
import com.example.debugging.utils.ServerUtil.changeServer
import com.example.debugging.utils.ServerUtil.serverData
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.OnMultiTextWatcher
import com.example.framework.utils.function.view.checked
import com.example.framework.utils.function.view.checkedIndex
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.text
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize

/**
 * 切换服务器
 */
@SuppressLint("SetTextI18n")
class ServerChangeDialog(context: Context) : BaseDialog<ViewDialogServerChangeBinding>(context), OnClickListener, EditTextImpl {
    private var serverBean: ServerBean?=null
    private var onConfirm: ((ServerBean?) -> Unit)? = null

    init {
        clicks(mBinding?.tvCancel, mBinding?.tvSure)
    }

    override fun shown(flag: Boolean) {
        super.shown(flag)
        val data = serverData()
        val serverType = data.first
        val serverList = data.second
        serverBean = serverList.safeGet(serverType)
        mBinding?.rgGroup?.removeAllViews()
        serverList.forEachIndexed { index, bean ->
            val button = RadioButton(context)
            if (index == serverType) button.checked(true)
            button.size(MATCH_PARENT, WRAP_CONTENT)
            button.textColor(R.color.textPrimary)
            button.textSize(R.dimen.textSize13)
            button.text = bean.getUrl()
            mBinding?.rgGroup?.addView(button)
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    serverBean = serverList.safeGet(mBinding?.rgGroup.checkedIndex())
                }
            }
        }
    }

    fun setDialogListener(onConfirm: (ServerBean?) -> Unit = {}) {
        this.onConfirm = onConfirm
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_cancel -> dismiss()
            R.id.tv_sure -> {
                //获取当前选中的地址下标
                changeServer(mBinding?.rgGroup.checkedIndex())
                dismiss()
            }
        }
    }

}