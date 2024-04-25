package com.example.amap.utils

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
object CoordinateTransUtil {
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323
    private const val PI = 3.1415926535897932384626
    private const val X_PI = 3.14159265358979324 * 3000.0 / 180.0
    private const val DEF_PI = 3.14159265359//PI
    private const val DEF_PI180 = 0.01745329252//PI/180.0
    private const val DEF_R = 6370693.5//radius of earth

    /**
     * 根据圆心、半径算出经纬度范围
     * @param lon 圆心经度
     * @param lat 圆心纬度
     * @param r   半径（米）
     * @return double[4] 南侧经度，北侧经度，西侧纬度，东侧纬度
     */
    @JvmStatic
    fun getRange(lon: Double, lat: Double, r: Int): DoubleArray {
        val range = DoubleArray(4)
        //角度转换为弧度
        val ns = lat * DEF_PI180
        val sinNs = sin(ns)
        val cosNs = cos(ns)
        val cosTmp = cos(r / DEF_R)
        //经度的差值
        val lonDif = acos((cosTmp - sinNs * sinNs) / (cosNs * cosNs)) / DEF_PI180
        //保存经度
        range[0] = lon - lonDif
        range[1] = lon + lonDif
        val m = 0 - 2 * cosTmp * sinNs
        val n = cosTmp * cosTmp - cosNs * cosNs
        val o1 = (0 - m - sqrt(m * m - 4 * n)) / 2
        val o2 = (0 - m + sqrt(m * m - 4 * n)) / 2
        //纬度
        val lat1 = 180 / DEF_PI * asin(o1)
        val lat2 = 180 / DEF_PI * asin(o2)
        //保存
        range[2] = lat1
        range[3] = lat2
        return range
    }

    /**
     * 获取一组经纬度的相对中心点
     */
    @JvmStatic
    fun getCenterPoint(latLngList: MutableList<LatLng>): LatLng {
        val total = latLngList.size
        var calculationX = 0.0
        var calculationY = 0.0
        var calculationZ = 0.0
        for (index in latLngList.indices) {
            var x: Double
            var y: Double
            var z: Double
            val lon = (latLngList[index].longitude) * Math.PI / 180
            val lat = (latLngList[index].latitude) * Math.PI / 180
            x = cos(lat) * cos(lon)
            y = cos(lat) * sin(lon)
            z = sin(lat)
            calculationX += x
            calculationY += y
            calculationZ += z
        }
        calculationX /= total
        calculationY /= total
        calculationZ /= total
        val lon = atan2(calculationY, calculationX)
        val hYp = sqrt(calculationX * calculationX + calculationY * calculationY)
        val lat = atan2(calculationZ, hYp)
        return LatLng(lat * 180 / Math.PI, lon * 180 / Math.PI)
    }

    /**
     * 设置东南和西北角，构筑一个矩形范围
     */
    @JvmStatic
    fun createBounds(latA: Double, lngA: Double, latB: Double, lngB: Double): LatLngBounds {
        val topLat: Double
        val topLng: Double
        val bottomLat: Double
        val bottomLng: Double
        if (latA >= latB) {
            topLat = latA
            bottomLat = latB
        } else {
            topLat = latB
            bottomLat = latA
        }
        if (lngA >= lngB) {
            topLng = lngA
            bottomLng = lngB
        } else {
            topLng = lngB
            bottomLng = lngA
        }
        return LatLngBounds(LatLng(bottomLat, bottomLng), LatLng(topLat, topLng))
    }

    /**
     * 在矩形内随机生成经纬度
     * @param MinLon：最小经度  MaxLon： 最大经度   MinLat：最小纬度   MaxLat：最大纬度
     * @return LatLng
     */
    @JvmStatic
    fun randomLatLng(minLon: Double, maxLon: Double, minLat: Double, maxLat: Double): LatLng {
        var bigDecimal = BigDecimal(Math.random() * (maxLon - minLon) + minLon)
//        val lon = bigDecimal.setScale(6, BigDecimal.ROUND_HALF_UP).toDouble() //小数后6位
        val lon = bigDecimal.setScale(6, RoundingMode.HALF_UP).toSafeDouble() //小数后6位
        bigDecimal = BigDecimal(Math.random() * (maxLat - minLat) + minLat)
//        val lat = bigDecimal.setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
        val lat = bigDecimal.setScale(6, RoundingMode.HALF_UP).toSafeDouble()
        return LatLng(lat, lon)
    }

    /**
     * 生成以中心点附近指定radius内的坐标数组
     */
    @JvmStatic
    fun randomGenerateLatLng(startLatLng: LatLng, endLatLng: LatLng): MutableList<LatLng> {
        val latLng = ArrayList<LatLng>()
        latLng.add(startLatLng)
        val lat = startLatLng.latitude
        val lng = startLatLng.longitude
        for (i in 0..49) {
            val newLat = lat + (10 - Math.random() * 20) / 10.0.pow((Random().nextInt(3) + 1).toDouble())
            val newLng = lng + (10 - Math.random() * 20) / 10.0.pow((Random().nextInt(3) + 1).toDouble())
            val newLatLng = LatLng(newLat, newLng)
            if (!latLng.contains(newLatLng)) {
                latLng.add(newLatLng)
            }
        }
        //如果随机生成的数组个数为0，则再随机添加一个距离中心点更近的坐标
        if (latLng.size == 1) {
            latLng.add(LatLng(lat + (if (Math.random() > 0.5) 1 else -1) / 10.0.pow(3.0), lng + (if (Math.random() > 0.5) 1 else -1) / 10.0.pow(3.0)))
        }
        if (!latLng.contains(endLatLng)) {
            latLng.add(endLatLng)
        }
        return latLng
    }

    /**
     * 百度坐标（BD09）转 GCJ02
     *
     * @param lng 百度经度
     * @param lat 百度纬度
     * @return GCJ02 坐标：[经度，纬度]
     */
    @JvmStatic
    fun transformBD09ToGCJ02(lng: Double, lat: Double): DoubleArray {
        val x = lng - 0.0065
        val y = lat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * X_PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * X_PI)
        val gcjLng = z * cos(theta)
        val gcjLat = z * sin(theta)
        return doubleArrayOf(gcjLng, gcjLat)
    }

    /**
     * GCJ02 转百度坐标
     *
     * @param lng GCJ02 经度
     * @param lat GCJ02 纬度
     * @return 百度坐标：[经度，纬度]
     */
    @JvmStatic
    fun transformGCJ02ToBD09(lng: Double, lat: Double): DoubleArray {
        val z = sqrt(lng * lng + lat * lat) + 0.00002 * sin(lat * X_PI)
        val theta = atan2(lat, lng) + 0.000003 * cos(lng * X_PI)
        val bdLng = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return doubleArrayOf(bdLng, bdLat)
    }

    /**
     * GCJ02 转 WGS84
     *
     * @param lng 经度
     * @param lat 纬度
     * @return WGS84坐标：[经度，纬度]
     */
    @JvmStatic
    fun transformGCJ02ToWGS84(lng: Double, lat: Double): DoubleArray {
        return if (outOfChina(lng, lat)) {
            doubleArrayOf(lng, lat)
        } else {
            var dLat = transformLat(lng - 105.0, lat - 35.0)
            var dLng = transformLng(lng - 105.0, lat - 35.0)
            val radLat = lat / 180.0 * PI
            var magic = sin(radLat)
            magic = 1 - EE * magic * magic
            val sqrtMagic = sqrt(magic)
            dLat = dLat * 180.0 / (A * (1 - EE) / (magic * sqrtMagic) * PI)
            dLng = dLng * 180.0 / (A / sqrtMagic * cos(radLat) * PI)
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
    private fun outOfChina(lng: Double, lat: Double): Boolean {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
    }

    private fun transformLat(lng: Double, lat: Double): Double {
        var ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * sqrt(abs(lng))
        ret += (20.0 * sin(6.0 * lng * PI) + 20.0 * sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(lat * PI) + 40.0 * sin(lat / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(lat / 12.0 * PI) + 320 * sin(lat * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLng(lng: Double, lat: Double): Double {
        var ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * sqrt(abs(lng))
        ret += (20.0 * sin(6.0 * lng * PI) + 20.0 * sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(lng * PI) + 40.0 * sin(lng / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(lng / 12.0 * PI) + 300.0 * sin(lng / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * 百度坐标BD09 转 WGS84
     *
     * @param lng 经度
     * @param lat 纬度
     * @return WGS84 坐标：[经度，纬度]
     */
    @JvmStatic
    fun transformBD09ToWGS84(lng: Double, lat: Double): DoubleArray {
        val lngLat = transformBD09ToGCJ02(lng, lat)
        return transformGCJ02ToWGS84(lngLat[0], lngLat[1])
    }

}