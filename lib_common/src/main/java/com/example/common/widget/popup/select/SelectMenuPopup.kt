package com.example.common.widget.popup.select

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.BasePopupWindow
import com.example.common.databinding.ViewPopupSelectMenuBinding
import com.example.common.utils.ScreenUtil
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import java.lang.ref.WeakReference

/**
 * Created by wangyanbin
 * 顶部多选弹框
 * 支持左右侧，部分单位可该系
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectMenuPopup<T>(activity: FragmentActivity, var formatter: (T?) -> String?) : BasePopupWindow<ViewPopupSelectMenuBinding>(activity, hasLight = false) {
    private var lastMenuWidth = 0
    private var onCurrent: ((item: String?, index: Int) -> Unit)? = null

    /**
     * 假设弹出的上方的view是绘制好的
     *  mBinding?.llFilter?.doOnceAfterLayout {
     *    coin.setParams(listOf(string(R.string.unitINR), string(R.string.unitIDR)), mBinding?.llFilter?.measuredWidth.orZero)
     *  }
     */
    fun setParams(list: List<T>, menuWidth: Int = 0, horizontalMargin: Int = 15.pt, verticalMargin: Int = 0, gravity: Int = Gravity.END) {
        lastMenuWidth = menuWidth
        setConfiguration(list, MenuBean().apply {
            this.menuWidth = menuWidth
            this.horizontalMargin = horizontalMargin
            this.verticalMargin = verticalMargin
            this.gravity = gravity
        })
    }

    fun setParams(list: List<T>, view: WeakReference<View>, gravity: Int = Gravity.END) {
        if (0 == lastMenuWidth) {
            view.get()?.doOnceAfterLayout {
                setParams(list, menuWidth = it.measuredWidth, gravity = gravity)
            }
        } else {
            setParams(list, menuWidth = lastMenuWidth, gravity = gravity)
        }
    }

    private fun setConfiguration(list: List<T>, bean: MenuBean = MenuBean()) {
        mBinding?.apply {
            // 确定箭头大小/位置
            setupArrow(viewArrow, bean)
            // 确定容器大小/位置
            setupContainer(llItem, list, bean)
        }
    }

    private fun setupArrow(arrowView: View, bean: MenuBean) {
        arrowView.layoutGravity = bean.gravity
        arrowView.margin(
            start = if (bean.gravity == Gravity.START) 10.pt + bean.horizontalMargin else 0,
            top = bean.verticalMargin,
            end = if (bean.gravity == Gravity.END) 10.pt + bean.horizontalMargin else 0
        )
    }

    private fun setupContainer(container: ViewGroup, list: List<T>, bean: MenuBean) {
        container.apply {
            if (bean.menuWidth != 0) size(bean.menuWidth, WRAP_CONTENT)
            layoutGravity = bean.gravity
            margin(
                start = if (bean.gravity == Gravity.START) bean.horizontalMargin else 0,
                top = 10.pt + bean.verticalMargin,
                end = if (bean.gravity == Gravity.END) bean.horizontalMargin else 0
            )
            removeAllViews()
            list.forEachIndexed { index, t ->
                addView(SelectItemHolder(this, formatter(t), index).also {
                    it.onItemClick = { item, clickIndex ->
                        dismiss()
                        onCurrent?.invoke(item, clickIndex)
                    }
                }.mBinding.root)
                // 绘制下划线
                if (list.safeSize - 1 > index) {
                    addDivider(this)
                }
            }
        }
    }

    private fun addDivider(container: ViewGroup) {
        val view = View(container.context)
        view.setBackgroundColor(color(R.color.bgLine))
        view.size(MATCH_PARENT, 1.pt)
        view.margin(start = 1.pt, end = 1.pt)
        container.addView(view)
    }

    fun setOnItemClickListener(onCurrent: ((item: String?, index: Int) -> Unit)) {
        this.onCurrent = onCurrent
    }

}

class MenuBean {
    var menuWidth: Int = ScreenUtil.screenWidth//父item的宽度，不传则为手机宽度
    var horizontalMargin: Int = 15.pt//距左右侧默认位置
    var verticalMargin: Int = 0//距上下默认位置（默认就在view下放，贴合）
    var gravity: Int = Gravity.END//菜单位置（左右侧）
}