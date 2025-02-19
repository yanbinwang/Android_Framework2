package com.example.mvvm.activity

import android.os.Bundle
import androidx.core.graphics.drawable.toBitmapOrNull
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.bean.UserBean
import com.example.common.config.ARouterPath
import com.example.common.utils.file.FileBuilder
import com.example.common.utils.function.drawable
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.toJson
import com.example.common.utils.toList
import com.example.common.utils.toObj
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
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.framework.utils.logE
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
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
 *
 * https://blog.csdn.net/mqdxiaoxiao/article/details/135101003
 * https://gitee.com/tryohang/EdgeTranslucent
 * com.ulive.common.widget.EdgeTransparentView
 *
 * kotlin的===等价于java的==
 * kotlin的==等价于java的equals
 *
 * 数组的比较
 * 使用.contentEquals() -》不比较顺序，顺序不一致内容一致为true  和.contentDeepEquals() -》比较顺序，顺序不一致则内容一致为false 比较两个数组是否具有相同顺序的相同元素
 * 不要使用相等（==）和不等（！=）运算符来比较数组的内容，这些操作符检查指定的变量是否指向同一个对象
 * https://blog.csdn.net/cyclelucky/article/details/135106212
 *
一、基于 Java 并发包 (java.util.concurrent) 的集合
Java 提供了丰富的线程安全集合类，Kotlin 可以直接使用：

1. 线程安全的 List
CopyOnWriteArrayList
特点：写操作复制整个数组，读操作无锁。适用于读多写少的场景（如配置列表、事件监听器列表）。
示例：
import java.util.concurrent.CopyOnWriteArrayList

val list = CopyOnWriteArrayList<Int>()
list.add(1) // 写操作复制数组
for (num in list) { // 读操作无需加锁
println(num)
}
2. 线程安全的 Map
ConcurrentHashMap
特点：分段锁机制，高并发性能，支持原子操作（如 putIfAbsent）。
示例：
import java.util.concurrent.ConcurrentHashMap

val map = ConcurrentHashMap<String, Int>()
map.put("key", 1) // 原子插入
val value = map.getOrDefault("key", 0) // 原子读取
3. 线程安全的 Queue
ConcurrentLinkedQueue
特点：基于链表的实现，无锁算法，适合高并发场景。
BlockingQueue
特点：支持阻塞操作（如 take()、put()），用于生产者-消费者模型。
示例：
import java.util.concurrent.ArrayBlockingQueue

val queue = ArrayBlockingQueue<Int>(10)
queue.put(1) // 阻塞插入
val num = queue.take() // 阻塞取出
4. 线程安全的 Set
ConcurrentHashMap.newKeySet()
特点：基于 ConcurrentHashMap 实现的无序集合，支持高并发操作。
CopyOnWriteArraySet
特点：写操作复制数组，读操作无锁，适用于读多写少场景。
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
    private val builder by lazy { FileBuilder(this) }
    private val album by lazy { AlbumHelper(this) }

    data class Book(val title: String, val author: String, val genre: String)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
//        BaseApplication.instance.initPrivacyAgreed()

        mBinding?.ivArrow.click {
//            it.rotate()
            mBinding?.finder?.onShutter()
        }

        val books = listOf(
            Book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy"),
            Book("The Two Towers", "J.R.R. Tolkien", "Fantasy"),
            Book("The Catcher in the Rye", "J.D. Salinger", "Fiction"),
            Book("To Kill a Mockingbird", "Harper Lee", "Fiction")
        )

        //https://www.cnblogs.com/zhangwenju/p/16658993.html
//        //sortBy 指定以genre属性进行升序排序
//        books.sortedBy { it.genre }
//        //sortedByDescending 指定以genre属性进行降序排序
//        books.sortedByDescending  { it.genre }

        //https://blog.csdn.net/android_cai_niao/article/details/108407853
//        //使用sortWith实现升序排序
//        val userList = mutableListOf(Book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy"), Book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy"))
//        userList.sortWith { u1, u2 ->
//            u1.genre.compareTo(u2.genre)
//        }
//        userList.forEach(::println)
//        //使用sortWith实现降序排序
//        userList.sortWith { u1, u2 ->
//            u2.genre.compareTo(u1.genre)
//        }
//        userList.sortWith { u1, u2 ->
//            if (u1.author != u2.author) {
//                u2.author.compareTo(u1.author) // 状态以降序排序
//            } else {
//                u1.genre.compareTo(u2.genre)         // 名字以升序排序
//            }
//        }
//        userList.sortWith(
////            //该方法指定了都是升序排序
////            compareBy ( {it.author},{it.genre})
////            //该方法可先升序再降序
////            compareBy(Book::author).thenBy(Book::genre)
//        )

        val booksByGenre = books.groupBy { it.genre }
        booksByGenre.forEach { (genre, books) ->
            println("$genre: ${books.map { it.title }}")
        }
        //集合转json
        "------------------------集合转json------------------------\n${books.toJson()}".logE
        //json转集合
        val testList = "[{\"author\":\"n11111\",\"genre\":\"11111\",\"title\":\"The Fng11111\"},{\"author\":\"J.D. Sa222\",\"genre\":\"Fn22222\",\"title\":\"Thye22222\"}]".toList(Book::class.java)
        "------------------------json转集合------------------------\n${testList?.safeGet(0)?.author}".logE
        //json转对象
        val testBean = "{\"author\":\"啊啊啊啊\",\"genre\":\"2 2 2 2 2 2\",\"title\":\"十大大大大1111\"}".toObj(Book::class.java)
        "------------------------json转对象------------------------\n${testBean?.title}".logE

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