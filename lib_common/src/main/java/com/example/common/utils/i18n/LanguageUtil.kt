package com.example.common.utils.i18n

import com.example.common.BaseApplication
import com.example.common.bean.LanguageBean
import com.example.common.config.CacheData.language
import com.example.common.utils.i18n.Language.Companion.en_US
import com.example.common.utils.i18n.Language.Companion.in_ID
import com.example.common.utils.i18n.Language.Companion.zh_TW
import com.example.common.utils.i18n.LanguagePackAsset.Companion.en_US_PACK
import com.example.common.utils.i18n.LanguagePackAsset.Companion.in_ID_PACK
import com.example.common.utils.i18n.LanguagePackAsset.Companion.zh_TW_PACK
import com.example.framework.utils.function.value.toSafeInt
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * 语言选项工具类 (默认英语)
 */
object LanguageUtil {

    /**
     * 設置語言
     */
    fun setLanguage(@Language packName: String) {
        language.set(packName)
    }

    /**
     * 獲取服务器需要的用於识别的语言名
     */
    @Language
    fun getLanguage(): String {
        return language.get() ?: en_US
    }

    /**
     * 設置本機的語言
     */
    fun applyLanguage(language: String? = getLanguage()) {
        if (language.isNullOrEmpty()) {
            I18nUtil.apply(en_US, LanguageBean())
            return
        }
        val bean = I18nUtil.getLanguageBeanFromAsset(language) ?: return
        if (bean.data.isNullOrEmpty()) return
        // 语言包版本相同也可以进行更新，这里主要是用作同版本语言切换
        if (bean.version.toSafeInt() >= I18nUtil.getLanguageBeanVersion()) {
            I18nUtil.apply(language, bean)
        }
    }

    /**
     * 根據取到的手機語言切換對應語言
     */
    fun resetLanguage() {
        setLanguage(
            when (Locale.getDefault().language.lowercase()) {
                "zh" -> zh_TW
                "en" -> en_US
                "in" -> in_ID
                else -> en_US
            }
        )
    }

    /**
     * 检测语言包是否需要更新为本地版本的
     */
    fun checkLanguage(language: String? = getLanguage()) {
        if (language.isNullOrEmpty()) {
            I18nUtil.apply(en_US, LanguageBean())
            return
        }
        val version = getLanguageVersionFromAsset(language) ?: return
        // 只有语言包版本大于缓存版本需要强制更新，这里主要用作更新后语言包的强制更新
        if (version > I18nUtil.getLanguageBeanVersion()) {
            val bean = I18nUtil.getLanguageBeanFromAsset(language) ?: return
            I18nUtil.apply(language, bean)
        }
    }

    /**
     * 获取目前选定语言的本地json
     */
    fun getLanguageFromAsset(language: String? = getLanguage()): String {
        return when (language) {
            zh_TW -> zh_TW_PACK
            en_US -> en_US_PACK
            in_ID -> in_ID_PACK
            else -> en_US_PACK
        }
    }

    /**
     * 獲取本機語言包版本
     */
    fun getLanguageVersionFromAsset(language: String): Int? {
        val pack = getLanguageFromAsset(language)
        val assetManager = BaseApplication.instance.applicationContext.assets
        return try {
            // 打开指定语言包，失败直接返回0
            assetManager.open(pack).use { input ->
                // 指定UTF-8编码，避免系统默认编码问题；use自动关闭流
                InputStreamReader(input, Charsets.UTF_8).use { inputReader ->
                    BufferedReader(inputReader).use { reader ->
                        var count = 0
                        var version: Int? = null
                        // 最多读3行，找到版本号立即退出
                        while (reader.ready() && count < 3 && version == null) {
                            count++
                            // 空行直接跳过
                            val line = reader.readLine() ?: continue
                            // 匹配版本号并转换
                            val versionStr = Regex("""(?<="version"\s?:\s?")\d*(?=")""").find(line)?.value
                            if (!versionStr.isNullOrEmpty()) {
                                version = versionStr.toSafeInt()
                            }
                        }
                        // 返回匹配到的版本号（null则表示未找到）
                        version
                    }
                }
            }
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取web端需要的语言字段
     */
    fun getLanguageWebHeader(): String {
        return when (getLanguage()) {
            zh_TW -> "zh"
            en_US -> "en"
            in_ID -> "in"
            else -> "en"
        }
    }

}

/**
 * 定義本地語種和json文件命名
 */
annotation class Language {
    companion object {
        // 繁中
        const val zh_TW = "zh_TW"
        // 英语
        const val en_US = "en_US"
        // 印尼语
        const val in_ID = "id_ID"
    }
}

annotation class LanguagePackAsset {
    companion object {
        // 繁中
        const val zh_TW_PACK = "zh_tw.json"
        // 英语
        const val en_US_PACK = "en_us.json"
        // 印尼语
        const val in_ID_PACK = "in_id.json"
    }
}