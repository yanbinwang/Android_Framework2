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
    private var softEmpty: SoftReference<EmptyLayout>? = null//遮罩UI
    private var softRecycler: SoftReference<XRecyclerView>? = null//列表UI

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(activity: Activity?, context: Context?, view: BaseView?) {
        this.weakActivity = WeakReference(activity)
        this.weakContext = WeakReference(context)
        this.softView = SoftReference(view)
    }

//    fun <T> loadHttp(
//        request: suspend CoroutineScope.() -> ApiResponse<T>,  // 请求
//        resp: (T?) -> Unit = {},                            // 相应
//        err: (String?) -> Unit = {},                   // 错误处理
//        end: () -> Unit = {},                          // 最后执行方法
//        isShowToast: Boolean = true,                   // 是否toast
//        isShowDialog: Boolean = true,                  // 是否显示加载框
//    ) {
//        launch {
//            try {
//                if (isShowDialog) getView()?.showDialog()
//                //请求+响应数据
//                val data = request()
//                val body = data.apiCall(isShowToast)
//                if (null != body) resp(body) else err(data.msg)
//            } catch (e: Exception) {
//                //可根据具体异常显示具体错误提示异常处理
//                err(e.message ?: "")
//            } finally {
//                end()
//                if (isShowDialog) getView()?.hideDialog()
//            }
//        }
//    }

    fun <T> loadHttp(
        request: suspend CoroutineScope.() -> ApiResponse<T>,  // 请求
        resp: (T?) -> Unit = {},                            // 相应
        err: (String?) -> Unit = {},                   // 错误处理
        end: () -> Unit = {},                          // 最后执行方法
        isShowToast: Boolean = true,                   // 是否toast
        isShowDialog: Boolean = true,                  // 是否显示加载框
    ) {
        launch {
            if (isShowDialog) getView()?.showDialog()
            this.loadHttp(request = { request() }, resp = {
                resp(it)
            }, err = {
                err(it)
            }, end = {
                end()
                if (isShowDialog) getView()?.hideDialog()
            }, isShowToast = isShowToast)
        }
    }

    fun setEmptyView(container: ViewGroup) {
        this.softEmpty = SoftReference(container.getEmptyView())
    }

    fun setEmptyView(xRecyclerView: XRecyclerView) {
        this.softEmpty = SoftReference(xRecyclerView.empty)
        this.softRecycler = SoftReference(xRecyclerView)
    }

    fun getEmptyView() = softEmpty?.get()!!

    fun getRecycler() = softRecycler?.get()!!

    fun disposeView() {
        softRecycler?.get()?.finishRefreshing()
        softEmpty?.get()?.visibility = View.GONE
    }

    @JvmOverloads
    fun showEmpty(imgInt: Int = -1, text: String = "") {
        softEmpty?.get()?.visibility = View.VISIBLE
        softEmpty?.get()?.showEmpty(imgInt, text)
    }

    protected fun getView() = softView?.get()

    protected fun getActivity() = weakActivity?.get()

    protected fun getContext() = weakContext?.get()

    override fun onCleared() {
        super.onCleared()
        weakActivity?.clear()
        weakContext?.clear()
        softView?.clear()
        softEmpty?.clear()
        softRecycler?.clear()
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