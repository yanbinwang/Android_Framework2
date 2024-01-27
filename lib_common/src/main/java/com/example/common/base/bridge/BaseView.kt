package com.example.common.base.bridge

import android.app.Activity
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.listener.OnPageChangedListener
import com.app.hubert.guide.model.GuidePage

/**
 * Created by WangYanBin on 2020/6/8.
 * 控件操作
 */
interface BaseView {

    /**
     * 刷新动画dialog
     * 如果设置了second，flag可改为true
     */
    fun showDialog(flag: Boolean = false, second: Long = -1L, block: () -> Unit = {})

    /**
     * 隐藏刷新球控件
     */
    fun hideDialog()

    /**
     * 遮罩引导
     * https://www.jianshu.com/p/f28603e59318
     * ConfigHelper.showGuide(this,"sdd",GuidePage
     *  .newInstance()
     *  .addHighLight(binding.btnList)
     *  .setBackgroundColor(color(R.color.bgOverlay))//可不写，默认赋值了遮罩颜色
     *  .setLayoutRes(R.layout.view_guide_simple)
     *  .setOnLayoutInflatedListener(object :OnLayoutInflatedListener{
     *  override fun onLayoutInflated(view: View?, controller: Controller?) {
     *  val hand = view?.findViewById<ImageView>(R.id.iv_hand)
     *  hand.margin(top = getStatusBarHeight() + 80.pt + 80.pt)
     *  }})
     *  with方法可以传入Activity或者Fragment，获取引导页的依附者。Fragment中使用建议传入fragment，内部会添加监听，当依附的Fragment销毁时，引导层自动消失。
     *  setLabel方法用于设置引导页的标签，区别不同的引导页，该方法必须调用设置，否则会抛出异常。内部使用该label控制引导页的显示次数。
     *  addGuidePage方法添加一页引导页，这里的引导层可以有多个引导页，但至少需要一页。
     *  GuidePage即为引导页对象，表示一页引导页，可以通过.newInstance()创建对象。并通过addHighLight添加一个或多个需要高亮的view，该方法有多个重载，可以设置高亮的形状，以及padding等（默认是矩形）。setLayoutRes方法用于引导页说明布局，就是上图的说明文字的布局。
     *  show方法直接显示引导层，如果不想马上显示可以使用build方法返回一个Controller对象，完成构建。需要显示得时候再次调用Controller对象的show方法进行显示。
     *  setOnLayoutInflatedListener可操作插入view的坐标和显示位置
     */
    fun showGuide(label: String, isOnly: Boolean = true, vararg pages: GuidePage, guideListener: OnGuideChangedListener? = null, pageListener: OnPageChangedListener? = null)

    /**
     * 路由跳转
     * params->页面参数类，跳转的参数，刷新页面页数操作
     */
    fun navigation(path: String, vararg params: Pair<String, Any?>?): Activity

}