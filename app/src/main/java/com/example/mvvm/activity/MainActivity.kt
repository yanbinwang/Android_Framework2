package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.dialog.TestDialog


@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val testDialog by lazy { TestDialog(this) }
//    private val testPopup by lazy { TestPopup(this) }

    override fun initEvent() {
        super.initEvent()
//        val list = listOf(AutomaticBean(0, "key1", "标题1"), AutomaticBean(1, "key2", "标题2"))
//        val viewList = list.toNewList { AutomaticBuilder.builder(it).build(this) }
//        binding.llContainer.removeAllViews()
//        viewList.forEach {
//            binding.llContainer.addView(it.getView())
//        }
//        binding.btnFileManager.padding()
//       binding.btnFileManager.click { testDialog.shown() }
//        val list = listOf(AutomaticBean(0, "key1", "标题1"), AutomaticBean(1, "key2", "标题2"))
//        GsonUtil.listToJson(list).logWTF

//        val list =
//            GsonUtil.jsonToList<AutomaticBean>("[{\"key\":\"key1\",\"label\":\"标题1\",\"type\":0},{\"key\":\"key2\",\"label\":\"标题2\",\"type\":1}]")
//        "${list?.size}".logWTF
    }

}