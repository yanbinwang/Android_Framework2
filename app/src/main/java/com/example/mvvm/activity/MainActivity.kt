package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.config.Constants
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.click
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.automatic.AutomaticBean
import com.example.mvvm.widget.automatic.AutomaticBuilder
import com.example.mvvm.widget.dialog.TestDialog
import com.example.mvvm.widget.popup.TestPopup


@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val testDialog by lazy { TestDialog(this) }
//    private val testPopup by lazy { TestPopup(this) }

    override fun initEvent() {
        super.initEvent()
        val list = listOf(AutomaticBean(0, "key1", "标题1"), AutomaticBean(1, "key2", "标题2"))
        val viewList = list.toNewList { AutomaticBuilder.builder(it).build(this) }
        binding.llContainer.removeAllViews()
        viewList.forEach {
            binding.llContainer.addView(it.getView())
        }
       binding.btnFileManager.click { testDialog.shown() }
        "${Constants.APPLICATION_PATH}".logWTF
    }

}