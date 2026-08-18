package com.example.common.utils

import android.os.Build
import java.io.File

/**
 * 设备检测
 */
object EmulatorDetector {

    /**
     * 检测是否为安卓模拟器(PC 模拟器)
     * 策略：正常真机绝对不会触发, 恶意改机用户拦截无影响
     */
    fun isEmulator(): Boolean {
        return checkQemuFiles() || checkBuildHardware() || checkBootloader()
    }

    /**
     * 检测 QEMU 虚拟设备文件
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

    /**
     * 官方模拟器 bootloader 固定为 "unknown" 或含 emulator 字样
     */
    private fun checkBootloader(): Boolean {
        val bootloader = Build.BOOTLOADER.lowercase()
        return bootloader == "unknown" || bootloader.contains("emulator")
    }

}