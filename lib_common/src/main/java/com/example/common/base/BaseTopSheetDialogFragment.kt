package com.example.common.base

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.base.utils.function.value.currentTimeNano
import com.example.base.utils.function.value.orFalse
import com.example.base.utils.logE
import com.example.common.utils.AppManager
import com.example.common.utils.builder.StatusBarBuilder
import com.example.topsheet.TopSheetDialogFragment
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType

/**
 * @description
 * @author 顶部弹出的dialog
 * 可实现顶部弹出后，导航栏于弹框一致
 */
abstract class BaseTopSheetDialogFragment<VDB : ViewDataBinding> : TopSheetDialogFragment() {
    protected lateinit var binding: VDB
    protected var mContext: Context? = null
    protected val mActivity: FragmentActivity
        get() {
            return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity ?: FragmentActivity()
        }
    protected val statusBarBuilder by lazy { StatusBarBuilder(mActivity.window) }//状态栏工具类
    private var showTime = 0L
    private val isShow: Boolean
        get() = dialog.let { it?.isShowing.orFalse } && !isRemoving

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            val superclass = javaClass.genericSuperclass
            val aClass = (superclass as ParameterizedType).actualTypeArguments[0] as Class<*>
            val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            binding = method.invoke(null, layoutInflater, container, false) as VDB
        } catch (_: Exception) {
        }
        return binding.root
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (isAdded) return
        if (isShow) return
        if (activity?.isFinishing.orFalse) return
        if (manager.findFragmentByTag(tag) != null) return
        if (manager.isDestroyed) return
        //防止因为意外情况连续call两次show，设置500毫秒的最低间隔
        if (currentTimeNano - showTime < 500) return
        showTime = currentTimeNano
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.toString().logE
        }
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun onDetach() {
        super.onDetach()
        binding.unbind()
    }
    // </editor-fold>

}