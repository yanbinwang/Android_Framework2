package com.example.common.utils

import com.example.common.bean.LanguageBean
import com.example.common.config.CacheData.language
import com.example.common.utils.Language.Companion.en_IN
import com.example.common.utils.Language.Companion.in_ID
import com.example.common.utils.Language.Companion.zh_TW
import com.example.common.utils.LanguagePackAsset.Companion.en_IN_PACK
import com.example.common.utils.LanguagePackAsset.Companion.in_ID_PACK
import com.example.common.utils.LanguagePackAsset.Companion.zh_TW_PACK
import com.example.framework.utils.function.value.toSafeInt
import java.util.Locale

/**
 * @description 國家工具類
 * @author yan
 */
object LanguageUtil {

    /**
     * 設置本機的語言
     */
    fun setLocalLanguage(language: String?) {
        if (language.isNullOrEmpty()) {
            I18nUtil.setLanguagePack(en_IN, LanguageBean())
            return
        }
        val bean = I18nUtil.getLocalLanguageBean(language) ?: return
        if (bean.data.isNullOrEmpty()) return
        //语言包版本相同也可以进行更新，这里主要是用作同版本语言切换
        if (bean.version.toSafeInt() >= I18nUtil.getPackVersion()) {
            I18nUtil.setLanguagePack(language, bean)
        }
    }

    /**
     * 設置語言
     */
    fun setLanguage(languageStr: String) {
        language.set(languageStr)
    }

    /**
     * @return 獲取服务器需要的用於识别的语言名
     */
    @Language
    fun getLanguage(): String {
        return language.get()
    }

    /**
     * @return 获取目前选定语言包的本地json
     */
    fun getLanguageLocalAsset(language: String? = getLanguage()): String {
        return when (language) {
            in_ID -> in_ID_PACK
            zh_TW -> zh_TW_PACK
            en_IN -> en_IN_PACK
            else -> en_IN_PACK
        }
    }

    /**
     * 根據取到的手機語言切換對應語言包
     */
    fun resetLanguage() {
        when (Locale.getDefault().language.lowercase()) {
            "in" -> language.set(in_ID)
            "zh" -> language.set(zh_TW)
            "en" -> language.set(en_IN)
            else -> language.set(en_IN)
        }
    }

    /**
     * 检测语言包是否需要更新为本地版本的
     */
    fun checkLanguageVersion(language: String?) {
        if (language.isNullOrEmpty()) {
            I18nUtil.setLanguagePack(en_IN, LanguageBean())
            return
        }
        val version = I18nUtil.getLocalLanguageVersion(language) ?: return
        //只有语言包版本大于缓存版本需要强制更新，这里主要用作更新后语言包的强制更新
        if (version > I18nUtil.getPackVersion()) {
            val bean = I18nUtil.getLocalLanguageBean(language) ?: return
            I18nUtil.setLanguagePack(language, bean)
        }
    }

}

/**
 * 定義本地語種和json文件命名
 */
annotation class Language {
    companion object {
        /**印尼语*/
        const val in_ID = "id_ID"

        /**繁中*/
        const val zh_TW = "zh_HK"

        /**印度英语*/
        const val en_IN = "en_IN"
    }
}

annotation class LanguagePackAsset {
    companion object {
        /**印尼语*/
        const val in_ID_PACK = "in_id.json"

        /**繁中*/
        const val zh_TW_PACK = "zh_tw.json"

        /**印度英语*/
        const val en_IN_PACK = "en_in.json"
    }
}