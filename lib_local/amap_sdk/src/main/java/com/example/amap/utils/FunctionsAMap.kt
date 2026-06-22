package com.example.amap.utils

import android.graphics.Color
import android.graphics.Point
import android.graphics.PointF
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolygonOptions
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.toSafeFloat
import kotlin.math.roundToInt

// ==============================
//  1) 出行规划 / 查询等可参考高德官方文档 : https://lbs.amap.com/api/android-sdk/guide/computing-equipment/coordinate-transformation
//  2) 该类是对地图和经纬度类的扩展
// ==============================

/**
 * 其他坐标系 -> 高德坐标系（GPS/百度/图吧等）
 */
fun CoordinateConverter.convert(sourceLatLng: LatLng, type: CoordinateConverter.CoordType = CoordinateConverter.CoordType.GPS): LatLng {
    // CoordType.GPS 待转换坐标类型
    from(type)
    // sourceLatLng待转换坐标点 LatLng类型
    coord(sourceLatLng)
    // 执行转换操作
    return convert()
}

/**
 * 两点间的直线距离计算
 * latLng1,latLng2 -> 起点 / 终点
 */
fun LatLng.calculateLineDistance(latLng: LatLng): Float {
    return AMapUtils.calculateLineDistance(this,latLng)
}

/**
 * 面积计算
 * leftTopLatlng,rightBottomLatlng -> 左上 / 右下
 */
fun LatLng.calculateArea(latLng: LatLng): Float {
    return AMapUtils.calculateArea(this,latLng)
}

/**
 * 屏幕坐标 Point -> 经纬度 LatLng
 */
fun AMap?.screenToLatLng(paramPoint: Point): LatLng {
    this ?: return LatLng(0.0, 0.0)
    return projection.fromScreenLocation(paramPoint)
}

/**
 * 经纬度 LatLng -> 屏幕坐标 Point
 * 经纬度 → 屏幕像素（int）
 */
fun AMap?.latLngToScreen(paramLatLng: LatLng): Point {
    this ?: return Point(0, 0)
    return projection.toScreenLocation(paramLatLng)
}

/**
 * 经纬度 -> OpenGL 坐标（PointF）
 * Point -> 存 int 整数（屏幕像素用）
 * PointF -> 存 float 小数（地图精确坐标用）
 */
fun AMap?.latLngToOpenGL(paramLatLng: LatLng): PointF {
    this ?: return PointF(0f, 0f)
    return projection.toOpenGLLocation(paramLatLng)
}

/**
 * 屏幕宽度转 OpenGL 世界宽度
 * @screenWidth 地图 View 的屏幕像素宽度（px）
 * @return 该宽度在地图 OpenGL 世界坐标系中的长度（float）
 */
fun AMap?.screenWidthToOpenGL(screenWidth: Int): Float {
    this ?: return 0f
    return projection.toOpenGLWidth(screenWidth)
}

/**
 * 移动地图到指定经纬度
 */
fun AMap?.moveToLatLng(latLng: LatLng, zoom: Float = 18f, anim: Boolean = false) {
    this ?: return
    val update = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
    if (anim) animateCamera(update) else moveCamera(update)
}

/**
 * 移动到点集合的中心点
 */
fun AMap?.moveToCenterPoint(latLngs: MutableList<LatLng>, zoom: Float = 18f, anim: Boolean = false) {
    this ?: return
    moveToLatLng(CoordinateUtil.calculateCenterPoint(latLngs), zoom, anim)
}

/**
 * 只调整缩放级别
 */
fun AMap?.setZoomLevel(zoom: Float, anim: Boolean = false) {
    this ?: return
    val update = CameraUpdateFactory.zoomTo(zoom)
    if (anim) animateCamera(update) else moveCamera(update)
}

/**
 * 根据范围（米）自动调整视野
 */
fun AMap?.adjustCameraByRange(latLng: LatLng, range: Int) {
    this ?: return
    // 移动地图需要进行一定的换算
    val scale = scalePerPixel.toSafeFloat()
    // 代表range（米）的像素数量
    val pixel = (range / scale).roundToInt()
    // 小范围，小缩放级别（比例尺较大），有精度损失，将地图的中心点，转换为屏幕上的点
    val center = latLngToScreen(latLng)
    // 获取距离中心点为pixel像素的左、右两点（屏幕上的点
    val top = Point(center.x, center.y + pixel)
    // 将屏幕上的点转换为地图上的点
    moveToLatLng(screenToLatLng(top), 16f, true)
}

/**
 * 添加自定义 View 标记点
 */
fun AMap?.addMarkerByView(latLng: LatLng, view: View, extra: String = "") {
    this ?: return
    // 声明覆盖物参数对象 / 给地图覆盖物加上额外的集合数据（点击时候获取）
    val marker = addMarker(MarkerOptions()
        // 设置位置
        .position(latLng)
        // 设置图标样式
        .icon(BitmapDescriptorFactory.fromView(view))
        // 图标正中心对准经纬度坐标点
        .anchor(0.5f, 0.5f)
        // 设置marker所在层级 (值越大越在上方)
        .zIndex(9f)
        // 设置手势是否可拖拽
        .draggable(false))
//    marker?.title = extra
    // 使用摘录比title更规范些
    marker?.snippet = extra
    // 去除拉近动画
    marker?.setFixingPointEnable(false)
    // 禁止高德地图自己的弹出窗口
    marker?.isInfoWindowEnable = false
}

/**
 * 绘制多边形
 */
fun AMap?.addPolygonFill(latLngs: MutableList<LatLng>) {
    this ?: return
    // 声明多边形参数对象
    addPolygon(PolygonOptions()
        // 添加经纬度集合
        .addAll(latLngs)
        // 多边形的边框
        .strokeWidth(15f)
        // 边框颜色
        .strokeColor(Color.argb(50, 1, 1, 1))
        // 多边形的填充色
        .fillColor(Color.argb(1, 1, 1, 1)))
}

/**
 * 判断点是否在多边形内
 */
fun AMap?.isPointInPolygon(latLngs: MutableList<LatLng>, point: LatLng): Boolean {
    this ?: return false
    val polygon = addPolygon(PolygonOptions()
        // 添加经纬度集合
        .addAll(latLngs)
        // 设置区域是否显示
        .visible(false))
    val contains = polygon?.contains(point).orFalse
    polygon?.remove()
    return contains
}