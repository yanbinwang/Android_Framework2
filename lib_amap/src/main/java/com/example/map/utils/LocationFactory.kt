package com.example.map.utils

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.example.common.BaseApplication
import com.example.common.config.Constants
import com.example.common.utils.DataStringCacheUtil
import com.example.common.utils.function.toJsonString
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.string
import com.example.map.R

/**
 *  Created by wangyanbin
 *  定位-必须要有定位权限，否则定位失败，可以不开gps会走网络定位
 *  定位工具类写成class避免每次init都要初始化
 *  1.先实现回调
 *  2.key文件一定要校准
 *  3.选择3d地图定位套件
 */
class LocationFactory private constructor() : AMapLocationListener {
    private val context get() = BaseApplication.instance.applicationContext
    private var locationClient: AMapLocationClient? = null

    companion object {
        //单例->定位的类不需要每次都重新初始化
        val instance by lazy { LocationFactory() }

        //经纬度json->默认杭州
        private const val AMAP_LATLNG = "map_latlng"
        val aMapLatlng = DataStringCacheUtil(AMAP_LATLNG, "{latitude:30.2780010000,longitude:120.1680690000}")

        //回调监听
        internal var onShutter: (location: AMapLocation?, flag: Boolean) -> Unit = { _, _ -> }

        /**
         * flag->true表示定位成功，当不一定有定位对象，false表示定位失败，一定不会有定位对象
         */
        fun setOnLocationListener(onShutter: (location: AMapLocation?, flag: Boolean) -> Unit) {
            this.onShutter = onShutter
        }

        /**
         * 跳转设置gps
         */
        fun FragmentActivity.settingGps(listener: (flag: Boolean) -> Unit = {}): Boolean {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            //判断GPS模块是否开启，如果没有则开启
            return if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AppDialog(this).apply {
                    onConfirm = {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                            if (it.resultCode == Activity.RESULT_OK) {
                                listener.invoke(true)
                            }
                        }.launch(intent)
                    }
                    setParams(
                        string(R.string.hint),
                        string(R.string.map_gps),
                        string(R.string.map_gps_go_setting),
                        string(R.string.cancel)
                    )
                    show()
                }
                false
            } else true
        }

        /**
         * 高德内部构建定位通知
         */
        private fun Context.buildNotification(): Notification {
            val builder: Notification.Builder?
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                val notificationChannel = NotificationChannel(Constants.PUSH_CHANNEL_ID, Constants.PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.apply {
                    enableLights(true) //是否在桌面icon右上角展示小圆点
                    lightColor = Color.BLUE //小圆点颜色
                    setShowBadge(true) //是否在久按桌面图标时显示此渠道的通知
                }
                notificationManager?.createNotificationChannel(notificationChannel)
                builder = Notification.Builder(this, Constants.PUSH_CHANNEL_ID)
            } else builder = Notification.Builder(this)
            builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Constants.APPLICATION_NAME)
                .setContentText("正在定位...")
                .setWhen(System.currentTimeMillis())
            return builder.build()
        }
    }

    init {
        //初始化定位
        locationClient = AMapLocationClient(context)
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
            httpTimeOut = 8000
        }
        //设置定位参数
        locationClient?.setLocationOption(aMapLocationClientOption)
        //启动后台定位，第一个参数为通知栏ID，建议整个APP使用一个
        locationClient?.enableBackgroundLocation(2001, context.buildNotification())
    }


    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null && aMapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            aMapLatlng.set(LatLng(aMapLocation.latitude, aMapLocation.longitude).toJsonString())
            //部分地区可能地址取到为空，直接赋值一个未获取地址的默认显示文案
            if (aMapLocation.address.isNullOrEmpty()) aMapLocation.address = "未获取到地址"
            onShutter.invoke(aMapLocation, true)
        } else {
            onShutter.invoke(null, false)
        }
        stop()
    }

    /**
     * 开始定位(高德的isStart取到的不是实时的值,直接调取开始或停止内部api会做判断)
     * 必须具备定位权限,不区分安卓版本！用于打卡，签到，地图矫正
     */
    fun start() {
        try {
            locationClient?.startLocation()
        } catch (_: Exception) {
            onShutter.invoke(null, false)
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

}