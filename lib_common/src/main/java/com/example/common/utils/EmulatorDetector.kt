package com.example.common.utils

import android.os.Build
import java.io.File

/**
 * 设备检测
 */
object EmulatorDetector {

    /**
     * 检测是否为安卓模拟器(PC模拟器)
     * 策略：正常真机绝对不会触发, 恶意改机用户拦截无影响
     */
    fun isEmulator(): Boolean {
        return checkQemuFiles() || checkBuildHardware()
    }

    /**
     * 检测QEMU虚拟设备文件
     * /dev/qemu_pipe、/dev/socket/qemud 是 QEMU 虚拟化内核才会创建的虚拟设备节点
     */
    private fun checkQemuFiles(): Boolean {
        val emulatorFiles = arrayOf("/dev/qemu_pipe", "/dev/socket/qemud")
        return emulatorFiles.any { File(it).exists() }
    }

    /**
     * 检测模拟器专属硬件标识
     * goldfish/ranchu: 官方模拟器硬件名
     * qemu: 通用模拟器标识
     */
    private fun checkBuildHardware(): Boolean {
        val hardware = Build.HARDWARE.lowercase()
        return hardware.contains("qemu") || hardware.contains("goldfish") || hardware.contains("ranchu")
    }

}
//object EmulatorDetector {
//    private val context by lazy { BaseApplication.instance.applicationContext }
//
//    /**
//     * 多维度校验是否为虚拟机
//     * 满足 2 项及以上则判定为模拟器
//     */
//    fun isEmulator(): Boolean {
//        val checks = mutableListOf<Boolean>()
//        // 系统属性校验
//        checks.add(checkSystemProperties())
//        // 硬件信息校验
//        checks.add(checkHardwareInfo())
//        // 传感器校验
//        checks.add(checkSensors())
//        // 电池信息校验
//        checks.add(checkBattery())
//        // Android ID 辅助校验（仅作为补充，不单独生效）
//        checks.add(checkAndroidId())
//        // 统计满足的校验项数量，≥2 则判定为虚拟机
//        return checks.count { it } >= 2
//    }
//
//    /**
//     * 系统属性校验
//     * 虚拟机系统会携带 qemu 标识
//     */
//    private fun checkSystemProperties(): Boolean {
//        return try {
//            // ro.kernel.qemu（虚拟机必带，值为1）
//            val qemuKernel = getSystemProperty("ro.kernel.qemu") == "1"
//            // ro.boot.qemu、ro.hardware.qemu
//            val qemuBoot = getSystemProperty("ro.boot.qemu") == "1"
//            val qemuHardware = getSystemProperty("ro.hardware")?.contains("qemu", ignoreCase = true) ?: false
//            qemuKernel || qemuBoot || qemuHardware
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    /**
//     * 硬件信息校验（CPU/主板型号）
//     */
//    private fun checkHardwareInfo(): Boolean {
//        val cpuModel = getSystemProperty("ro.product.cpu.abi") ?: ""
//        val boardModel = getSystemProperty("ro.product.board") ?: ""
//        val hardwareModel = getSystemProperty("ro.hardware") ?: ""
//        // 虚拟机常见 CPU/主板标识
//        val emulatorCpuKeywords = arrayOf("x86", "x86_64", "intel", "atom")
//        val emulatorBoardKeywords = arrayOf("qemu", "emulator", "virt")
//        return emulatorCpuKeywords.any { cpuModel.contains(it, ignoreCase = true) }
//                || emulatorBoardKeywords.any { boardModel.contains(it, ignoreCase = true) }
//                || emulatorBoardKeywords.any { hardwareModel.contains(it, ignoreCase = true) }
//    }
//
//    /**
//     * 传感器校验（虚拟机传感器数量少）
//     */
//    private fun checkSensors(): Boolean {
//        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return false
//        // 真实手机至少有 3 个以上传感器（加速度、陀螺仪、光线、距离等）
//        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
//        return sensors.size < 3
//    }
//
//    /**
//     * 电池信息校验
//     */
//    private fun checkBattery(): Boolean {
//        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return false
//        // 虚拟机电池容量异常（固定值/超大值）
//        val batteryCapacity = try {
//            // Android 10+ 直接获取电池容量
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
//            } else {
//                // 低版本通过系统文件获取（需读取权限，可选）
//                getSystemProperty("ro.sys.battery.capacity")?.toIntOrNull() ?: 0
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            0
//        }
//        // 虚拟机常见电池容量：0、10000、20000 等异常值
//        return batteryCapacity == 0 || batteryCapacity >= 10000
//    }
//
//    /**
//     * Android ID 辅助校验
//     */
//    @SuppressLint("HardwareIds")
//    private fun checkAndroidId(): Boolean {
//        return try {
//            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//            // 不仅匹配旧固定值，还匹配常见虚拟机随机值格式
//            androidId == "9774d56d682e549c" || androidId.isNullOrEmpty() || androidId.length != 16
//        } catch (e: Exception) {
//            e.printStackTrace()
//            true
//        }
//    }
//
//    /**
//     * 获取系统属性（通过反射，避免直接调用 SystemProperties 被限制）
//     */
//    @SuppressLint("PrivateApi")
//    private fun getSystemProperty(key: String): String? {
//        return try {
//            val clazz = Class.forName("android.os.SystemProperties")
//            val method = clazz.getDeclaredMethod("get", String::class.java)
//            method.isAccessible = true
//            method.invoke(null, key) as? String
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//}