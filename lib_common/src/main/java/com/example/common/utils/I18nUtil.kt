package com.example.common.utils

import android.content.Context
import androidx.annotation.StringRes
import com.example.common.BaseApplication
import com.example.common.bean.LanguageBean
import com.example.common.config.CacheData.languageBean
import com.example.common.config.I18nMap
import com.example.common.event.EventCode.EVENT_LANGUAGE_CHANGE
import com.example.common.utils.GsonUtil.gson
import com.example.common.utils.LanguagePackAsset.Companion.en_IN_PACK
import com.example.common.utils.function.resString
import com.example.common.widget.i18n.I18nImpl
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logE
import com.example.framework.utils.logV
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference

/**
 * @description 國際化工具類
 * @author yan
 */
object I18nUtil {
    //用於存儲項目中所有繼承I18nImpl的view
    private val viewList = ArrayList<WeakReference<I18nImpl>>()
    //語言包
    private val languageMap: HashMap<String, String?>
        get() {
            if (languageBean.get() == null) {
                languageBean.set(LanguageBean())
            } else if (languageBean.get()?.data == null) {
                languageBean.get()?.data = HashMap()
            }
            return languageBean.get()?.data ?: HashMap()
        }

    /**
     * 注册
     */
    fun register(textView: I18nImpl) {
        viewList.add(textView.getWeakRef())
        checkList()
    }

    /**
     * 解除注册
     */
    fun unregister(textView: I18nImpl) {
        viewList.remove(textView.getWeakRef())
        checkList()
    }

    /**
     * 检查是否有已被回收的item
     */
    private fun checkList() {
        viewList.filter { it.get() == null }.forEach {
            viewList.remove(it)
        }
    }

    /**
     * 刷新全局语言
     * 對應view內實現語言的資源切換
     */
    fun refreshLanguage() {
        checkList()
        viewList.forEach {
            it.get()?.refreshText()
        }
    }

    /**
     * 获取当前语言对应key的内容，优先级是 国际化text > Resources > ""
     */
    fun getText(ctx: Context, @StringRes res: Int, vararg param: Int): String {
        if (param.isEmpty()) return getText(ctx, res)
        val paramString = param.toNewList { getText(ctx, it) }.toTypedArray()
        return getText(ctx, res, *paramString)
    }

    fun getText(ctx: Context, @StringRes res: Int, vararg param: String): String {
        if (param.isEmpty()) return getText(ctx, res)
        val result = getText(ctx, res)
        return try {
            String.format(result, *param)
        } catch (e: Exception) {
            val sb = StringBuilder()
            param.forEach {
                sb.append("$it ")
            }
            result
        }
    }

    fun getText(ctx: Context, @StringRes res: Int): String {
        return getTextFromI18n(res) ?: getTextFromRes(ctx, res) ?: onResultNull(res)
    }

    /**
     * 获取国际化text
     */
    private fun getTextFromI18n(@StringRes res: Int?): String? {
        val key = I18nMap.map[res]
        if (key.isNullOrEmpty()) {
            if (isDebug) {
                "I18N key is null or empty. res:$res txt:${resString(res.orZero)}".logV
            }
            return null
        }
        return languageMap[key]
    }

    /**
     * 获取Resources中的String
     */
    private fun getTextFromRes(ctx: Context, @StringRes res: Int?): String? {
        if (res == null) return null
        if (res == -1) return null
        return ctx.string(res)
    }

    /**
     * 空处理
     */
    private fun onResultNull(@StringRes res: Int?): String {
        val key = I18nMap.map[res]
        if (isDebug) {
            "No value is set for i18n. res:$res txt:${resString(res.orZero)} key:$key".logV
        }
        return ""
    }

    /**
     * 設置語言後，同步刷新view
     */
    fun setLanguagePack(packName: String, bean: LanguageBean, needRefresh: Boolean = true) {
        LanguageUtil.setLanguage(packName)
        languageBean.set(bean)
        if (needRefresh) {
            refreshLanguage()
            EVENT_LANGUAGE_CHANGE.post()
        }
    }

    /**
     * 讀取assets下的font字體文件
     */
    fun getLocalLanguageBean(language: String?): LanguageBean? {
        val pack = LanguageUtil.getLanguageLocalAsset(language)
        val assetManager = BaseApplication.instance.assets
        val inputStream = try {
            assetManager.open(pack)
        } catch (e: Exception) {
            assetManager.open(en_IN_PACK)
        }
        val reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
        val bean = try {
            gson.fromJson<LanguageBean>(reader, object : TypeToken<LanguageBean>() {}.type)
        } catch (e: Exception) {
            null
        } finally {
            inputStream.close()
            reader.close()
        }
        return bean
    }

    /**
     * 獲取本機語言包版本
     */
    fun getLocalLanguageVersion(language: String?): Int? {
        val pack = LanguageUtil.getLanguageLocalAsset(language)
        val assetManager = BaseApplication.instance.assets
        val inputStream = try {
            assetManager.open(pack)
        } catch (e: Exception) {
            return 0
        }
        val reader = BufferedReader(InputStreamReader(inputStream))
        val version = try {
            var count = 0
            val reg = Regex("""(?<="version"\s?:\s?")\d*(?=")""")
            var result: Int? = null
            while (reader.ready()) {
                count++
                if (count > 3) break
                val line = reader.readLine()
                val value = reg.find(line)?.value
                if (!value.isNullOrEmpty()) {
                    result = value.toSafeInt()
                    break
                }
            }
            result
        } catch (e: java.lang.Exception) {
            null
        } finally {
            try {
                reader.close()
                inputStream.close()
            } catch (e: Exception) {
                e.logE
            }
        }
        return version
    }

    /**
     * 獲取本地存儲的語言包版本
     */
    fun getPackVersion(): Int {
        return languageBean.get()?.version.toSafeInt()
    }

}

/**
 *  <string name="dollar">\$%1$s</string>
 *  string(R.string.dollar, "10086")
 *  $10086
 *  字符串表达式的处理
 *  %n$ms：代表输出的是字符串，n代表是第几个参数，设置m的值可以在输出之前放置空格
 *  %n$md：代表输出的是整数，n代表是第几个参数，设置m的值可以在输出之前放置空格，也可以设为0m,在输出之前放置m个0
 *  %n$mf：代表输出的是浮点数，n代表是第几个参数，设置m的值可以控制小数位数，如m=2.2时，输出格式为00.00
 *  也可简单写成：
 *  %d   （表示整数）
 *  %f   （表示浮点数）
 *  %s   （表示字符串）
 */
fun string(@StringRes res: Int, vararg param: Int): String {
    return I18nUtil.getText(BaseApplication.instance, res, *param)
}

fun string(@StringRes res: Int, vararg param: String): String {
    return I18nUtil.getText(BaseApplication.instance, res, *param)
}

fun string(@StringRes res: Int): String {
    return I18nUtil.getText(BaseApplication.instance, res)
}