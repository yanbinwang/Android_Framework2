package com.example.common.widget.i18n

import java.lang.ref.WeakReference

/**
 * @description 全局刷新
 * @author yan
 */
interface I18nImpl {

    /**
     * 刷新賦值的文本
     */
    fun applyI18n()

    /**
     * 返回其本身（弱引用防止內存洩漏）
     */
    fun getI18nRef(): WeakReference<I18nImpl>

}