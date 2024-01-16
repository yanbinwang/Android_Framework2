package com.example.common.widget.popup.select

import android.view.Gravity
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.common.databinding.ViewPopupSelectMenuBinding
import com.example.common.utils.ScreenUtil
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size

/**
 * Created by wangyanbin
 * 顶部多选弹框
 * 支持左右侧，部分单位可该系
 * private val occupationPopup by lazy { SelectBottomPopup<String>(this) { it }.apply { setParams(UserAuthBean.jobList) }}
 */
class SelectMenuPopup<T>(activity: FragmentActivity, var formatter: (T?) -> String?) : BasePopupWindow<ViewPopupSelectMenuBinding>(activity, light = false) {
    private var onCurrent: ((item: String, index: Int) -> Unit)? = null

    fun setParams(list: List<T>, menuWidth: Int? = 0, horizontalMargin: Int? = 15.pt, location: Location? = Location.END) {
        setParams(list, MenuBean().apply {
            this.menuWidth = menuWidth
            this.horizontalMargin = horizontalMargin
            this.location = location
        })
    }

    fun setParams(list: List<T>, bean: MenuBean? = MenuBean()) {
        mBinding?.apply {
            viewArrow.layoutGravity = bean?.getLayoutGravity().orZero
            val horizontalMargin = bean?.horizontalMargin.orZero
            val verticalMargin = bean?.verticalMargin.orZero
            viewArrow.margin(
                top = verticalMargin,
                start = if (bean?.location == Location.START) 10.pt + horizontalMargin else 0,
                end = if (bean?.location == Location.END) 10.pt + horizontalMargin else 0)
            llItem.apply {
                if (0 != bean?.menuWidth) size(width = bean?.menuWidth)
                layoutGravity = bean?.getLayoutGravity().orZero
                margin(
                    top = 10.pt + verticalMargin,
                    start = if (bean?.location == Location.START) horizontalMargin else 0,
                    end = if (bean?.location == Location.END) horizontalMargin else 0)
                removeAllViews()
                list.forEachIndexed { index, t ->
                    addView(SelectItemHolder(llItem, Pair(formatter(t).orEmpty(), index)).let {
                        it.onItemClick = { item, index ->
                            hidden()
                            onCurrent?.invoke(item.orEmpty(), index)
                        }
                        it.mBinding.root
                    })
                }
            }
        }
    }

    fun setOnItemClickListener(onCurrent: ((item: String, index: Int) -> Unit)) {
        this.onCurrent = onCurrent
    }

}

class MenuBean {
    var menuWidth: Int? = ScreenUtil.screenWidth//父item的宽度，不传则为手机宽度
    var verticalMargin: Int? = 0//距上下默认位置（默认就在view下放，贴合）
    var horizontalMargin: Int? = 15.pt//距左右侧默认位置
    var location: Location? = Location.END//菜单位置（左右侧）

    fun getLayoutGravity(): Int {
        return if (location == Location.END) Gravity.END else Gravity.START
    }
}

enum class Location {
    START, END
}
