package com.example.common.base

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.example.base.utils.LogUtil
import com.example.base.utils.ToastUtil
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.page.PageParams
import com.example.common.base.proxy.SimpleTextWatcher
import com.example.common.constant.Extras
import com.example.common.utils.builder.StatusBarBuilder
import com.example.common.widget.dialog.LoadingDialog
import java.io.Serializable
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * Created by WangYanBin on 2020/6/3.
 * 对应页面传入继承自BaseViewModel的数据模型类，以及由系统生成的ViewDataBinding绑定类
 * 在基类中实现绑定，向ViewModel中注入对应页面的Activity和Context
 */
abstract class BaseActivity<VDB : ViewDataBinding> : AppCompatActivity(), BaseImpl, BaseView {
    protected lateinit var binding: VDB
    protected val activity by lazy { WeakReference<Activity>(this) } //基类activity弱引用
    protected val context by lazy { WeakReference<Context>(this) }//基类context弱引用
    protected val statusBarBuilder by lazy { StatusBarBuilder(this) }//状态栏工具类
    private var baseViewModel: BaseViewModel? = null//数据模型
    private val loadingDialog by lazy { LoadingDialog(this) }//刷新球控件，相当于加载动画
    private val TAG = javaClass.simpleName.toLowerCase(Locale.getDefault()) //额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected fun <VM : BaseViewModel?> createViewModel(vmClass: Class<VM>): VM {
        if (null == baseViewModel) {
            baseViewModel = ViewModelProvider(this).get(vmClass)
            baseViewModel?.initialize(this, this, this)
            lifecycle.addObserver(baseViewModel!!)
        }
        return baseViewModel as VM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vbClass?.getMethod("inflate", LayoutInflater::class.java)
                binding = method?.invoke(null, layoutInflater) as VDB
                binding.lifecycleOwner = this
            } catch (e: Exception) {
                e.printStackTrace()
            }
            setContentView(binding.root)
        }
        initView()
        initEvent()
        initData()
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    override fun isEmpty(vararg objs: Any?): Boolean {
        for (obj in objs) {
            if (obj == null) {
                return true
            } else if (obj is String && obj == "") {
                return true
            }
        }
        return false
    }

    override fun openDecor(view: View?) {
        closeDecor(view)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                    0,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }, 200)
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 2)
    }

    override fun closeDecor(view: View?) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun getFocus(view: View?) {
        view?.isFocusable = true //设置输入框可聚集
        view?.isFocusableInTouchMode = true //设置触摸聚焦
        view?.requestFocus() //请求焦点
        view?.findFocus() //获取焦点
    }

    override fun getParameters(view: View?): String? {
        return when (view) {
            is EditText -> view.text.toString().trim { it <= ' ' }
            is TextView -> view.text.toString().trim { it <= ' ' }
            is CheckBox -> view.text.toString().trim { it <= ' ' }
            is RadioButton -> view.text.toString().trim { it <= ' ' }
            is Button -> view.text.toString().trim { it <= ' ' }
            else -> null
        }
    }

    override fun onTextChanged(simpleTextWatcher: SimpleTextWatcher?, vararg views: View?) {
        for (view in views) {
            if (view is EditText) {
                view.addTextChangedListener(simpleTextWatcher)
            }
        }
    }

    override fun onClick(onClickListener: View.OnClickListener?, vararg views: View?) {
        for (view in views) {
            view?.setOnClickListener(onClickListener)
        }
    }

    override fun VISIBLE(vararg views: View?) {
        for (view in views) {
            if (view != null) {
                view.visibility = View.VISIBLE
            }
        }
    }

    override fun INVISIBLE(vararg views: View?) {
        for (view in views) {
            if (view != null) {
                view.visibility = View.INVISIBLE
            }
        }
    }

    override fun GONE(vararg views: View?) {
        for (view in views) {
            if (view != null) {
                view.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    override fun log(msg: String) {
        LogUtil.e(TAG, msg)
    }

    override fun showToast(msg: String) {
        ToastUtil.mackToastSHORT(msg, applicationContext)
    }

    override fun showDialog(flag: Boolean) {
        loadingDialog.show(flag)
    }

    override fun hideDialog() {
        loadingDialog.hide()
    }

    override fun navigation(path: String, params: PageParams?): Activity {
        val postcard = ARouter.getInstance().build(path)
        var code: Int? = null
        if (params != null) {
            val map: Map<String, Any> = params.map
            for (key in map.keys) {
                val value = map[key]
                val cls: Class<*> = value?.javaClass!!
                if (key == Extras.REQUEST_CODE) {
                    code = value as Int?
                    continue
                }
                when {
                    value is Parcelable -> postcard.withParcelable(key, value as Parcelable?)
                    value is Serializable -> postcard.withSerializable(key, value as Serializable?)
                    cls == String::class.java -> postcard.withString(key, value as String?)
                    cls == Int::class.javaPrimitiveType -> postcard.withInt(key, value as Int)
                    cls == Long::class.javaPrimitiveType -> postcard.withLong(key, value as Long)
                    cls == Boolean::class.javaPrimitiveType -> postcard.withBoolean(key, value as Boolean)
                    cls == Float::class.javaPrimitiveType -> postcard.withFloat(key, value as Float)
                    cls == Double::class.javaPrimitiveType -> postcard.withDouble(key, value as Double)
                    cls == CharArray::class.java -> postcard.withCharArray(key, value as CharArray?)
                    cls == Bundle::class.java -> postcard.withBundle(key, value as Bundle?)
                    else -> throw RuntimeException("不支持参数类型" + ": " + cls.simpleName)
                }
            }
        }
        if (code == null) {
            postcard.navigation()
        } else {
            postcard.navigation(this, code)
        }
        return this
    }
    // </editor-fold>

}