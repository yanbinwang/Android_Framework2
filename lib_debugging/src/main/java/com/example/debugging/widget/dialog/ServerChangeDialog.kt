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
import com.example.common.utils.function.pt
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.debugging.R
import com.example.debugging.databinding.ViewDialogServerChangeBinding
import com.example.debugging.utils.ServerUtil.changeServer
import com.example.debugging.utils.ServerUtil.serverData
import com.example.framework.utils.function.view.checkedIndex
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize

/**
 * 切换服务器
 */
@SuppressLint("SetTextI18n")
class ServerChangeDialog(context: Context) : BaseDialog<ViewDialogServerChangeBinding>(context), OnClickListener, EditTextImpl {
    private var onConfirm: ((ServerBean?) -> Unit)? = null

    init {
        clicks(mBinding?.tvCancel, mBinding?.tvSure)
    }

    override fun shown(flag: Boolean) {
        super.shown(flag)
        val data = serverData()
        val serverList = data.second
        mBinding?.rgGroup?.removeAllViews()
        serverList.forEachIndexed { _, bean ->
            val button = RadioButton(context)
            button.size(MATCH_PARENT, WRAP_CONTENT)
            button.textColor(R.color.textPrimary)
            button.textSize(R.dimen.textSize12)
            button.text = bean.getUrl()
            mBinding?.rgGroup?.addView(button)
            button.margin(top = 2.pt)
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
                val index = mBinding?.rgGroup.checkedIndex()
                val bean = changeServer(index)
                onConfirm?.invoke(bean)
                dismiss()
            }
        }
    }

}