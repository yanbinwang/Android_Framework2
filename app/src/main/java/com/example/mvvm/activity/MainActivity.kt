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