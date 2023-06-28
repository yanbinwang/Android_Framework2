package com.example.common.utils.i18n

import com.example.common.bean.LanguageBean
import com.example.common.config.CacheData.language
import com.example.common.utils.i18n.Language.Companion.en_US
import com.example.common.utils.i18n.Language.Companion.in_ID
import com.example.common.utils.i18n.Language.Companion.zh_TW
import com.example.common.utils.i18n.LanguagePackAsset.Companion.en_US_PACK
import com.example.common.utils.i18n.LanguagePackAsset.Companion.in_ID_PACK
import com.example.common.utils.i18n.LanguagePackAsset.Companion.zh_TW_PACK
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
            I18nUtil.setLanguagePack(en_US, LanguageBean())
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
            en_US -> en_US_PACK
            else -> en_US_PACK
        }
    }

    /**
     * 根據取到的手機語言切換對應語言包
     */
    fun resetLanguage() {
        when (Locale.getDefault().language.lowercase()) {
            "in" -> language.set(in_ID)
            "zh" -> language.set(zh_TW)
            "en" -> language.set(en_US)
            else -> language.set(en_US)
        }
    }

    /**
     * 检测语言包是否需要更新为本地版本的
     */
    fun checkLanguageVersion(language: String?) {
        if (language.isNullOrEmpty()) {
            I18nUtil.setLanguagePack(en_US, LanguageBean())
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
        /**繁中*/
        const val zh_TW = "zh_HK"

        /**英语*/
        const val en_US = "en_US"

        /**印尼语*/
        const val in_ID = "id_ID"
    }
}

annotation class LanguagePackAsset {
    companion object {
        /**繁中*/
        const val zh_TW_PACK = "zh_tw.json"

        /**英语*/
        const val en_US_PACK = "en_us.json"

        /**印尼语*/
        const val in_ID_PACK = "in_id.json"
    }
}