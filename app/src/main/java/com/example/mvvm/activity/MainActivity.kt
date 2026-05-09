package com.example.mvvm.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmapOrNull
import com.example.common.BaseApplication.Companion.needOpenHome
import com.example.common.base.BaseActivity
import com.example.common.base.bridge.viewModels
import com.example.common.base.page.Extra
import com.example.common.base.page.ResultCode.RESULT_ALBUM
import com.example.common.base.page.ResultCode.RESULT_FINISH
import com.example.common.base.page.ResultCode.RESULT_IMAGE
import com.example.common.bean.UserBean
import com.example.common.config.RouterPath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.drawable
import com.example.common.utils.function.getFileFromUri
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.intentParcelableArrayList
import com.example.common.utils.function.pt
import com.example.common.utils.toJson
import com.example.common.utils.toList
import com.example.common.utils.toObj
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragRate
import com.example.framework.utils.BitmapSpan
import com.example.framework.utils.ColorSpan
import com.example.framework.utils.ImageSpan
import com.example.framework.utils.SizeSpan
import com.example.framework.utils.TextSpan
import com.example.framework.utils.builder.TimerBuilder
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
import com.example.framework.utils.logWTF
import com.example.gallery.utils.MediaPicker
import com.example.mvvm.R
import com.example.mvvm.bean.TestBean
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.viewmodel.TestViewModel
import com.example.mvvm.widget.dialog.TestBottomDialog
import com.therouter.router.Route
import com.example.gallery.feature.durban.Durban
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull

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
 * kotlin的===等价于java的==  仅在需要严格比较引用时使用,如检查两个变量是否指向同一个对象实例
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

方法一：基于用户对象的唯一性（完全相等）
假设 User 类已正确重写 equals() 和 hashCode() 方法：

kotlin
data class User(val id: Int, val name: String) {
override fun equals(other: Any?): Boolean {
if (this === other) return true
if (other is User) return id == other.id && name == other.name
return false
}

override fun hashCode(): Int {
return Objects.hash(id, name)
}
}

val list = listOf(User(1, "Alice"), User(2, "Bob"))
val list2 = listOf(User(2, "Bob"), User(3, "Charlie"))

// 1. 生成重复集合（在两个列表中都存在的用户）
val repeated = list.toSet().intersection(list2.toSet())

// 2. 生成不重复集合（只存在于一个列表中的用户）
val allUsers = list.toSet().union(list2.toSet())
val unique = allUsers.subtract(repeated)

// 结果
println("Repeated users: $repeated") // [User(id=2, name=Bob)]
println("Unique users: $unique")     // [User(id=1, name=Alice), User(id=3, name=Charlie)]
方法二：基于用户 ID 判断重复（推荐）
如果 User 类的唯一标识是 id，可以提取 id 进行比较：

kotlin
data class User(val id: Int, val name: String)

val list = listOf(User(1, "Alice"), User(2, "Bob"))
val list2 = listOf(User(2, "Bob"), User(3, "Charlie"))

// 1. 提取 ID 并生成重复集合
val repeatedIds = list.map { it.id }.toSet().intersection(list2.map { it.id }.toSet())
val repeatedUsers = list.filter { it.id in repeatedIds }.toSet()

// 2. 生成不重复集合
val uniqueUsers = list.filter { it.id not in repeatedIds }
.plus(list2.filter { it.id not in repeatedIds })
.toSet()

// 结果
println("Repeated users: $repeatedUsers") // [User(id=2, name=Bob)]
println("Unique users: $uniqueUsers")     // [User(id=1, name=Alice), User(id=3, name=Charlie)]
关键说明
重复集合：使用集合的交集操作找出同时在两个列表中的用户。
不重复集合：通过并集减去交集得到仅存在于一个列表中的用户。
性能优化：转换为集合 (toSet()) 后操作时间复杂度更低（接近 O(1)）。
最终代码
kotlin
data class User(val id: Int, val name: String)

val list = listOf(User(1, "Alice"), User(2, "Bob"))
val list2 = listOf(User(2, "Bob"), User(3, "Charlie"))

// 方法一：基于对象相等性
val repeated1 = list.toSet().intersection(list2.toSet())
val unique1 = list.toSet().union(list2.toSet()).subtract(repeated1)

// 方法二：基于 ID 判断
val repeated2 = list.filter { it.id in list2.map { it.id }.toSet() }.toSet()
val unique2 = (list + list2)
.distinct() // 去重
.filter { !it.id in list2.map { it.id }.toSet() || !it.id in list.map { it.id }.toSet() }
.toSet()

// 输出结果
println("Repeated users (method 1): $repeated1")
println("Unique users (method 1): $unique1")
println("Repeated users (method 2): $repeated2")
println("Unique users (method 2): $unique2")

//关于Lifecycle和LifecycleOwner
其中LifecycleOwner是Activity/Fragment所实现的生命周期管理，从中可以get到Lifecycle，而Lifecycle则可以获取到对应的生命周期回调
在一些对上下文窗体不是很敏感的工具类里，可以用Lifecycle而非LifecycleOwner

类委托
类委托借助 by 关键字，把接口的实现委托给另一个对象。这样一来，当一个类实现某个接口时，就可以把接口方法的具体实现委托给另一个已经实现该接口的对象，而不用自己再去实现这些方法。

// 定义一个接口
interface MyInterface {
fun doSomething()
fun doAnotherThing()
}

// 实现接口的类
class MyInterfaceImpl : MyInterface {
override fun doSomething() {
println("Doing something...")
}

override fun doAnotherThing() {
println("Doing another thing...")
}
}

// 使用类委托的类
class MyDelegatingClass(private val delegate: MyInterface) : MyInterface by delegate

fun main() {
val impl = MyInterfaceImpl()
val delegatingClass = MyDelegatingClass(impl)

delegatingClass.doSomething()
delegatingClass.doAnotherThing()
}

MyInterface 是一个接口，定义了两个方法 doSomething() 和 doAnotherThing()。
MyInterfaceImpl 是实现了 MyInterface 接口的类，对接口方法进行了具体实现。
MyDelegatingClass 类同样实现了 MyInterface 接口，但它把接口方法的实现委托给了传入的 delegate 对象。
在 main 函数中，创建了 MyInterfaceImpl 和 MyDelegatingClass 的实例，调用 MyDelegatingClass 的方法时，实际上是调用了委托对象的方法。

属性委托
属性委托允许把属性的 getter 和 setter 方法委托给另一个对象。通过 by 关键字，能让属性的读写操作由另一个对象来处理。

标准库中的属性委托
Kotlin 标准库提供了一些常用的属性委托，例如 lazy、observable 等。
lazy 委托
lazy 委托用于实现属性的延迟初始化，即属性在第一次被访问时才会进行初始化

val lazyValue: String by lazy {
println("Initializing lazy value...")
"Lazy value"
}

fun main() {
println(lazyValue)
println(lazyValue)
}

observable 委托
observable 委托用于监听属性值的变化，当属性值发生改变时，会触发相应的回调函数。

class User {
var name: String by Delegates.observable("Initial Name") { property, oldValue, newValue ->
println("Property ${property.name} changed from $oldValue to $newValue")
}
}

fun main() {
val user = User()
user.name = "New Name"
}

在这个例子中，User 类的 name 属性使用 observable 委托，当 name 属性的值发生改变时，会打印出属性名、旧值和新值。

自定义属性委托
除了使用标准库中的属性委托，还可以自定义属性委托。自定义属性委托需要实现 ReadWriteProperty 或 ReadOnlyProperty 接口。

class MyDelegate {
private var value: String = ""

operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String {
println("Getting value of ${property.name}")
return value
}

operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: String) {
println("Setting value of ${property.name} to $newValue")
value = newValue
}
}

class MyClass {
var myProperty: String by MyDelegate()
}

fun main() {
val myClass = MyClass()
myClass.myProperty = "New Value"
println(myClass.myProperty)
}
在这个例子中，MyDelegate 类实现了属性委托的 getValue 和 setValue 方法，MyClass 类的 myProperty 属性使用 MyDelegate 作为委托对象。当访问或修改 myProperty 属性时，会调用 MyDelegate 类的 getValue 或 setValue 方法。
 */
@Route(path = RouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {
    //    private val illustratePopup by lazy { IllustratePopup(this) }
//    private val testDialog by lazy { TestTopDialog() }
    private val testDialog by lazy { TestBottomDialog(this) }

    //    private val ids = listOf(R.color.blue_2a3160, R.color.blue_1566ec, R.color.blue_6e7ce2, R.color.blue_aac6f4)
//    private val adapter by lazy { ImageAdapter() }
//    private val halfPosition by lazy { Int.MAX_VALUE / 2 }  //设定一个中心值下标
//    private val map = mapOf("1111" to "一", "2222" to "二", "3333" to "三")
    private val selectList by lazy { listOf("1" to true, "2" to true, "3" to true) }

    private val viewModel: TestViewModel by viewModels()
    private val bean by lazy { intentParcelable<UserBean>("bean") }

    //    private val builder by lazy { FileBuilder(this) }
//    private val album by lazy { AlbumHelper(this) }

    data class Book(val title: String, val author: String, val genre: String)

    private val timerBuilder by lazy { TimerBuilder(this) }

    private val gallery by lazy { MediaPicker(this) }

    override fun isImmersionBarEnabled() = false

    private var index = 0
    //https://sxlp.linan.gov.cn:9082/app/v1/login/show?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aW1lc3RhbXAiOjE3NjA0MTU0NTQsInJhbmQiOjQzNjk2LCJmaWxlUGF0aCI6InVwbG9hZHMvMjAyNS8wOS8yMi8xMTc2ODIyODE0NTYxMjc1OTA1Lm1wNCJ9.xXRF5pDtSDjZ4dAKzWsUDVNlid_HWI9y6VzdyRGdBSc

//    private val gsyHelper by lazy { GSYVideoHelper(this) }

    @SuppressLint("RestrictedApi")
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
//        overridePendingTransition(0, 0)
//        BaseApplication.instance.initPrivacyAgreed()

//        gsyHelper.bind(mBinding?.gsyPlayer, true)
//        // 竖屏
////        gsyHelper.setUrl("https://sxlp.linan.gov.cn:9082/app/v1/login/show?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aW1lc3RhbXAiOjE3NjA0MTU0NTQsInJhbmQiOjQzNjk2LCJmaWxlUGF0aCI6InVwbG9hZHMvMjAyNS8wOS8yMi8xMTc2ODIyODE0NTYxMjc1OTA1Lm1wNCJ9.xXRF5pDtSDjZ4dAKzWsUDVNlid_HWI9y6VzdyRGdBSc")
//        // 横屏
//        gsyHelper.setUrl("https://stream7.iqilu.com/10339/upload_transcode/202002/09/20200209105011F0zPoYzHry.mp4")
//        gsyHelper.setOnQuitFullscreenListener {
//            initImmersionBar()
//        }
//
//
//        mBinding?.flCard.adjustRadiusDrawable(R.color.bgBlue,5.pt)
//        launch {
//            ImageLoader.instance.loadRoundedImageFromUrl(mBinding?.ivThumb,
//                "https://qcloud.dpfile.com/pc/5Ct4AVJJv2aq5MjcUIeJ2STd0ZYkopTa4r99ekPIg6qMpU7jk1n9-dyjZitV3vvb.jpg",
//                cornerRadius = 60.dp)
//            ImageLoader.instance.loadScaledImage(mBinding?.ivThumb,
//                "https://qcloud.dpfile.com/pc/5Ct4AVJJv2aq5MjcUIeJ2STd0ZYkopTa4r99ekPIg6qMpU7jk1n9-dyjZitV3vvb.jpg")
//            val card = CardView(ContextThemeWrapper(this@MainActivity, R.style.CardViewStyle)).apply {
//                radius = 10.ptFloat
//            }
//            card.size(MATCH_PARENT,MATCH_PARENT)
//            val imageView = ImageView(this@MainActivity)
//            imageView.scaleType = ImageView.ScaleType.FIT_XY
//            card.addView(imageView)
//            imageView.size(MATCH_PARENT,MATCH_PARENT)
//            ImageLoader.instance.loadImageFromUrl(imageView,
//                "https://qcloud.dpfile.com/pc/5Ct4AVJJv2aq5MjcUIeJ2STd0ZYkopTa4r99ekPIg6qMpU7jk1n9-dyjZitV3vvb.jpg")
//            mBinding?.flCard?.addView(card)
//        }
        initImmersionBar(navigationBarDark = true, navigationBarColor = R.color.bgWhite)
        data class User(val id: Int, val name: String, val amount: Double)
        // 服务器数据（基准）
        val serverUsers = listOf(
            User(1, "张三", 100.0),
            User(2, "李四", 200.0),
            User(3, "王五", 350.0)
        )
        // 本地数据（原有）
        val localUsers = listOf(
            User(1, "张三", 100.0),
            User(3, "王五", 300.0),
            User(5, "赵六", 500.0),
            User(7, "孙七", 700.0)
        )
        setOnActivityResultListener {
            if (it.resultCode == RESULT_OK) {
                val tempUri = it.data?.data
                val tempFile = tempUri.getFileFromUri(this)
                tempFile?.absolutePath.shortToast()
//                if (tempFile != null) {
//                    // 获取源文件的真实后缀（比如从Uri/文件名解析）
//                    tempUri.getRealSourceSuffix(this).shortToast()
//                }
            }
            if (it.resultCode == RESULT_FINISH) {
//                val list = it.data?.getExtra(Extra.BUNDLE_LIST,ArrayList::class.java) as? ArrayList<TestBean>
                val list = it.data?.intentParcelableArrayList<TestBean>(Extra.BUNDLE_LIST)
                "回退的集合:${list.toJson()}".logWTF("wyb")
            }
        }
        mBinding?.codeInput?.focusNow(this)
        mBinding?.ivArrow.click {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                navigation(RouterPath.ScreenActivity)
//            }
//            navigation(RouterPath.TouchActivity, Extra.RESULT_CODE to RESULT_FINISH)
//            mActivityResult.pullUpAlbum()
//            val trueList = localUsers.toExtract(serverUsers,{localItem, serverItem ->
//                localItem.id == serverItem.id
//            },{localItem, serverItem ->
//                localItem.name != serverItem.name || localItem.amount != serverItem.amount
//            },true)
//            "toExtract为true:${trueList.toJson()}".logWTF("wyb")
//            val falseList = localUsers.toExtract(serverUsers,{localItem, serverItem ->
//                localItem.id == serverItem.id
//            },{localItem, serverItem ->
//                localItem.name != serverItem.name || localItem.amount != serverItem.amount
//            })
//            "toExtract为false:${falseList.toJson()}".logWTF("wyb")
//            // 2s一跳,测试刷新
//            timerBuilder.startTask("10086",{
//                var logText = "------ 测试数据 ------\n"
//                val bean = DataBindingBean()
//                val txtRan = Random.nextInt(1, 3)
//                if (txtRan % 2 == 0) {
//                    logText = logText+"高亮文本:\n高亮文本 -> $index\n"
//                    bean.spannable = TextSpan().add("高亮文本 -> $index", ColorSpan(color(R.color.bgMain))).build()
//                } else {
//                    logText = logText+"普通文本:\n普通文本 -> $index\n"
//                    bean.text = "普通文本 -> $index"
//                }
//                val colorRan = Random.nextInt(1, 3)
//                if (colorRan % 2 == 0) {
//                    logText = logText+"文本颜色:\n文本颜色 -> ${R.color.bgBlack}\n"
//                    bean.textColor = R.color.bgBlack
//                } else {
//                    logText = logText+"文本颜色:\n文本颜色 -> ${R.color.bgWhite}\n"
//                    bean.textColor = R.color.bgWhite
//                }
//                val backRan = Random.nextInt(1, 3)
//                if (backRan % 2 == 0) {
//                    logText = logText+"背景:\n背景 -> ${R.drawable.shape_r20_blue}\n"
//                    bean.background = R.drawable.shape_r20_blue
//                } else {
//                    logText = logText+"背景:\n背景 -> ${R.drawable.shape_r20_grey}\n"
//                    bean.background = R.drawable.shape_r20_grey
//                }
//                val visRan = Random.nextInt(1, 3)
//                if (visRan % 2 == 0) {
//                    logText = logText+"可见性:\n可见性 -> ${View.VISIBLE}\n"
//                    bean.visibility = View.VISIBLE
//                } else {
//                    logText = logText+"可见性:\n可见性 -> ${View.GONE}\n"
//                    bean.visibility = View.GONE
//                }
//                mBinding?.setVariable(BR.bean,bean)
//                logText.logWTF("wyb")
//            },2000)
//            navigation(RouterPath.LoginActivity)
//            viewModel.getShare()
//            navigation(ARouterPath.TestActivity2)
//            it.rotate()
//            mBinding?.finder?.onShutter()
            mPermission.requestPermissions { isGranted, _ ->
                if (isGranted) {
//                    pullUpImage()
//                    gallery.takePicture(true){
//                        it.shortToast()
//                    }
                    gallery.videoMultipleSelection()
//                    gallery.imageSelection(hasDurban = true)
//                    gallery.imageMultipleSelection(true)
//                    navigation(ARouterPath.TestActivity)
                }
            }
//            testDialog.show()
//            SnackBarBuilder.custom(it, Snackbar.LENGTH_LONG, { snackbar ->
//                // 透明背景
//                snackbar.setBackgroundTint(Color.TRANSPARENT)
//                // 获取 Snackbar 的根视图
//                val snackbarView = snackbar.view
//                // 隐藏默认的文本和动作视图
//                val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
//                snackbarText.gone()
//                val snackbarAction = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
//                snackbarAction.gone()
//                // 加载自定义视图
//                val binding = ViewSnackbarImageStyleBinding.bind(this.inflate(R.layout.view_snackbar_image_style))
//                binding.ivType.setImageResource(R.mipmap.ic_toast)
//                binding.tvLabel.text = "复制成功"
//                //父布局
//                val root = snackbarView as? ViewGroup
//                // 移除默认视图
//                root?.removeAllViews()
//                // 添加自定义视图
//                root?.addView(binding.root)
////                // 空出顶部导航栏
////                binding.root.margin(top = getStatusBarHeight())
//                return@custom snackbar
//            }, true)
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
        val testList =
            "[{\"author\":\"n11111\",\"genre\":\"11111\",\"title\":\"The Fng11111\"},{\"author\":\"J.D. Sa222\",\"genre\":\"Fn22222\",\"title\":\"Thye22222\"}]".toList(
                Book::class.java
            )
        "------------------------json转集合------------------------\n${testList?.safeGet(0)?.author}".logE
        //json转对象
        val testBean =
            "{\"author\":\"啊啊啊啊\",\"genre\":\"2 2 2 2 2 2\",\"title\":\"十大大大大1111\"}".toObj(
                Book::class.java
            )
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

//        /**
//         * take(n)	返回前 n 个元素的新列表
//         * drop(n)	跳过前 n 个元素，返回剩余元素的列表
//         * subList(n)	返回从索引 n 开始的子列表（Java 风格）
//         */
//        val list = listOf(1, 2, 3, 4, 5)
//        // 截取前 3 个元素
//        val result = list.take(3) // [1, 2, 3]
//        // 截取超过长度的元素（原列表长度为 5，n=10）
//        val result2 = list.take(10) // [1, 2, 3, 4, 5]
//        // n 为负数时返回空列表
//        val result3 = list.take(-5) // []


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_ALBUM) {
            data ?: return
            val mImageList = Durban.parseResult(data)
            mImageList.safeGet(0).shortToast()
        }
        if (requestCode == RESULT_IMAGE) {
            val uri = data?.data
            val oriFile = uri.getFileFromUri(this)
            "${oriFile?.absolutePath}".shortToast()
        }
//        if (requestCode == RESULT_ALBUM) {
//            data ?: return
//            val mImageList = Durban.parseResult(data)
//            mImageList.safeGet(0).shortToast()
//        }
    }

//    class TestBean(
//        var key: String? = null,
//        var value: String? = null
//    )

    private val list by lazy { ArrayList<Int>() }

    private suspend fun test() {
        //方式1-通过withTimeoutOrNull 函数可以实现取消flow携程功能， 通过该功能可以处理类似于某些场景下的超时机制，兜底逻辑等
        withTimeoutOrNull(250) { // 在 250 毫秒后超时
            var i = 0
            flow {
                //重复3次
                repeat(3) {
                    delay(1000)
                    i++
                    emit(i)
                }
            }.collect {
                list.add(it)
            }
        }
        //方式2-flowof扩展函数其实内部也是调用flow扩展函数，只不过flowof是将传递进来的可变参数，遍历了一遍，并且调用flow收集器的emit方法发送取出而已
        val flow2 = flowOf(1, 2, 3).onEach {
            delay(1000)
        }.collect {
            list.add(it)
        }
        //方式3-调用list顶级接口类Iterable的asFlow方法，其实内部还是调用了flow扩展函数，将元素遍历之后emit出去的
        val flow3 = listOf(1, 2, 3).asFlow().onEach {
            delay(1000)
        }.collect {
            list.add(it)
        }
//        map<T>(transform: suspend T.() -> R)**
//        作用：将每个元素转换为另一个值。
//        特点：自动挂起，适合执行耗时操作（如网络请求）。
//        示例：
//        kotlin
//        flowOf(1, 2, 3)
//            .map { it * 2 } // 转换为 [2, 4, 6]
//            .collect { println(it) }

//        filter(predicate: suspend (T) -> Boolean)**
//        作用：过滤不符合条件的元素。
//        示例：
//        kotlin
//        flowOf(1, 2, 3, 4)
//            .filter { it % 2 == 0 } // 过滤偶数 [2, 4]
//            .collect { println(it) }

//        flatMap<T>(transform: suspend (T) -> Flow<T>)**
//        作用：将每个元素替换为一个子流，并将所有子流的元素合并到当前流中。
//        示例：
//        kotlin
//        flowOf(1, 2, 3)
//            .flatMap { flowOf(it * 10, it * 100) } // 展开为 [10, 100, 20, 200, 30, 300]
//            .collect { println(it) }

//        collect(consumer: suspend (T) -> Unit)**
//        作用：触发流的执行并消费数据。
//        关键点：冷流只有调用 collect 后才会开始发射数据。
//        示例：
//        kotlin
//        flowOf(1, 2, 3).collect { println("Received: $it") }

//        toList() / toSet()**
//        作用：将流收集到一个列表或集合中。
//        示例：
//        kotlin
//        val list = flowOf(1, 2, 3).toList() // [1, 2, 3]

//        flowOn(context: CoroutineContext)**
//        作用：切换流的上下文（Dispatcher），仅影响后续操作符。
//        示例：
//        kotlin
//        flowOf(1, 2, 3)
//            .flowOn(Dispatchers.IO) // 后续操作在 IO 线程执行
//            .map { heavyCalculation(it) } // 在 IO 线程执行耗时操作
//            .collect { println(it) } // 在主线程收集结果

//        catch(handler: suspend (Throwable) -> Unit)**
//        作用：捕获流中抛出的异常，并继续发射数据。
//        示例：
//        kotlin
//        flowOf(1, 2, 3)
//            .map { if (it == 2) throw RuntimeException("Oops") else it }
//            .catch { e -> emit(-1) } // 异常时发射 -1
//            .collect { println(it) }

//        onCompletion(handler: suspend () -> Unit)**
//        作用：在流完成（无异常或被取消）时触发回调。
//        示例：
//        kotlin
//        flowOf(1, 2, 3)
//            .onCompletion { println("Stream completed!") }
//            .collect { println(it) }

//        buffer(bufferSize: Int)**
//        作用：缓冲元素，允许在收集时异步发射。
//        场景：防止因下游处理慢导致上游阻塞。
//        示例：
//        kotlin
//        flowOf(1, 2, 3)
//            .buffer(5) // 缓冲最多 5 个元素
//            .collect { println(it) }

//        zip(other: Flow<T>)**
//        作用：将两个流按顺序配对，取最短长度。
//        示例：
//        kotlin
//        val flowA = flowOf(1, 2, 3)
//        val flowB = flowOf("A", "B")
//
//        flowA.zip(flowB) { a, b -> Pair(a, b) }
//            .collect { println(it) } // 输出 (1,A), (2,B)

//        操作符执行顺序（重要！）
//        kotlin
//        flowOf(1, 2, 3)
//            .map { /* 上游操作 */ }       // 1
//            .flowOn(Dispatchers.IO)     // 2（切换上下文，仅影响后续操作）
//            .flatMap { /* 中游操作 */ }  // 3
//            .catch { /* 下游错误处理 */ } // 4
//            .collect { /* 最终消费 */ }    // 5
//        上下文切换（如 flowOn）只会影响其后的操作符。
//        错误处理（如 catch）会在流末尾捕获所有上游异常。

//        1. merge
//        用途：将多个独立的Flow合并为一个流，数据按发出顺序交错并发出，无序且支持并发处理。
//        特性：
//        并发性：多个流的数据会同时发出，下游按接收顺序处理。
//        无序性：合并后的数据顺序与各流内部顺序无关，取决于处理速度。
//        实时性：立即处理每个流的数据，适合实时场景（如传感器数据、聊天消息）。
//        示例：
//        kotlin
//        val flow1 = flow { emit(1); delay(100); emit(3) }
//        val flow2 = flow { emit(2); delay(50); emit(4) }
//        val merged = merge(flow1, flow2)
//        merged.collect { println(it) } // 输出可能为 1 2 3 4 或 2 1 4 3 等
//        2. flatMapMerge->警告
//        用途：将嵌套的Flow<Flow<T>>（即流的流）展平并合并，支持并发处理，不保证顺序。
//        特性：
//        并发性：内层流同时启动并发出数据。
//        无序性：合并后的数据顺序由各内层流的处理速度决定。
//        灵活性：可指定并发数（通过参数控制），避免资源过载。
//        示例：
//        kotlin
//        val source = flowOf(1, 2, 3).onEach { delay(100) }
//        val transformed = source.flatMapMerge(concurrency = 4) { x ->
//            flow {
//                emit("First: $x")
//                delay(150)
//                emit("Second: ${x * x}")
//            }
//        }
//        transformed.collect { println(it) }
        // 输出可能为：
        // First: 1, First: 2, First: 3
        // Second: 1, Second: 4, Second: 9
//        关键区别
//        特性	merge	flatMapMerge
//        输入类型	多个独立的Flow	嵌套的Flow<Flow<T>>
//        顺序保证	无序（并发导致）	无序（并发导致）
//        并发控制	无限制	可指定并发数（如concurrency参数）
//        适用场景	合并多个独立数据源	处理转换后的嵌套流（如每个元素生成一个流）
//        总结
//        使用merge：当需要合并多个独立流且不关心顺序时（如实时数据聚合）。
//        使用flatMapMerge：当需要将元素转换为流后并发处理（如批量请求或动态生成流）

//            //并行
//            val result = Result()
//            val taskCenter = flowWithType("bean", flowOf(req.request({ FundsSubscribe.getTaskCenterApi(reqBodyOf()) })))
//            val taskList = flowWithType("list", flowOf(req.request({ FundsSubscribe.getTaskListApi(reqBodyOf()) })))
//            merge(taskCenter, taskList).onCompletion {
//                if (req.successful()) {
//                    reset(false)
//                    pageInfo.postValue(result.value())
//                }
//            }.collect { (type, data) ->
//                when (type) {
//                    "bean" -> result.bean = data as? TaskCenterBean
//                    "list" -> result.list = data as? List<TaskBean>
//                }
//            }
//
//            //串行
//        val combinedFlow = flow {
//            emitAll(flowAdvertiseInfo())->抽出去flow
//            emitAll(flowCoinInfo(bean))
//        }
//            val result = Result()
//            val taskCenter = flowWithType("bean", flowOf(req.request({ FundsSubscribe.getTaskCenterApi(reqBodyOf()) })))
//            val taskList = flowWithType("list", flowOf(req.request({ FundsSubscribe.getTaskListApi(reqBodyOf()) })))
//            listOf(taskCenter,taskList).asFlow().flattenConcat().onCompletion {
//                if (req.successful()) {
//                    reset(false)
//                    pageInfo.postValue(result.value())
//                }
//            }.collect { (type, data) ->
//                when (type) {
//                    "bean" -> result.bean = data as? TaskCenterBean
//                    "list" -> result.list = data as? List<TaskBean>
//                }
//            }
//    //为每个 Flow 添加标识符（如类型标签），便于后续区分
//    fun <T> flowWithType(type: String, flow: Flow<T>): Flow<Pair<String, T>> = flow.map { Pair(type, it) }
//
//    data class Result(var bean: TaskCenterBean? = null, var list: List<TaskBean>? = null) {
//        fun value(): Pair<TaskCenterBean?, List<TaskBean>?> {
//            return bean to list
//        }
//    }
//data class CoinDetail(
//    var coinBean: CoinBean? = null,
//    var rateBean: CoinRateBean? = null,
//    var amount: String? = null
//)
//
//        /**
//         * flow收集所有货币的详细信息，比率和保留小数位数可用數量等
//         */
//        fun getCoinDetail() {
//            launch {
//                val list = ArrayList<CoinDetail>()
//                flow {
//                    val currencies = CommonSubscribe.getCoinListApi().data.orEmpty()
//                    emitAll(currencies.asFlow())
//                }.flatMapConcat { currency ->
//                    flow {
//                        val rateBean = FundsSubscribe.getCoinRateApi(
//                            reqBodyOf(
//                            "coinName" to currency.digitalCurrency,
//                            "marketCoinName" to currency.legalCurrency
//                        )
//                        ).data
//                        val amount = FundsSubscribe.getCoinAmountApi(reqBodyOf("unit" to currency.digitalCurrency)).data
//                        emit(CoinDetail(currency, rateBean, amount))
//                    }
//                }.uiFlow().collect {
//                    list.add(it)
//                }
//            }
//        }

//        在 Kotlin 中，StateFlow 和 SharedFlow 是 Flow 库中用于处理热流（Hot Flow）的核心组件，适用于需要实时更新或共享数据的场景。以下是它们的使用方法和区别：
//
//        1. StateFlow
//        StateFlow 是一种特殊的热流（Hot Flow），用于表示 单一数据源的持续状态，类似于 LiveData，但更灵活且线程安全。它总是持有最新值，且会自动通知所有订阅者状态变化。
//
//        核心特性：
//        初始值：创建时必须指定初始值，且后续更新会覆盖旧值。
//        自动通知：当值变化时，所有订阅者会自动收到更新。
//        去重：仅在新值与旧值不同时触发更新。
//        使用示例：
//        kotlin
//        class ReactiveCounter {
//            private val _count = MutableStateFlow(0) // 可变状态流
//            val count: StateFlow<Int> = _count // 只读状态流
//
//            fun increment() {
//                _count.value++ // 更新状态
//            }
//        }
//
//        fun main() = runBlocking {
//            val counter = ReactiveCounter()
//            launch {
//                counter.count.collect { value ->
//                    println("当前计数值：$value") // 每次状态变化自动触发
//                }
//            }
//            counter.increment() // 输出：1
//            counter.increment() // 输出：2
//        }
//        引用来源：、
//
//        2. SharedFlow
//        SharedFlow 是更通用的热流，用于表示 事件流或多值发射，支持灵活的配置（如重放值数量、缓冲策略等）。
//
//        核心特性：
//        无初始值：创建时不需指定初始值，需手动发射数据。
//        可配置性：通过参数控制重放值（replay）、缓冲容量（extraBufferCapacity）等。
//        事件触发：每次发射新值均会通知所有订阅者，适合非状态类场景（如点击事件）。
//        使用示例：
//        kotlin
//        val sharedFlow = MutableSharedFlow<Int>()
//
//        fun main() = runBlocking {
//            launch {
//                sharedFlow.collect { value ->
//                    println("收到事件：$value")
//                }
//            }
//            sharedFlow.emit(1) // 输出：1
//            sharedFlow.emit(2) // 输出：2
//        }
//        引用来源：、
//
//        3. StateFlow 与 SharedFlow 的区别
//                特性	StateFlow	SharedFlow
//        数据类型	单一状态值（类似 LiveData）	多值事件流
//        初始值	必须指定	无需指定
//        自动更新	值变化时自动通知所有订阅者	需手动发射数据
//        去重	仅在新旧值不同时触发更新	每次发射均触发
//        适用场景	状态管理（如计数器、UI 状态）	事件流（如点击、网络请求回调）
//        引用来源：
//
//        4. 常见操作
//        取消订阅：通过协程的 cancel() 方法停止收集。
//        终止流：使用 takeWhile 或异常处理提前终止。
//        转换冷流：通过 stateIn 操作符将冷流转为 StateFlow。
//        总结
//        StateFlow：适合需要维护单一状态且自动更新的场景（如 UI 状态）。
//        SharedFlow：适合需要灵活控制事件流和多值发射的场景（如事件总线）。
//        通过合理选择两者，可以高效实现响应式状态管理和实时数据流处理。

//        flow<Unit> {
//            //认证回退
//            request { AccountSubscribe.getAuthBackApi() }
//            request { CommonSubscribe.getUserAuthApi() }.apply { AccountHelper.refresh(this) }
//        }.withHandling().launchIn(viewModelScope)
    }

    override fun initEvent() {
        super.initEvent()
        setOnWindowInsetsChanged {
            testDialog.setNavigationBar(it)
        }
        //通过代码动态重置一下顶部的高度
        val bgHeight = 164.pt + getStatusBarHeight()
        mBinding?.ivFundsBg.size(height = bgHeight)
        mBinding?.llFunds.apply {
            size(height = bgHeight)
            padding(top = getStatusBarHeight())
        }
        //全屏的刷新，顶部需要空出导航栏的距离
        mBinding?.refresh.setHeaderDragRate()
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

    override fun onResume() {
        if (needOpenHome.get()) needOpenHome.set(false)
        super.onResume()
    }

}