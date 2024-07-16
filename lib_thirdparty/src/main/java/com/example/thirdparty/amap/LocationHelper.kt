package com.example.thirdparty.amap

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.example.common.config.Constants.APPLICATION_NAME
import com.example.common.utils.DataStringCacheUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.registerResult
import com.example.common.utils.function.string
import com.example.common.utils.toJsonString
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.orFalse
import com.example.thirdparty.R

/**
 *  Created by wangyanbin
 *  定位-必须要有定位权限，否则定位失败，可以不开gps会走网络定位
 *  定位工具类写成class避免每次init都要初始化
 *  1.先实现回调
 *  2.key文件一定要校准
 *  3.选择3d地图定位套件
 */
class LocationHelper(private val mActivity: FragmentActivity) : AMapLocationListener, LifecycleEventObserver {
    private var retry = false
    private var locationClient: AMapLocationClient? = null
    private var listener: OnLocationListener? = null
    private val retryTime = 8000L
    private val handler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val manager by lazy { mActivity.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }
    private val mDialog by lazy { AppDialog(mActivity) }
    private val result = mActivity.registerResult {
        if (it.resultCode == Activity.RESULT_OK) {
            listener?.onGpsSetting(true)
        } else {
            listener?.onGpsSetting(false)
        }
    }

    companion object {
        //经纬度json->默认杭州
        private const val AMAP_JSON = "{latitude:30.2780010000,longitude:120.1680690000}"
        private const val AMAP_LATLNG = "map_latlng"
        internal val aMapLatLng = DataStringCacheUtil(AMAP_LATLNG, AMAP_JSON)
    }

    init {
        mActivity.lifecycle.addObserver(this)
        //初始化定位
        locationClient = AMapLocationClient(mActivity)
        //初始化定位参数
        val aMapLocationClientOption = AMapLocationClientOption()
        //设置定位监听
        locationClient?.setLocationListener(this)
        aMapLocationClientOption.apply {
            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //设置是否gps优先，只在高精度模式下有效
            isGpsFirst = true
            //设置定位场景为出行
            locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn
            //设置定位间隔,单位毫秒,默认为2000ms
            interval = 1000
            //true表示允许外界在定位SDK通过GPS定位时模拟位置，false表示不允许模拟GPS位置
            isMockEnable = true
            //设置是否返回方向角(取值范围：【0，360】，其中0度表示正北方向，90度表示正东，180度表示正南，270度表示正西)
            isSensorEnable = true
            //启动定位时SDK会返回最近3s内精度最高的一次定位结果（+）
            isOnceLocationLatest = true
            //请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒
            httpTimeOut = retryTime
        }
        //设置定位参数
        locationClient?.setLocationOption(aMapLocationClientOption)
        //启动后台定位，第一个参数为通知栏ID，建议整个APP使用一个
        locationClient?.enableBackgroundLocation(2001, mActivity.buildNotification())
    }

    /**
     * 高德内部构建定位通知
     */
    private fun Context.buildNotification(): Notification {
        val builder: Notification.Builder?
        //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(string(R.string.notificationChannelId), string(R.string.notificationChannelName), NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.apply {
                enableLights(true) //是否在桌面icon右上角展示小圆点
                lightColor = Color.BLUE //小圆点颜色
                setShowBadge(true) //是否在久按桌面图标时显示此渠道的通知
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(this, string(R.string.notificationChannelId))
        } else {
            builder = Notification.Builder(this)
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(APPLICATION_NAME)
            .setContentText(string(R.string.mapLocationLoading))
            .setWhen(System.currentTimeMillis())
        return builder.build()
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null && aMapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            aMapLatLng.set(LatLng(aMapLocation.latitude, aMapLocation.longitude).toJsonString())
            //部分地区可能地址取到为空，直接赋值一个未获取地址的默认显示文案
            if (aMapLocation.address.isNullOrEmpty()) aMapLocation.address = string(R.string.mapLocationEmpty)
            listener?.onLocationChanged(aMapLocation, true)
        } else {
            aMapLatLng.set(AMAP_JSON)
            listener?.onLocationChanged(null, false)
        }
        stop()
    }

    /**
     * 开始定位(高德的isStart取到的不是实时的值,直接调取开始或停止内部api会做判断)
     * 必须具备定位权限,不区分安卓版本！用于打卡，签到，地图矫正
     */
    fun start() {
        if (retry) {
            R.string.mapLocationProcessing.shortToast()
            return
        }
        retry = true
        handler.postDelayed({ retry = false }, retryTime)
        try {
            locationClient?.startLocation()
        } catch (_: Exception) {
            listener?.onLocationChanged(null, false)
            clear()
        }
    }

    /**
     * 停止定位，在页面OnDestroy调用
     */
    fun stop() {
        //关闭后台定位，参数为true时会移除通知栏，为false时不会移除通知栏，但是可以手动移除
        locationClient?.disableBackgroundLocation(true)
        //结束定位(高德的isStart取到的不是实时的值,直接调取开始或停止内部api会做判断)
        locationClient?.stopLocation()
        //清空消息
        clear()
    }

    /**
     * 清除消息队列
     */
    private fun clear() {
        retry = false
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * 释放，销毁定位客户端调用
     */
    fun destroy() {
        stop()
        locationClient?.unRegisterLocationListener(this)
        locationClient?.onDestroy()
        locationClient = null
    }

    /**
     * flag->true表示定位成功，当不一定有定位对象，false表示定位失败，一定不会有定位对象
     */
    fun setOnLocationListener(listener: OnLocationListener) {
        this.listener = listener
    }

    /**
     * 跳转设置gps
     */
    fun settingGps(): Boolean {
        //判断GPS模块是否开启，如果没有则开启
        return if (!manager?.isProviderEnabled(LocationManager.GPS_PROVIDER).orFalse) {
            mDialog
                .setParams(
                    string(R.string.hint),
                    string(R.string.mapGps),
                    string(R.string.mapGpsGoSetting),
                    string(R.string.cancel))
                .setDialogListener({ result?.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                .show()
            false
        } else {
            true
        }
    }

    /**
     * 生命周期管理
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> stop()
            Lifecycle.Event.ON_DESTROY -> {
                destroy()
                result?.unregister()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    /**
     * 回调监听
     */
    interface OnLocationListener {
        /**
         * 定位详细信息，是否成功
         */
        fun onLocationChanged(aMapLocation: AMapLocation?, flag: Boolean)

        /**
         * gps开启信息，是否开启
         */
        fun onGpsSetting(flag: Boolean)
    }

}