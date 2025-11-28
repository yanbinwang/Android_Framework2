package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.common.BaseApplication.Companion.needOpenHome
import com.example.common.base.BaseActivity
import com.example.common.bean.ServerLanguage
import com.example.common.config.Constants.LANGUAGE_LIST
import com.example.common.config.RouterPath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.I18nUtil.getLocalLanguageBean
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.margin
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.MainIndicator
import com.therouter.router.Route
import kotlinx.coroutines.launch

/**
 * 首页
 */
@Route(path = RouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {
    private val indicator by lazy { MainIndicator(this, mBinding?.tbIndicator) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding?.tvTw.margin(top = getStatusBarHeight() + 10.pt)
        indicator.init()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvTw, mBinding?.tvUs, mBinding?.tvId)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_tw -> getLanguageMap(LANGUAGE_LIST.safeGet(0))
            R.id.tv_us -> getLanguageMap(LANGUAGE_LIST.safeGet(1))
            R.id.tv_id -> getLanguageMap(LANGUAGE_LIST.safeGet(2))
        }
    }

    private fun getLanguageMap(bean: ServerLanguage?) {
        bean ?: return
        val language = bean.language ?: return
        launch {
            val localPack = getLocalLanguageBean(bean.language)
            localPack?.let { result ->
                result.version = bean.version
                I18nUtil.setLanguagePack(language, result)
                "语言切换成功".shortToast()
            }
        }
    }

    override fun onResume() {
        if (needOpenHome.get()) needOpenHome.set(false)
        super.onResume()
    }

}