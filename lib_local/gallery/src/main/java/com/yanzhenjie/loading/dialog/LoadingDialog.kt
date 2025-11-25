package com.yanzhenjie.loading.dialog

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.example.gallery.R
import com.yanzhenjie.loading.LoadingView

/**
 * 加载动画Dialog
 * Created by yan
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.loadingDialog_Loading) {
    private val mLoadingView = findViewById<LoadingView>(R.id.loading_view)
    private val mTvMessage = findViewById<TextView>(R.id.loading_tv_message)

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.loading_wait_dialog)
    }

    fun setCircleColors(r1: Int, r2: Int, r3: Int) {
        mLoadingView.setCircleColors(r1, r2, r3)
    }

    fun setMessage(resId: Int) {
        mTvMessage.setText(resId)
    }

    fun setMessage(message: String) {
        mTvMessage.text = message
    }

}