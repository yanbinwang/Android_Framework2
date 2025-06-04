package com.example.thirdparty.amap

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolygonOptions
import com.example.amap.utils.CoordinateTransUtil
import com.example.common.utils.function.ActivityResultRegistrar
import com.example.common.utils.permission.checkSelfLocation
import com.example.common.utils.toObj
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.gone
import com.example.thirdparty.amap.LocationHelper.Companion.aMapLatLng
import kotlin.math.roundToInt

/**
 *  Created by wangyanbin
 *  高德地图工具类
 */
class MapHelper(private val mActivity: FragmentActivity, registrar: ActivityResultRegistrar) : LifecycleEventObserver {
    private var mapView: MapView? = null
    private val mapLatLng by lazy { aMapLatLng.get().toObj(LatLng::class.java) }//默认地图经纬度-杭州
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

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 绑定地图
     */
    fun bind(savedInstanceState: Bundle?, mapView: MapView?, initLoaded: Boolean = true) {
        this.mapView = mapView
        this.aMap = mapView?.map
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)创建地图
        mapView?.onCreate(savedInstanceState)
        //更改地图view设置
        mapView?.viewTreeObserver?.addOnGlobalLayoutListener {
            try {
                val child = mapView.getChildAt(0) as? ViewGroup //地图框架
                val logo = child?.getChildAt(2)
                logo?.gone() //隐藏logo
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        aMap?.isTrafficEnabled = true //显示实时交通状况
        aMap?.uiSettings?.isRotateGesturesEnabled = false //屏蔽旋转
        aMap?.uiSettings?.isZoomControlsEnabled = false //隐藏缩放插件
        aMap?.uiSettings?.isTiltGesturesEnabled = false //屏蔽双手指上下滑动切换为3d地图
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(18f))
        //初始化定位回调
        location.setOnLocationListener(object : LocationHelper.OnLocationListener {
            override fun onLocationChanged(aMapLocation: AMapLocation?, flag: Boolean) {
                this@MapHelper.aMapLocation = aMapLocation
                if (flag) {
                    moveCamera(latLng)
                } else {
                    moveCamera()
                }
            }

            override fun onGpsSetting(flag: Boolean) {
            }
        })
        //地图加载完成，定位一次，让地图移动到坐标点
        aMap?.setOnMapLoadedListener {
            //是否需要在网络发生改变时，移动地图
            if (initLoaded) {
                //先移动到默认点再检测权限定位
                moveCamera()
                location()
            }
        }
    }

    /**
     * 地图定位
     */
    fun location() {
        if (mActivity.checkSelfLocation().orFalse) location.start()
    }

    /**
     * 地图移动
     */
    fun moveCamera(latLng: LatLng? = mapLatLng, zoom: Float = 18f, anim: Boolean = false) {
        aMap?.apply {
            val update = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            if (anim) animateCamera(update) else moveCamera(update)
        }
    }

    /**
     * 移动到中心点
     */
    fun moveCamera(latLngList: MutableList<LatLng>, zoom: Float = 18f, anim: Boolean = false) {
        moveCamera(CoordinateTransUtil.getCenterPoint(latLngList), zoom, anim)
    }

    /**
     * 需要移动的经纬度，需要移动的范围（米）
     */
    fun adjustCamera(latLng: LatLng, range: Int) {
        //移动地图需要进行一定的换算
        val scale = aMap?.scalePerPixel.toSafeFloat()
        //代表range（米）的像素数量
        val pixel = (range / scale).roundToInt()
        //小范围，小缩放级别（比例尺较大），有精度损失
        val projection = aMap?.projection ?: return
        //将地图的中心点，转换为屏幕上的点
        val center = projection.toScreenLocation(latLng)
        //获取距离中心点为pixel像素的左、右两点（屏幕上的点
        val top = Point(center.x, center.y + pixel)
        //将屏幕上的点转换为地图上的点
        moveCamera(projection.fromScreenLocation(top), 16f, true)
    }

    /**
     * 添加覆盖物
     */
    fun addMarker(latLng: LatLng, view: View, json: String = "") {
        //将标识绘制在地图上
        val markerOptions = MarkerOptions()
        val bitmap = BitmapDescriptorFactory.fromView(view)
        markerOptions.position(latLng) //设置位置
            .icon(bitmap) //设置图标样式
            .anchor(0.5f, 0.5f)
            .zIndex(9f) //设置marker所在层级
            .draggable(false) //设置手势拖拽
        //给地图覆盖物加上额外的集合数据（点击时候取）
        val marker = aMap?.addMarker(markerOptions)
//        marker?.title = json
        marker?.snippet = json//使用摘录比title更规范些
        marker?.setFixingPointEnable(false) //去除拉近动画
        marker?.isInfoWindowEnable = false //禁止高德地图自己的弹出窗口
    }

    /**
     * 绘制多边形
     */
    fun addPolygon(latLngList: MutableList<LatLng>) {
        // 声明多边形参数对象
        val polygonOptions = PolygonOptions()
        for (latLng in latLngList) {
            polygonOptions.add(latLng)
        }
        polygonOptions.strokeWidth(15f) // 多边形的边框
            .strokeColor(Color.argb(50, 1, 1, 1))// 边框颜色
            .fillColor(Color.argb(1, 1, 1, 1))// 多边形的填充色
        aMap?.addPolygon(polygonOptions)
    }

    /**
     * 判断经纬度是否在多边形范围内
     */
    fun isPolygonContainsPoint(latLngList: MutableList<LatLng>, point: LatLng): Boolean {
        val options = PolygonOptions()
        for (index in latLngList.indices) {
            options.add(latLngList[index])
        }
        options.visible(false)//设置区域是否显示
        val polygon = aMap?.addPolygon(options)
        val contains = polygon?.contains(point)
        polygon?.remove()
        return contains.orFalse
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
                mapView?.onDestroy()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}