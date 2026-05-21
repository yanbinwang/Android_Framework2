package com.example.amap.utils

import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Random
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *  Created by yan
 *  1) 提供了百度坐标（BD09）、国测局坐标（火星坐标，GCJ02）、和WGS84坐标系之间的转换
 *  2) 高德地图计算文档 : https://lbs.amap.com/api/android-sdk/guide/computing-equipment/coordinate-transformation
 *  3) 克拉索夫斯基椭球长半轴（GCJ02 火星坐标互转 / 只能给坐标纠偏用） -> 6378245.0
 *     WGS84 椭球长半轴（GPS 原始坐标、国际定位 / 全球定位专用） -> 6378137.0
 *     地球平均球体半径（圆形范围、半径测距、圈选区域 / 只做球面粗略计算，画圈测距用） -> 6370693.5
 */
object CoordinateUtil {
    // 地球物理参数
    private const val EARTH_RADIUS_METERS = 6370693.5 // 地球平均半径 (画圆、测距、范围框、球面计算，单位：米)
    private const val GCJ_AXIS = 6378245.0 // GCJ02 算法专用椭球长半轴 （所有坐标转换 GCJ02、BD09、WGS 互转）
    private const val ECCENTRICITY = 0.006693421622965943 // 地球椭球体第一偏心率平方
    // 坐标转换算法常量
    private const val X_PI = Math.PI * 3000.0 / 180.0 // 百度坐标系专用常量（π*3000/180）
    // 角度转换常量
    private const val PI_OVER_180 = Math.PI / 180.0 // 角度转弧度常量（π/180）
    private const val PI_TIMES_180 = 180.0 / Math.PI // 弧度转角度常量（180/π）

    /**
     * 计算经纬度边界框
     * @param lon 圆心经度
     * @param lat 圆心纬度
     * @param radius 半径（米）
     * @return [最小纬度, 最大纬度, 最小经度, 最大经度]  标准顺序
     */
    @JvmStatic
    fun calculateBoundingBox(lon: Double, lat: Double, radius: Int): DoubleArray {
        // 角度转弧度
        val latRad = lat * PI_OVER_180
        val sinLat = sin(latRad)
        val cosLat = cos(latRad)
        val radDist = radius / EARTH_RADIUS_METERS
        val cosDist = cos(radDist)
        // 纬度范围
        val minLat = lat - radDist * PI_TIMES_180
        val maxLat = lat + radDist * PI_TIMES_180
        // 经度范围
        val lonDiff = acos((cosDist - sinLat * sinLat) / (cosLat * cosLat)) * PI_TIMES_180
        val minLon = lon - lonDiff
        val maxLon = lon + lonDiff
        // 标准顺序：纬度在前，经度在后
        return doubleArrayOf(minLat, maxLat, minLon, maxLon)
    }

    /**
     * 计算多点几何中心
     */
    @JvmStatic
    fun calculateCenterPoint(latLngList: MutableList<LatLng>): LatLng {
        if (latLngList.isEmpty()) return LatLng(0.0, 0.0)
        val total = latLngList.size
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0
        for (point in latLngList) {
            val lngRad = point.longitude * PI_OVER_180
            val latRad = point.latitude * PI_OVER_180
            sumX += cos(latRad) * cos(lngRad)
            sumY += cos(latRad) * sin(lngRad)
            sumZ += sin(latRad)
        }
        val avgX = sumX / total
        val avgY = sumY / total
        val avgZ = sumZ / total
        val centerLng = atan2(avgY, avgX) * PI_TIMES_180
        val hyp = sqrt(avgX * avgX + avgY * avgY)
        val centerLat = atan2(avgZ, hyp) * PI_TIMES_180
        return LatLng(centerLat, centerLng)
    }

    /**
     * 设置东南和西北角，构筑一个矩形范围
     */
    @JvmStatic
    fun createBoundingBox(latA: Double, lngA: Double, latB: Double, lngB: Double): LatLngBounds {
        val minLat = minOf(latA, latB)
        val maxLat = maxOf(latA, latB)
        val minLng = minOf(lngA, lngB)
        val maxLng = maxOf(lngA, lngB)
        return LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng))
    }

    /**
     * 在矩形内随机生成经纬度
     * @param minLng 最小经度
     * @param maxLng 最大经度
     * @param minLat 最小纬度
     * @param maxLat 最大纬度
     * @return LatLng
     */
    @JvmStatic
    fun generateRandomPoint(minLng: Double, maxLng: Double, minLat: Double, maxLat: Double): LatLng {
        val lon = BigDecimal(Math.random() * (maxLng - minLng) + minLng).setScale(6, RoundingMode.HALF_UP).toDouble()
        val lat = BigDecimal(Math.random() * (maxLat - minLat) + minLat).setScale(6, RoundingMode.HALF_UP).toDouble()
        return LatLng(lat, lon)
    }

    /**
     * 生成【起点周围、半径范围内】的随机点（从起点到终点的随机路径点的假轨迹）
     * 1) 断网 / 调用api失效时使用，作为保底措施
     * 2) 坐标系会穿墙，飘忽不定
     */
    @JvmStatic
    fun generateRandomPath(startLatLng: LatLng, endLatLng: LatLng): MutableList<LatLng> {
        val pathPoints = ArrayList<LatLng>()
        pathPoints.add(startLatLng)
        val startLat = startLatLng.latitude
        val startLng = startLatLng.longitude
        val random = Random()
        for (i in 0..49) {
            val randomLat = startLat + (10 - Math.random() * 20) / 10.0.pow((random.nextInt(3) + 1).toDouble())
            val randomLng = startLng + (10 - Math.random() * 20) / 10.0.pow((random.nextInt(3) + 1).toDouble())
            val randomPoint = LatLng(randomLat, randomLng)
            if (!pathPoints.contains(randomPoint)) {
                pathPoints.add(randomPoint)
            }
        }
        // 如果随机生成的数组个数为0，则再随机添加一个距离中心点更近的坐标
        if (pathPoints.size == 1) {
            pathPoints.add(LatLng(startLat + (if (Math.random() > 0.5) 1 else -1) / 10.0.pow(3.0), startLng + (if (Math.random() > 0.5) 1 else -1) / 10.0.pow(3.0)))
        }
        if (!pathPoints.contains(endLatLng)) {
            pathPoints.add(endLatLng)
        }
        return pathPoints
    }

    /**
     * 支持GPS/Mapbar/Baidu等多种类型坐标在高德地图上使用
     * val converter = CoordinateConverter(context)
     */
    @JvmStatic
    fun convert(converter: CoordinateConverter, sourceLatLng: LatLng, type: CoordinateConverter.CoordType = CoordinateConverter.CoordType.GPS): LatLng {
        // CoordType.GPS 待转换坐标类型
        converter.from(type)
        // sourceLatLng待转换坐标点 LatLng类型
        converter.coord(sourceLatLng)
        // 执行转换操作
        return converter.convert()
    }

    /**
     * 百度坐标 [BD09] 转火星坐标 [GCJ02]
     * @param lng 百度经度
     * @param lat 百度纬度
     * @return GCJ02 坐标：[经度，纬度]
     */
    @JvmStatic
    fun bd09ToGcj02(lng: Double, lat: Double): DoubleArray {
        val x = lng - 0.0065
        val y = lat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * X_PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * X_PI)
        val gcjLng = z * cos(theta)
        val gcjLat = z * sin(theta)
        return doubleArrayOf(gcjLng, gcjLat)
    }

    /**
     * 百度坐标 [BD09] 转 [WGS84] 坐标
     * @param lng 经度
     * @param lat 纬度
     * @return WGS84 坐标：[经度，纬度]
     */
    @JvmStatic
    fun bd09ToWgs84(lng: Double, lat: Double): DoubleArray {
        val gcj = bd09ToGcj02(lng, lat)
        return gcj02ToWgs84(gcj[0], gcj[1])
    }

    /**
     * 火星坐标 [GCJ02] 转 [WGS84] 坐标
     * @param lng 经度
     * @param lat 纬度
     * @return WGS84坐标：[经度，纬度]
     */
    @JvmStatic
    fun gcj02ToWgs84(lng: Double, lat: Double): DoubleArray {
        if (isOutOfChina(lng, lat)) return doubleArrayOf(lng, lat)
        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)
        val radLat = lat * PI_OVER_180
        var magic = sin(radLat)
        magic = 1 - ECCENTRICITY * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = dLat * 180.0 / (GCJ_AXIS * (1 - ECCENTRICITY) / (magic * sqrtMagic) * Math.PI)
        dLng = dLng * 180.0 / (GCJ_AXIS / sqrtMagic * cos(radLat) * Math.PI)
        val mgLat = lat + dLat
        val mgLng = lng + dLng
        return doubleArrayOf(lng * 2 - mgLng, lat * 2 - mgLat)
    }

    private fun transformLat(lng: Double, lat: Double): Double {
        var ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * sqrt(abs(lng))
        ret += (20.0 * sin(6.0 * lng * Math.PI) + 20.0 * sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
        ret += (20.0 * sin(lat * Math.PI) + 40.0 * sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0
        ret += (160.0 * sin(lat / 12.0 * Math.PI) + 320 * sin(lat * Math.PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLng(lng: Double, lat: Double): Double {
        var ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * sqrt(abs(lng))
        ret += (20.0 * sin(6.0 * lng * Math.PI) + 20.0 * sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
        ret += (20.0 * sin(lng * Math.PI) + 40.0 * sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0
        ret += (150.0 * sin(lng / 12.0 * Math.PI) + 300.0 * sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * 火星坐标 [GCJ02] 转百度坐标 [BD09]
     * @param lng GCJ02 经度
     * @param lat GCJ02 纬度
     * @return 百度坐标：[经度，纬度]
     */
    @JvmStatic
    fun gcj02ToBd09(lng: Double, lat: Double): DoubleArray {
        val z = sqrt(lng * lng + lat * lat) + 0.00002 * sin(lat * X_PI)
        val theta = atan2(lat, lng) + 0.000003 * cos(lng * X_PI)
        val bdLng = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return doubleArrayOf(bdLng, bdLat)
    }

    /**
     * [WGS84] 坐标转火星坐标 [GCJ02]
     */
    @JvmStatic
    fun wgs84ToGcj02(lng: Double, lat: Double): DoubleArray {
        if (isOutOfChina(lng, lat)) return doubleArrayOf(lng, lat)
        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)
        val radLat = lat * PI_OVER_180
        var magic = sin(radLat)
        magic = 1 - ECCENTRICITY * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = dLat * 180.0 / (GCJ_AXIS * (1 - ECCENTRICITY) / (magic * sqrtMagic) * Math.PI)
        dLng = dLng * 180.0 / (GCJ_AXIS / sqrtMagic * cos(radLat) * Math.PI)

        val gcjLat = lat + dLat
        val gcjLng = lng + dLng
        return doubleArrayOf(gcjLng, gcjLat)
    }

    /**
     * [WGS84] 坐标转百度坐标 [BD09]
     */
    @JvmStatic
    fun wgs84ToBd09(lng: Double, lat: Double): DoubleArray {
        val gcj = wgs84ToGcj02(lng, lat)
        return gcj02ToBd09(gcj[0], gcj[1])
    }

    /**
     * 判断坐标是否不在国内
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标是否在国内
     */
    @JvmStatic
    fun isOutOfChina(lng: Double, lat: Double): Boolean {
        return lng !in 72.004..137.8347 || lat < 0.8293 || lat > 55.8271
    }

}