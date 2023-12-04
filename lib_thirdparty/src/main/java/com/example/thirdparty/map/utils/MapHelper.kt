package com.example.thirdparty.map.utils

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolygonOptions
import com.example.common.utils.function.toObj
import com.example.common.utils.permission.checkSelfLocation
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.gone
import com.example.thirdparty.map.utils.LocationFactory.Companion.aMapLatlng
import kotlin.math.roundToInt

/**
 *  Created by wangyanbin
 *  高德地图工具类
 */
class MapHelper(lifecycleOwner: LifecycleOwner) : LifecycleEventObserver {
    private var mapView: MapView? = null
    private var aMap: AMap? = null
    private val mapLatLng by lazy { aMapLatlng.get().toObj(LatLng::class.java) }//默认地图经纬度-杭州

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * 绑定地图
     */
    fun bind(savedInstanceState: Bundle?, mapView: MapView, initialize: Boolean = true) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)创建地图
        mapView.onCreate(savedInstanceState)
        //更改地图view设置
        mapView.viewTreeObserver.addOnGlobalLayoutListener {
            val child = mapView.getChildAt(0) as? ViewGroup //地图框架
            val logo = child?.getChildAt(2)
            logo?.gone() //隐藏logo
        }
        this.mapView = mapView
        this.aMap = mapView.map
        aMap?.isTrafficEnabled = true //显示实时交通状况
        aMap?.uiSettings?.isRotateGesturesEnabled = false //屏蔽旋转
        aMap?.uiSettings?.isZoomControlsEnabled = false //隐藏缩放插件
        aMap?.uiSettings?.isTiltGesturesEnabled = false //屏蔽双手指上下滑动切换为3d地图
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(18f))
        //初始化定位回调
        LocationFactory.instance.setOnLocationListener { location, flag ->
            if (flag) {
                moveCamera(LatLng(location?.latitude.orZero, location?.longitude.orZero))
            } else {
                moveCamera()
            }
        }
        //是否需要在网络发生改变时，移动地图
        if (initialize) {
            //地图加载完成，定位一次，让地图移动到坐标点
            aMap?.setOnMapLoadedListener {
                //先移动到默认点再检测权限定位
                moveCamera()
                if (mapView.context.checkSelfLocation()) location()
            }
        }
    }

    /**
     * 地图定位
     */
    fun location() {
        LocationFactory.instance.start()
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
    fun moveCamera(latLngList: MutableList<LatLng>, zoom: Float = 18f, anim: Boolean = false) = moveCamera(CoordinateTransUtil.getCenterPoint(latLngList), zoom, anim)

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
        marker?.title = json
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
        return contains ?: false
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> {}
        }
    }

    /**
     * 存储-保存地图当前的状态（对应页面调取）
     */
    fun saveInstanceState(outState: Bundle) = mapView?.onSaveInstanceState(outState)

    /**
     * 加载
     */
    private fun onResume() = mapView?.onResume()

    /**
     * 暂停
     */
    private fun onPause() = mapView?.onPause()

    /**
     * 销毁
     */
    private fun onDestroy() = mapView?.onDestroy()

}