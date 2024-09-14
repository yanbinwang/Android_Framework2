package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.mvvm.databinding.ActivityMainBinding

/**
 * 首页
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val STREAM_TYPES = arrayOf("Video-Audio", "Audio", "Import", "Screen")

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //从服务器或本地指定一个推流地址-》rtmp/srt/quic打头
        val publishUrl = "rtmp://dddddd"
        if (publishUrl.startsWith("rtmp")) {
            mBinding?.rbTransferRtmp?.setChecked(true)
        } else if (publishUrl.startsWith("srt")) {
            mBinding?.rbTransferSrt?.setChecked(true)
        } else {
            mBinding?.rbTransferQuic?.setChecked(true)
        }
        initStreamTypeSpinner()
    }

    private fun initStreamTypeSpinner() {
        mBinding?.spStreamTypes?.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            STREAM_TYPES
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        mBinding?.spStreamTypes?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}