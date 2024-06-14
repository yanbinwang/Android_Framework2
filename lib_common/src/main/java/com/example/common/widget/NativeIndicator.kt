package com.example.common.widget

import com.example.common.R
import com.example.common.databinding.ItemTabBinding
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.function.setI18nTheme
import com.example.common.widget.i18n.I18nTextView
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.textSize
import com.google.android.material.tabs.TabLayout

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 *      <com.google.android.material.tabs.TabLayout
 *             android:id="@+id/tb_menu"
 *             android:layout_width="match_parent"
 *             android:layout_height="44pt"
 *             android:background="@color/bgWhite"
 *             android:clipChildren="true"
 *             android:clipToPadding="false"
 *             android:theme="@style/TabLayoutStyle"
 *             app:tabIndicator="@drawable/layer_list_tab_line"
 *             app:tabMinWidth="0dp"
 *             app:tabMode="scrollable"
 *             app:tabPaddingBottom="0dp"
 *             app:tabPaddingEnd="0dp"
 *             app:tabPaddingStart="0dp"
 *             app:tabPaddingTop="0dp" />
 */
class NativeIndicator(tab: TabLayout?, tabTitle: List<Int>?) : TabLayoutBuilder<Int, ItemTabBinding>(tab, tabTitle) {
    private var redraw: ((binding: ItemTabBinding?, item: Int?, selected: Boolean, index: Int) -> Unit)? = null//如需自定義，重寫此監聽

    override fun getBindView() = ItemTabBinding.bind(mContext.inflate(R.layout.item_tab))

    override fun onBindView(mBinding: ItemTabBinding?, item: Int?, selected: Boolean, index: Int) {
        if(null == redraw) {
            mBinding?.tvTitle.setI18nTabTheme(item.orZero, selected)
        } else {
            redraw?.invoke(mBinding, item, selected, index)
        }
    }

    /**
     * 重写此方法表示部分标题字体字号样式等需要使用非默认配置
     * 需在调用bind（）方法前调取，一旦绑定就会执行，此时监听没赋值，控件会显示不出
     */
    fun setRedraw(redraw: ((mBinding: ItemTabBinding?, item: Int?, selected: Boolean, index: Int) -> Unit)) {
        this.redraw = redraw
    }

}

/**
 * 全局默认样式
 */
fun I18nTextView?.setI18nTabTheme(resText: Int = -1, selected: Boolean, colorRes: Pair<Int, Int> = R.color.tabSelected to R.color.tabUnselected, sizeRes: Pair<Int, Int> = R.dimen.textSize16 to R.dimen.textSize15, padding: Pair<Int, Int> = 6.pt to 6.pt) {
    setI18nTheme(resText, if (selected) colorRes.first.orZero else colorRes.second.orZero,)
    textSize(if (selected) sizeRes.first.orZero else sizeRes.second.orZero)
    padding(start = padding.first, end = padding.second)
    bold(selected)
}