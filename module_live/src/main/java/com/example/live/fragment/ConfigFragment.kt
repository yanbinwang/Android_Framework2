package com.example.live.fragment

import android.content.Intent
import androidx.databinding.ViewDataBinding
import com.example.common.base.BaseFragment

abstract class ConfigFragment<VDB : ViewDataBinding> : BaseFragment<VDB>() {

    abstract fun getIntent(): Intent

}