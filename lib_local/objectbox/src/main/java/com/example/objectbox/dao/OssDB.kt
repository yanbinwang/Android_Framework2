package com.example.objectbox.dao

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

/**
 * oss存储证据库，对应建立的证据表数据库
 */
@Entity
data class OssDB(
    @Id var id: Long = 0, //主键标识(默认情况下，id是会被objectbox管理的，也就是自增id)
    @Unique var baoquan: String? = null, //文件唯一识别码，用于发起自动上传->@Unique(表明在数据库当中，这个属性的值为唯一,否则抛出UniqueViolationException)
    var userId: String? = null, //当前用户的id(区别不同身份)
    var sourcePath: String? = null, //文件在手机中的路径
    var objectName: String? = null, //oss文件夹命名（每次都会根据时间戳产生，用于下次断点续传）
    var objectKey: String? = null, //oss上传完成后，服务器需要记录的值
    var state: Int = 0, //0上传中 1上传失败 2上传完成（证据缺失直接校验源文件路径）
    var extras: String? = null //保留字段
)