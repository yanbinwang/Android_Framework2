package com.example.common.base.bridge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.example.common.base.page.getEmptyView
import com.example.common.http.repository.ApiResponse
import com.example.common.http.repository.launch
import com.example.common.http.repository.loadHttp
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.refresh.XRefreshLayout
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 */
@SuppressLint("StaticFieldLeak")
abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {
    private var weakActivity: WeakReference<Activity>? = null//引用的activity
    private var weakContext: WeakReference<Context>? = null//引用的context
    private var softView: SoftReference<BaseView>? = null//基础UI操作
    //部分view的操作交予viewmodel去操作，不必返回activity再操作
    private var softEmpty: SoftReference<EmptyLayout>? = null//遮罩UI
    private var softRecycler: SoftReference<XRecyclerView>? = null//列表UI
    private var softRefresh: SoftReference<XRefreshLayout>? = null//刷新控件

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    //注入page需要的3个基础参数
    fun initialize(activity: Activity?, context: Context?, view: BaseView?) {
        this.weakActivity = WeakReference(activity)
        this.weakContext = WeakReference(context)
        this.softView = SoftReference(view)
    }

    //部分页面包含empty，recyclerview，或刷新控件，此处额外注入
    fun addEmptyView(container: ViewGroup) {
        this.softEmpty = SoftReference(container.getEmptyView())
    }

    fun addEmptyView(xRecyclerView: XRecyclerView) {
        this.softEmpty = SoftReference(xRecyclerView.empty)
        this.softRecycler = SoftReference(xRecyclerView)
    }

    fun addRefreshLayout(xRefreshLayout: XRefreshLayout) {
        this.softRefresh = SoftReference(xRefreshLayout)
    }

    var emptyView = softEmpty?.get()

    var recyclerView = softRecycler?.get()

    var xRefreshLayout = softRefresh?.get()

    protected var activity = weakActivity?.get()

    protected var context = weakContext?.get()

    protected var view = softView?.get()

    protected fun reset() {
        xRefreshLayout?.finishRefresh()
        recyclerView?.finishRefresh()
        emptyView?.visibility = View.GONE
    }

    protected fun <T> loadHttp(
        request: suspend CoroutineScope.() -> ApiResponse<T>,      // 请求
        resp: (T?) -> Unit = {},                                   // 响应
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {}, // 错误处理
        end: () -> Unit = {},                                      // 最后执行方法
        isShowToast: Boolean = true,                               // 是否toast
        isShowDialog: Boolean = true,                              // 是否显示加载框
        isClose: Boolean = true                                    // 请求结束前是否关闭dialog
    ) {
        launch {
            this.loadHttp(
                { if (isShowDialog) view?.showDialog() },
                { request() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog && isClose) view?.hideDialog()
                    end()
                },
                isShowToast
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        weakActivity?.clear()
        weakContext?.clear()
        softView?.clear()
        softEmpty?.clear()
        softRecycler?.clear()
        softRefresh?.clear()
    }
    // </editor-fold>

//    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
//    override fun onCreate(owner: LifecycleOwner) {
//        super.onCreate(owner)
//    }
//
//    override fun onStart(owner: LifecycleOwner) {
//        super.onStart(owner)
//    }
//
//    override fun onResume(owner: LifecycleOwner) {
//        super.onResume(owner)
//    }
//
//    override fun onPause(owner: LifecycleOwner) {
//        super.onPause(owner)
//    }
//
//    override fun onStop(owner: LifecycleOwner) {
//        super.onStop(owner)
//    }
//
//    override fun onDestroy(owner: LifecycleOwner) {
//        super.onDestroy(owner)
//    }
//    // </editor-fold>

}