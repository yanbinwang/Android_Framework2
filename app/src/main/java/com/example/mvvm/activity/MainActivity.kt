package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.BaseApplication
import com.example.common.base.BaseActivity
import com.example.common.bean.UserBean
import com.example.common.config.ARouterPath
import com.example.common.utils.file.FileBuilder
import com.example.common.utils.function.drawable
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderMaxDragRate
import com.example.framework.utils.BitmapSpan
import com.example.framework.utils.ColorSpan
import com.example.framework.utils.ImageSpan
import com.example.framework.utils.SizeSpan
import com.example.framework.utils.TextSpan
import com.example.framework.utils.function.color
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.isBottom
import com.example.framework.utils.function.view.isTop
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.rotate
import com.example.framework.utils.function.view.size
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.VideoSnapManager
import com.example.mvvm.viewmodel.TestViewModel
import com.example.mvvm.widget.dialog.TestTopDialog
import com.example.thirdparty.album.AlbumHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *  <data>
 *
 *  <import type="android.view.View" />
 *
 *  <variable
 *  name="kolVisible"
 *  type="Boolean"
 *  android:value="false" />
 *
 *  </data>
 *
 *  <ImageView
 *  android:id="@+id/iv_kol"
 *  android:layout_width="60pt"
 *  android:layout_height="70pt"
 *  android:layout_gravity="right|bottom"
 *  android:layout_marginBottom="154pt"
 *  android:src="@mipmap/ic_suspension"
 *  android:visibility="@{kolVisible==false?View.GONE:View.VISIBLE}" />
 *
 *  //去重
 *  Kotlin 标准库提供了 distinct() 函数，它可以用于删除集合中的所有重复项。
 *  但是，如果你使用的是自定义类（而不是基本数据类型），则需要确保该类正确实现了 equals() 和 hashCode() 函数，以便 distinct() 函数能够正常工作。
 *  data class Person(val name: String, val age: Int)
 *
 *  val listWithDuplicates = listOf(
 *  Person("Alice", 25),
 *  Person("Bob", 30),
 *  Person("Alice", 25),
 *  Person("Charlie", 35)
 *  )
 *  val listWithoutDuplicates = listWithDuplicates.distinct()
 *
 *  override fun equals(other: Any?): Boolean {
 *  if (other == null || other !is QuickPassBean) {
 *  return false
 *  }
 *  return outCoin == other.outCoin
 *  }
 *
 *  override fun hashCode(): Int {
 *  var result = 17
 *  result = 31 * result + outCoin.hashCode()
 *  return result
 *  }
 *
 *  对于两个长度相等且元素类型相同的列表，如果它们都包含具有相同 id 的元素，则可以使用以下代码查找在第二个列表中与第一个列表中元素不同的所有对象，并刷新它们：
 *
 *  kotlin
 *  firstList.forEachIndexed { index, firstItem ->
 *  val secondItem = secondList[index]
 *  if (firstItem.id == secondItem.id && firstItem != secondItem) {
 *  // 找到 id 相同但对象不同的元素，刷新它们
 *  secondList[index] = firstItem
 *  // 此处可以执行其他操作，例如更新 UI 界面等
 *  }
 *  }
 *  需要注意的是，这个方法是基于比较对象的引用，
 *  也就是比较两个对象是否为同一内存地址而不是比较对象内容。
 *  如果您想要比较对象的内容，请确保在对象类中实现 equals() 和 hashCode() 方法，并在比较时使用它们。
 *
 *  data class Book(val title: String, val author: String, val genre: String)
 *
 *  val books = listOf(
 *         Book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy"),
 *         Book("The Two Towers", "J.R.R. Tolkien", "Fantasy"),
 *         Book("The Catcher in the Rye", "J.D. Salinger", "Fiction"),
 *         Book("To Kill a Mockingbird", "Harper Lee", "Fiction")
 *     )
 *
 *     val booksByGenre = books.groupBy { it.genre }
 *     booksByGenre.forEach { (genre, books) ->
 *         println("$genre: ${books.map { it.title }}")
 *     }
 * 这段代码片段将图书按流派组织为一个map，其中键是类型，值是属于这些类型的图书列表。然后，它按类型打印出分组的书籍的标题，让人一眼就可以看到组织结构。
 *
 * https://blog.csdn.net/chuyouyinghe/article/details/137119441
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {
    //    private val illustratePopup by lazy { IllustratePopup(this) }
    private val testBottom by lazy { TestTopDialog() }
    //    private val ids = listOf(R.color.blue_2a3160, R.color.blue_1566ec, R.color.blue_6e7ce2, R.color.blue_aac6f4)
//    private val adapter by lazy { ImageAdapter() }
//    private val halfPosition by lazy { Int.MAX_VALUE / 2 }  //设定一个中心值下标
//    private val map = mapOf("1111" to "一", "2222" to "二", "3333" to "三")
    private val selectList by lazy { listOf("1" to true, "2" to true, "3" to true) }
    private val viewModel by lazy { TestViewModel().create() }
    private val bean by lazy { intentParcelable<UserBean>("bean") }
    private var isOpen = false
    private val builder by lazy { FileBuilder(this) }
    private val album by lazy { AlbumHelper(this) }

    data class Book(val title: String, val author: String, val genre: String)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
//        BaseApplication.instance.initPrivacyAgreed()

        mBinding?.ivArrow.click { isOpen = it.rotate(isOpen) }

        val books = listOf(
            Book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy"),
            Book("The Two Towers", "J.R.R. Tolkien", "Fantasy"),
            Book("The Catcher in the Rye", "J.D. Salinger", "Fiction"),
            Book("To Kill a Mockingbird", "Harper Lee", "Fiction")
        )

        val booksByGenre = books.groupBy { it.genre }
        booksByGenre.forEach { (genre, books) ->
            println("$genre: ${books.map { it.title }}")
        }

//        val numbers = listOf(1, 2, 3, 4, 5)
//        val squares = numbers.map { it * it }
//        println(squares) // 输出：[1, 4, 9, 16, 25]
//        在这个例子中，我们有一个整数列表numbers，我们使用map函数来计算每个数字的平方。it * it是一个lambda表达式，它定义了对每个元素执行的操作。


//        adapter.refresh(ids)
//        binding.rvTest.adapter = adapter
//        binding.rvTest.orientation = ViewPager2.ORIENTATION_VERTICAL
//        binding.rvTest.offscreenPageLimit = ids.safeSize - 1
//        binding.rvTest.setPageTransformer(CardTransformer())
//        binding.rvTest.hideFadingEdge()
//        binding.rvTest.setCurrentItem(
//            if (ids.size > 1) halfPosition - halfPosition % ids.size else 0,
//            false
//        )
//        val numberHelper = NumberEditTextHelper(binding.etTest)
//        numberHelper.setPrecision(2)
//
//
//        binding.btnTest.margin(top = getStatusBarHeight() + 80.pt)
//
//        showGuide("test", GuidePage
//            .newInstance()
//            .addHighLight(binding.btnTest)
//            .setLayoutRes(R.layout.view_guide_simple)
//            .setOnLayoutInflatedListener { view, _ ->
//                val hand = view?.findViewById<ImageView>(R.id.iv_hand)
//                hand.margin(top = getStatusBarHeight() + 80.pt + 80.pt)
//            })
//
//        binding.tvTest.margin(top = getStatusBarHeight() + 80.pt + 80.pt)

//        binding.tvTest.text = "我已阅读《用户协议》和《隐私政策》".setSpanFirst("《用户协议》",ClickSpan(object :XClickableSpan(R.color.appTheme){
//            override fun onLinkClick(widget: View) {
//                "点击用户协议".logWTF
//            }
//        }))


//        binding.tvTest.text = TextSpan()
//            .add("我已阅读《用户协议》和")
//         .add("《隐私政策》",SizeSpan(dimen(R.dimen.textSize10)),ColorSpan(color(R.color.grey_cccccc)),
//             RadiusSpan(RadiusBackgroundSpan(color(R.color.blue_aac6f4),5, 3.pt))
//         )
//        .build()

//        binding.tvTest.text = TextSpan()
//            .add("在Cheezeebit交易，訂單賺取高達", SizeSpan(dimen(R.dimen.textSize14)))
//            .add(
//                " 0.5% ",
//                SizeSpan(dimen(R.dimen.textSize14)),
//                ColorSpan(color(R.color.grey_cccccc))
//            )
//            .add("的訂單獎勵", SizeSpan(dimen(R.dimen.textSize14)))
//            .add("★")
//            .build().setRankSpan(18.pt)
//        binding.tvTest.movementMethod = LinkMovementMethod.getInstance()


        mBinding?.tvTest?.text = TextSpan()
            .add("在Cheezeebit交易，訂單賺取高達", SizeSpan(dimen(R.dimen.textSize14)))
            .add(
                " 0.5% ",
                SizeSpan(dimen(R.dimen.textSize14)),
                ColorSpan(color(R.color.textSecondary))
            )
            .add("的訂單獎勵", SizeSpan(dimen(R.dimen.textSize14)))
            .add("★", BitmapSpan(ImageSpan(drawable(R.mipmap.ic_rank)?.toBitmapOrNull(), 18.pt)))
            .build()


//        binding.tvTest.setClickSpan(
//            "我已阅读《用户协议》和《隐私政策》",
//            "《用户协议》",
//            R.color.appTheme
//        ) { "点击用户协议".logWTF }
//binding.tvTest.click {  }
//        class a(func:(a:Int,b:Int,c:Int)-> BigDecimal)
//
//        fun test(){
//            a{a,b,c->
//                BigDecimal.ONE
//            }
//            a{a,b,c->
//                BigDecimal.ONE
//            }
//        }
//        binding.tvTest.text = TextSpan()
//            .add("$", SizeSpan(dimen(R.dimen.textSize14)))
//            .add("111", ColorSpan(color(R.color.black)))
//            .build()
        //判断是全角字符  \u0020为半角空格，\u3000为全角空格
//        "${"是".regCheck("[^\\x00-\\xff]")}".logWTF

        val recycler = RecyclerView(this)
        val videoSnapManager = VideoSnapManager(this, OrientationHelper.VERTICAL, false)
        recycler.layoutManager = videoSnapManager
//        recycler.adapter
        videoSnapManager.setOnViewPagerListener(object : VideoSnapManager.OnViewPagerListener {
            override fun onPageRelease(isNest: Boolean, itemView: View) {
//                releaseVideo(itemView)
            }

            override fun onPageSelected(isBottom: Boolean, itemView: View) {
//                playVideo(itemView)
            }
        })
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //当前处于停止滑动
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (list.safeSize()<2) return
                    if (recyclerView.isTop()) {

                        return
                    }
                    if (recyclerView.isBottom()) {

                        return
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

//    class TestBean(
//        var key: String? = null,
//        var value: String? = null
//    )

    override fun initEvent() {
        super.initEvent()

        //通过代码动态重置一下顶部的高度
        val bgHeight = 164.pt + getStatusBarHeight()
        mBinding?.ivFundsBg.size(height = bgHeight)
        mBinding?.llFunds.apply {
            size(height = bgHeight)
            padding(top = getStatusBarHeight())
        }
        //全屏的刷新，顶部需要空出导航栏的距离
        mBinding?.refresh.setHeaderMaxDragRate()
        //设置头部的滑动监听
        mBinding?.refresh.setHeaderDragListener { isDragging, percent, offset, height, maxDragHeight ->
            changeBgHeight(offset)
        }
        mBinding?.viewContent.click {
//            mPermission.requestPermissions {
//                result.pullUpAlbum()
//            }
//            album.imageSelection(hasDurban = true) {
//
//            }
//            "dsfdsfdsfds".shortToast()
//            testBottom.show(supportFragmentManager)
//            illustratePopup.showUp(it, "测试文本测试文本测试文本测试文本测试文本测试文本测文本测试文本测试文本测试本测试文本测试文本测试文本本测试文本测试文本测试文本")

//            val view = ViewTestBinding.bind(inflate(R.layout.view_test)).root
//                builder.saveViewJob(view, 100 , onResult ={
//                    "更新相册${it}".shortToast()
//                    insertImageResolver(File(it.orEmpty()))
//                })

        }
        launch {
            delay(2000)
            mBinding?.ivBg?.load("https://images.91fafafa.com/upload/image/banner/banner.png")
        }

    }

    /**
     * 滑动时改变对应的图片高度
     */
    private fun changeBgHeight(offset: Int) {
        val imgBgHeight = mBinding?.llFunds?.measuredHeight.orZero
        if (imgBgHeight <= 0) return
        //设置视图围绕其旋转和缩放的点的 y 位置。默认情况下，枢轴点以对象为中心。设置此属性会禁用此行为并导致视图仅使用显式设置的 pivotX 和 pivotY 值。
        mBinding?.ivFundsBg?.pivotY = 0f
        //设置视图围绕轴心点在 Y 轴上缩放的量，作为视图未缩放宽度的比例。值为 1 表示不应用缩放。
        mBinding?.ivFundsBg?.scaleY = offset.toSafeFloat() / imgBgHeight.toSafeFloat() + 1f
    }

    /**
     * list1为服务器中数据
     * list2为本地存储数据
     * isDuplicate:是否返回重复的或不重复的数据
     * 正向查为服务器新增数据
     * 反向查为本地删除数据
     */
    private fun <T> List<T>?.filter(
        list: List<T>,
        isDuplicate: Boolean = false
    ): ArrayList<T>? {
        this ?: return null
        val filterSet = HashSet<T>(this)//将List1转换为Set，去除重复元素
        val duplicateSet = HashSet<T>()//重复的
        val incompleteSet = HashSet<T>()//不重复的
        list.forEach {
            if (filterSet.contains(it)) {
                duplicateSet.add(it)
            } else {
                incompleteSet.add(it)
            }
        }
        return if (isDuplicate) ArrayList(duplicateSet) else ArrayList(incompleteSet)
    }

}