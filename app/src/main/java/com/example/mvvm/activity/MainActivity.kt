package com.example.mvvm.activity

import android.annotation.SuppressLint
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.bean.ServerLanguage
import com.example.common.config.ARouterPath
import com.example.common.config.Constants.LANGUAGE_LIST
import com.example.common.event.Event
import com.example.common.event.EventCode.EVENT_LANGUAGE_CHANGE
import com.example.common.utils.NightModeUtil
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.I18nUtil.getLocalLanguageBean
import com.example.common.utils.i18n.LanguageUtil
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.padding
import com.example.mvvm.BR
import com.example.mvvm.adapter.LanguageAdapter
import com.example.mvvm.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n", "NotifyDataSetChanged")
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun isEventBusEnabled() = true

    override fun Event.onEvent() {
        isEvent(EVENT_LANGUAGE_CHANGE) {
            refreshLanguage()
        }
    }

    override fun initView() {
        super.initView()
        binding.flTitle.padding(top = getStatusBarHeight())
        binding.setVariable(BR.adapter, LanguageAdapter())
        binding.adapter?.notify(LANGUAGE_LIST)//接口请求之后
        refreshLanguage()
    }

    override fun initEvent() {
        super.initEvent()
        binding.tvModeSwitcher.click { NightModeUtil.toggleNightMode() }
        binding.adapter?.setOnItemClickListener { t, _ -> getServerLanguage(t) }
    }

    private fun refreshLanguage() {
        val checkedBean = LANGUAGE_LIST.find { it.language == LanguageUtil.getLanguage() }
        binding.tvLanguage.text = checkedBean?.name
        binding.tvHeader.text = checkedBean?.language
        binding.adapter?.notifyDataSetChanged()
    }

    private fun getServerLanguage(bean: ServerLanguage?) {
        bean ?: return
        val name = bean.language ?: return
        launch {
            val localPack = getLocalLanguageBean(bean.language)
            localPack?.let {
                it.version = bean.version.orZero.toString()
                I18nUtil.setLanguagePack(name, it)
//                string(R.string.languageChangeSuccess).shortToast()
//                finish()
            }
        }
    }

}