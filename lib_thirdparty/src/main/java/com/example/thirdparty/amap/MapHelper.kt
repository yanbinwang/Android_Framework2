package com.example.thirdparty.amap

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.example.amap.utils.moveToLatLng
import com.example.amap.utils.setZoomLevel
import com.example.common.utils.function.ActivityResultRegistrar
import com.example.common.utils.permission.checkSelfLocation
import com.example.common.utils.toObj
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.gone
import com.example.thirdparty.amap.LocationHelper.Companion.aMapLatLng

/**
 *  Created by wangyanbin
 *  高德地图工具类 (https://lbs.amap.com/api/android-sdk/guide/computing-equipment/coordinate-transformation)
 *  override fun onSaveInstanceState(outState: Bundle) {
 *     super.onSaveInstanceState(outState)
 *     helper.saveInstanceState(outState)
 * }
 *
 * override fun initView(savedInstanceState: Bundle?) {
 *     super.initView(savedInstanceState)
 *     //绑定地图，让地图移动到传入的经纬度点
 *     helper.bind(savedInstanceState, mBinding?.aMap)
 *     helper.moveCamera(LatLng(bean?.latitude.orZero, bean?.longitude.orZero))
 *     //禁止滑动
 *     helper.aMap?.uiSettings?.isScrollGesturesEnabled = false
 * }
 */
class MapHelper(private val mActivity: FragmentActivity, registrar: ActivityResultRegistrar) : LifecycleEventObserver {
    private var mapView: MapView? = null
    private val mapLatLng by lazy { aMapLatLng.get().toObj(LatLng::class.java) ?: LatLng(0.0, 0.0) } // 默认地图经纬度-杭州
    private val location by lazy { LocationHelper(mActivity, registrar) }
    /**
     * 地址控件
     */
    var aMap: AMap? = null
        private set
    /**
     * 详细定位类
     */
    var aMapLocation: AMapLocation? = null
        private set
    /**
     * 經緯度
     */
    val latLng get() = LatLng(aMapLocation?.latitude.orZero, aMapLocation?.longitude.orZero)
    /**
     * 是否定位成功
     */
    val isSuccessful get() = aMapLocation != null

    /**
     * 绑定页面生命周期
     */
    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 绑定地图
     */
    fun bind(savedInstanceState: Bundle?, mapView: MapView?, initLoaded: Boolean = true) {
        this.mapView = mapView
        this.aMap = mapView?.map
        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)创建地图
        mapView?.onCreate(savedInstanceState)
        // 更改地图view设置
        mapView?.viewTreeObserver?.addOnGlobalLayoutListener {
            try {
                // 地图框架
                val child = mapView.getChildAt(0) as? ViewGroup
                val logo = child?.getChildAt(2)
                // 隐藏logo
                logo?.gone()
                // 映射内部方法
                val uiSettings = aMap?.uiSettings
                val method = uiSettings?.javaClass?.getDeclaredMethod("setLogoEnable", Boolean::class.javaPrimitiveType)
                method?.isAccessible = true
                method?.invoke(uiSettings, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 显示实时交通状况
        aMap?.isTrafficEnabled = true
        // 屏蔽旋转
        aMap?.uiSettings?.isRotateGesturesEnabled = false
        // 隐藏缩放插件
        aMap?.uiSettings?.isZoomControlsEnabled = false
        // 屏蔽双手指上下滑动切换为3d地图
        aMap?.uiSettings?.isTiltGesturesEnabled = false
        // 调整缩放级别
        aMap?.setZoomLevel(18f)
        // 初始化定位回调
        location.setOnLocationListener(object : LocationHelper.OnLocationListener {
            override fun onLocationChanged(aMapLocation: AMapLocation?, flag: Boolean) {
                this@MapHelper.aMapLocation = aMapLocation
                if (flag) {
                    aMap.moveToLatLng(latLng)
                } else {
                    aMap.moveToLatLng(mapLatLng)
                }
            }

            override fun onGpsSetting(flag: Boolean) {
            }
        })
        // 地图加载完成，定位一次，让地图移动到坐标点
        aMap?.setOnMapLoadedListener {
            // 是否需要在网络发生改变时，移动地图
            if (initLoaded) {
                // 先移动到默认点再检测权限定位
                aMap.moveToLatLng(mapLatLng)
                location()
            }
        }
    }

    /**
     * 地图定位
     */
    fun location() {
        if (mActivity.checkSelfLocation()) {
            location.start()
        }
    }

    /**
     * 存储-保存地图当前的状态（对应页面调取）
     */
    fun saveInstanceState(outState: Bundle) {
        mapView?.onSaveInstanceState(outState)
    }

    /**
     * 生命周期管控
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                mapView?.onResume()
            }
            Lifecycle.Event.ON_PAUSE -> {
                mapView?.onPause()
            }
            Lifecycle.Event.ON_DESTROY -> {
                aMapLocation = null
                mapView?.onDestroy()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}