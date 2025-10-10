package com.example.amap.utils

import android.content.Context
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.example.framework.utils.function.value.toSafeDouble
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Random
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *  Created by wangyanbin
 *  提供了百度坐标（BD09）、国测局坐标（火星坐标，GCJ02）、和WGS84坐标系之间的转换
 */
object CoordinateUtil {
    // 地球物理参数
    private const val EARTH_RADIUS_METERS = 6370693.5  // 地球平均半径，单位：米
    private const val ECCENTRICITY = 0.00669342162296594323  // 地球椭球体第一偏心率平方
    // 坐标转换算法常量
    private const val X_PI = Math.PI * 3000.0 / 180.0  // 百度坐标系专用常量（π*3000/180）
    // 角度转换常量
    private const val PI_OVER_180 = Math.PI / 180.0  // 角度转弧度常量（π/180）
    private const val PI_TIMES_180 = 180.0 / Math.PI  // 弧度转角度常量（180/π）

    /**
     * 计算以圆心为中心的经纬度范围（边界框）
     * @param lon 圆心经度
     * @param lat 圆心纬度
     * @param radius   半径（米）
     * @return double[4] 南侧经度，北侧经度，西侧纬度，东侧纬度
     */
    @JvmStatic
    fun calculateBoundingBox(lon: Double, lat: Double, radius: Int): DoubleArray {
        val range = DoubleArray(4)
        // 角度转弧度
        val radiansLat = lat * PI_OVER_180
        val sinLat = sin(radiansLat)
        val cosLat = cos(radiansLat)
        val cosDistance = cos(radius / EARTH_RADIUS_METERS)
        // 计算经度差值
        val lonDiff = acos((cosDistance - sinLat * sinLat) / (cosLat * cosLat)) * PI_TIMES_180
        // 保存经度范围
        range[0] = lon - lonDiff  // 最小经度
        range[1] = lon + lonDiff  // 最大经度
        // 计算纬度范围
        val m = -2 * cosDistance * sinLat
        val n = cosDistance * cosDistance - cosLat * cosLat
        val o1 = (-m - sqrt(m * m - 4 * n)) / 2
        val o2 = (-m + sqrt(m * m - 4 * n)) / 2
        range[2] = asin(o1) * PI_TIMES_180  // 最小纬度
        range[3] = asin(o2) * PI_TIMES_180  // 最大纬度
        return range
    }

    /**
     * 计算一组坐标点的几何中心点
     */
    @JvmStatic
    fun calculateCenterPoint(latLngList: MutableList<LatLng>): LatLng {
        val total = latLngList.size
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0
        for (point in latLngList) {
            val lonRad = point.longitude * PI_OVER_180
            val latRad = point.latitude * PI_OVER_180
            sumX += cos(latRad) * cos(lonRad)
            sumY += cos(latRad) * sin(lonRad)
            sumZ += sin(latRad)
        }
        val avgX = sumX / total
        val avgY = sumY / total
        val avgZ = sumZ / total
        val centerLon = atan2(avgY, avgX) * PI_TIMES_180
        val hypotenuse = sqrt(avgX * avgX + avgY * avgY)
        val centerLat = atan2(avgZ, hypotenuse) * PI_TIMES_180
        return LatLng(centerLat, centerLon)
    }

    /**
     * 设置东南和西北角，构筑一个矩形范围
     */
    @JvmStatic
    fun createBoundingBox(latA: Double, lngA: Double, latB: Double, lngB: Double): LatLngBounds {
        val maxLat = maxOf(latA, latB)
        val minLat = minOf(latA, latB)
        val maxLng = maxOf(lngA, lngB)
        val minLng = minOf(lngA, lngB)
        return LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng))
    }

    /**
     * 在矩形内随机生成经纬度
     * @param minLon：最小经度  maxLon： 最大经度   minLat：最小纬度   maxLat：最大纬度
     * @return LatLng
     */
    @JvmStatic
    fun generateRandomPoint(minLon: Double, maxLon: Double, minLat: Double, maxLat: Double): LatLng {
        val lon = BigDecimal(Math.random() * (maxLon - minLon) + minLon).setScale(6, RoundingMode.HALF_UP).toSafeDouble() //小数后6位
        val lat = BigDecimal(Math.random() * (maxLat - minLat) + minLat).setScale(6, RoundingMode.HALF_UP).toSafeDouble()
        return LatLng(lat, lon)
    }

    /**
     * 生成以中心点附近指定radius内的坐标数组（从起点到终点的随机路径点）
     */
    @JvmStatic
    fun generateRandomPath(startLatLng: LatLng, endLatLng: LatLng): MutableList<LatLng> {
        val pathPoints = ArrayList<LatLng>()
        pathPoints.add(startLatLng)
        val startLat = startLatLng.latitude
        val startLng = startLatLng.longitude
        for (i in 0..49) {
            val randomLat = startLat + (10 - Math.random() * 20) / 10.0.pow((Random().nextInt(3) + 1).toDouble())
            val randomLng = startLng + (10 - Math.random() * 20) / 10.0.pow((Random().nextInt(3) + 1).toDouble())
            val randomPoint = LatLng(randomLat, randomLng)
            if (!pathPoints.contains(randomPoint)) {
                pathPoints.add(randomPoint)
            }
        }
        //如果随机生成的数组个数为0，则再随机添加一个距离中心点更近的坐标
        if (pathPoints.size == 1) {
            pathPoints.add(LatLng(startLat + (if (Math.random() > 0.5) 1 else -1) / 10.0.pow(3.0), startLng + (if (Math.random() > 0.5) 1 else -1) / 10.0.pow(3.0)))
        }
        if (!pathPoints.contains(endLatLng)) {
            pathPoints.add(endLatLng)
        }
        return pathPoints
    }

    /**
     * 百度坐标(BD09)转火星坐标(GCJ02)
     *
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
     * 百度坐标(BD09)转WGS84坐标
     *
     * @param lng 经度
     * @param lat 纬度
     * @return WGS84 坐标：[经度，纬度]
     */
    @JvmStatic
    fun bd09ToWgs84(lng: Double, lat: Double): DoubleArray {
        val lngLat = bd09ToGcj02(lng, lat)
        return gcj02ToWgs84(lngLat[0], lngLat[1])
    }

    /**
     * 火星坐标(GCJ02)转百度坐标(BD09)
     *
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
     * 火星坐标(GCJ02)转WGS84坐标
     *
     * @param lng 经度
     * @param lat 纬度
     * @return WGS84坐标：[经度，纬度]
     */
    @JvmStatic
    fun gcj02ToWgs84(lng: Double, lat: Double): DoubleArray {
        return if (isOutOfChina(lng, lat)) {
            doubleArrayOf(lng, lat)
        } else {
            var dLat = transformLat(lng - 105.0, lat - 35.0)
            var dLng = transformLng(lng - 105.0, lat - 35.0)
            val radLat = lat / 180.0 * Math.PI
            var magic = sin(radLat)
            magic = 1 - ECCENTRICITY * magic * magic
            val sqrtMagic = sqrt(magic)
            dLat = dLat * 180.0 / (EARTH_RADIUS_METERS * (1 - ECCENTRICITY) / (magic * sqrtMagic) * Math.PI)
            dLng = dLng * 180.0 / (EARTH_RADIUS_METERS / sqrtMagic * cos(radLat) * Math.PI)
            val mgLat = lat + dLat
            val mgLng = lng + dLng
            doubleArrayOf(lng * 2 - mgLng, lat * 2 - mgLat)
        }
    }

    /**
     * 判断坐标是否不在国内
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标是否在国内
     */
    private fun isOutOfChina(lng: Double, lat: Double): Boolean {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
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
     * 高德自带转换
     * https://lbs.amap.com/api/android-sdk/guide/computing-equipment/coordinate-transformation
     */
    @JvmStatic
    fun convert(context: Context, sourceLatLng: LatLng, type: CoordinateConverter.CoordType = CoordinateConverter.CoordType.GPS): LatLng {
        val converter = CoordinateConverter(context)
        // CoordType.GPS 待转换坐标类型
        converter.from(type)
        // sourceLatLng待转换坐标点 LatLng类型
        converter.coord(sourceLatLng)
        // 执行转换操作
        return converter.convert()
    }

}