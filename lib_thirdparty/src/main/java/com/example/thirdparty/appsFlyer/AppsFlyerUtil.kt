package com.example.thirdparty.appsFlyer

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.example.framework.utils.function.value.isDebug

/**
 * yan
 */
object AppsFlyerUtil {
    val appsFlyerInstance get() = AppsFlyerLib.getInstance()
    private var isInit = false

    fun init(instance: Application, channelId: String? = null) {
        val afDevKey = try {
            instance.packageManager
                .getApplicationInfo(instance.packageName, PackageManager.GET_META_DATA)
                .metaData["AF_DEV_KEY"]?.toString() ?: return
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return
        }
        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                conversionData.forEach {
                    "onConversionDataSuccess attribute: ${it.key} = ${it.value}".logE
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                "onConversionDataFail error getting conversion data: $errorMessage".logE
            }

            override fun onAppOpenAttribution(map: Map<String, String>) {
                map.forEach {
                    "onAppOpenAttribution attribute: ${it.key} = ${it.value}".logE
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                "error onAttributionFailure : $errorMessage".logE
            }
        }
        appsFlyerInstance.init(afDevKey, conversionListener, instance)
        appsFlyerInstance.start(instance)

        if (!channelId.isNullOrEmpty()) {
            AppsFlyerLib.getInstance().setOutOfStore(channelId)
        }
        isInit = true

    }

    fun track(context: Context?, event: String?, bundle: Bundle?) {
        if (!isInit) return
        appsFlyerInstance.logEvent(context, event, bundleToMap(bundle))
    }

    private fun bundleToMap(extras: Bundle?): Map<String, Any?>? {
        if (extras == null) return null
        val map: MutableMap<String, Any?> = HashMap()
        val ks = extras.keySet()
        for (key in ks) {
            map[key] = extras[key]
        }
        return map
    }

    fun initDeepLink(onDeepLink: (String?) -> Unit) {
        appsFlyerInstance.subscribeForDeepLink(DeepLinkListener { deepLinkResult ->
            val dlStatus = deepLinkResult.status
            if (dlStatus == DeepLinkResult.Status.FOUND) {
                val value = deepLinkResult.deepLink.getStringValue("deep_link_value")
                "Deep link is: $value".logE
                onDeepLink(value)
            } else if (dlStatus == DeepLinkResult.Status.NOT_FOUND) {
                "Deep link not found".logE
                return@DeepLinkListener
            } else {
                // dlStatus == DeepLinkResult.Status.ERROR
                "There was an error getting Deep Link data: ${deepLinkResult.error}".logE
                return@DeepLinkListener
            }
        })
    }

    private val String.logE get() = if (isDebug) Log.e("AppsFlyer", this) else null
}