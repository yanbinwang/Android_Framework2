package com.example.common.widget

import androidx.lifecycle.LifecycleOwner
import com.example.common.R
import com.example.common.databinding.ItemTabBinding
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.function.applyI18nTextStyle
import com.example.common.widget.i18n.I18nTextView
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.textSize
import com.google.android.material.tabs.TabLayout

/**
 * Created by wangyanbin
 * 菜单头工具类
 * 掏空系统 TabLayout 全部自定义
 * <com.google.android.material.tabs.TabLayout
 *     android:id="@+id/tb_menu"
 *     android:layout_width="match_parent"
 *     android:layout_height="44pt"
 *     android:background="@color/bgWhite"
 *     android:clipChildren="true"
 *     android:clipToPadding="false"
 *     android:theme="@style/TabLayoutStyle"
 *     app:tabIndicator="@drawable/layer_list_tab_line"
 *     app:tabMinWidth="0dp"
 *     app:tabMode="scrollable"
 *     app:tabPaddingBottom="0dp"
 *     app:tabPaddingEnd="0dp"
 *     app:tabPaddingStart="0dp"
 *     app:tabPaddingTop="0dp" />
 */
open class NativeIndicator(observer: LifecycleOwner, tab: TabLayout?, tabTitle: List<Int>?) : TabLayoutBuilder<Int, ItemTabBinding>(observer, tab, tabTitle) {
    private var itemRenderer: ((binding: ItemTabBinding?, item: Int?, selected: Boolean, index: Int) -> Unit)? = null // 如需自定義，重寫此監聽

    constructor(observer: LifecycleOwner, tab: TabLayout?, vararg data: Int) : this(observer, tab, data.toList())

    override fun getBindView(): ItemTabBinding {
        return ItemTabBinding.bind(getContext().inflate(R.layout.item_tab))
    }

    override fun onBindView(mBinding: ItemTabBinding?, item: Int?, selected: Boolean, index: Int) {
        if(null == itemRenderer) {
            mBinding?.tvTitle.setI18nTabTheme(item.orZero, selected)
        } else {
            itemRenderer?.invoke(mBinding, item, selected, index)
        }
    }

    /**
     * 重写此方法表示部分标题字体字号样式等需要使用非默认配置
     * 需在调用bind（）方法前调取，一旦绑定就会执行，此时监听没赋值控件会显示不出
     */
    fun setItemRenderer(renderer: ((binding: ItemTabBinding?, item: Int?, selected: Boolean, index: Int) -> Unit)) {
        itemRenderer = renderer
    }

}

/**
 * 全局默认样式
 */
fun I18nTextView?.setI18nTabTheme(resText: Int = -1, selected: Boolean, colorRes: Pair<Int, Int> = R.color.tabSelected to R.color.tabUnselected, bgRes: Pair<Int, Int> = -1 to -1, sizeRes: Pair<Int, Int> = R.dimen.textSize16 to R.dimen.textSize15, padding: Pair<Int, Int> = 6 to 6) {
    val (colorSelected, colorUnselected) = colorRes
    val (bgSelected, bgUnselected) = bgRes
    val (sizeSelected, sizeUnselected) = sizeRes
    val (padStart, padEnd) = padding
    applyI18nTextStyle(resText, if (selected) colorSelected else colorUnselected, if (selected) bgSelected else bgUnselected)
    textSize(if (selected) sizeSelected else sizeUnselected)
    padding(start = padStart.pt, end = padEnd.pt)
    bold(selected)
}