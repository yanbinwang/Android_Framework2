package com.example.mvvm.activity

import android.annotation.SuppressLint
import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.bean.ServerLanguage
import com.example.common.config.ARouterPath
import com.example.common.config.Constants.LANGUAGE_LIST
import com.example.common.event.Event
import com.example.common.event.EventCode.EVENT_LANGUAGE_CHANGE
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.I18nUtil.getLocalLanguageBean
import com.example.common.utils.i18n.LanguageUtil
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.margin
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.MainIndicator
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {
    private val indicator by lazy {
        MainIndicator(binding.tbMain, listOf(
            Triple(R.mipmap.ic_main_home, R.mipmap.ic_main_home_on, R.string.main_home),
            Triple(R.mipmap.ic_main_market, R.mipmap.ic_main_market_on, R.string.main_market),
            Triple(R.mipmap.ic_main_balance, R.mipmap.ic_main_balance_on, R.string.main_balance),
            Triple(R.mipmap.ic_main_user, R.mipmap.ic_main_user_on, R.string.main_user)
        ))
    }

    override fun isEventBusEnabled() = true

    override fun Event.onEvent() {
        isEvent(EVENT_LANGUAGE_CHANGE) {
            refreshLanguage()
        }
    }

    override fun initView() {
        super.initView()
        binding.tvLanguage.margin(top = getStatusBarHeight())
        refreshLanguage()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(binding.tvUs, binding.tvHk, binding.tvIn)
    }

    private fun refreshLanguage() {
        val checkedBean = LANGUAGE_LIST.find { it.language == LanguageUtil.getLanguage() }
        binding.tvLanguage.text = "当前语言：${checkedBean?.name}"
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_us -> getServerLanguage(LANGUAGE_LIST.safeGet(0))
            R.id.tv_hk -> getServerLanguage(LANGUAGE_LIST.safeGet(1))
            R.id.tv_in -> getServerLanguage(LANGUAGE_LIST.safeGet(2))
        }
    }

}