package com.example.thirdparty.part.bean

/**
 * 开始创建并写入part文件
 * @param filePath  分割文件地址
 * @param filePointer 分割文件大小
 */
class PartBean(
    var filePath: String? = null,
    var filePointer: Long? = null
)