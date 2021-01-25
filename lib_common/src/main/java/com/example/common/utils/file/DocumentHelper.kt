package com.example.common.utils.file

import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

/**
 *  Created by wangyanbin
 *  文件管理工具（切片，合并）
 */
object DocumentHelper {

    //分片文件类
    class FileTmpInfo(
        var path: String? = null,
        var fileSize: Long = 0
    )

    /**
     *  文件分割
     *  targetFile 分割的文件
     *  cutSize    分割文件的大小
     *  int        文件切割的个数
     */
    @JvmStatic
    fun getSplitFile(targetFile: File, cutSize: Long): MutableList<String> {
        val splitList = ArrayList<String>()
        //计算切割文件大小
        val count = if (targetFile.length() % cutSize == 0L) (targetFile.length() / cutSize).toInt() else (targetFile.length() / cutSize + 1).toInt()
        var raf: RandomAccessFile? = null
        try {
            //获取目标文件 预分配文件所占的空间 在磁盘中创建一个指定大小的文件   r 是只读
            raf = RandomAccessFile(targetFile, "r")
            val length = raf.length() //文件的总长度
            val maxSize = length / count //文件切片后的长度
            var offSet = 0L //初始化偏移量
            //最后一片单独处理
            for (i in 0 until count - 1) {
                val begin = offSet
                val end = (i + 1) * maxSize
                val tmpInfo: FileTmpInfo = getWrite(targetFile.absolutePath, i, begin, end)
                offSet = tmpInfo.fileSize
                splitList.add(tmpInfo.path!!)
            }
            if (length - offSet > 0) {
                val tmpInfo: FileTmpInfo = getWrite(
                    targetFile.absolutePath,
                    count - 1,
                    offSet,
                    length
                )
                splitList.add(tmpInfo.path!!)
            }
        } catch (e: IOException) {
        } finally {
            try {
                raf?.close()
            } catch (e: IOException) {
            }
            //保险措施-避免路径为空的情况
            for (i in splitList.indices.reversed()) {
                if (TextUtils.isEmpty(splitList[i])) {
                    splitList.removeAt(i)
                }
            }
        }
        return splitList
    }

    @JvmStatic
    fun getWrite(file: String, index: Int, begin: Long, end: Long) :FileTmpInfo{
        var endPointer = 0L
        val a: String = file.split(suffixName(File(file)).toRegex()).toTypedArray()[0]
        val tmpPath = ""
        try {
            //申明文件切割后的文件磁盘
            val randomAccessFile = RandomAccessFile(File(file), "r")
            //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            //读取切片文件
            val mFile = File(a + "_" + index + ".tmp")
            //存在文件则先删除，保证切片的文件是单独生成的
            if (isFileExist(mFile)) {
                FileUtil.deleteDirWithFile(mFile)
            }
            //不存在
            val out = RandomAccessFile(mFile, "rw")
            //申明具体每一文件的字节数组
            val b = ByteArray(1024)
            var n = 0
            //从指定位置读取文件字节流
            randomAccessFile.seek(begin)
            //判断文件流读取的边界
            while (randomAccessFile.read(b).also { n = it } != -1 && randomAccessFile.filePointer <= end) {
                //从指定每一份文件的范围，写入不同的文件
                out.write(b, 0, n)
            }
            //定义当前读取文件的指针
            endPointer = randomAccessFile.filePointer
            //关闭输入流
            randomAccessFile.close()
            //关闭输出流
            out.close()
        }catch (e: Exception){
        }
        return FileTmpInfo(tmpPath, endPointer - 1024)
    }

    /**
     * 获取文件后缀名 例如：.mp4 /.jpg /.apk
     * file 指定文件
     */
    @JvmStatic
    fun suffixName(file: File) : String{
        val fileName = file.name
        return fileName.substring(fileName.lastIndexOf("."), fileName.length)
    }

    /**
     * 判断文件是否存在
     */
    @JvmStatic
    fun isFileExist(file: File): Boolean {
        return file.exists()
    }

    /**
     * 文件合并
     * fileName   指定合并文件
     * targetFile 分割前的文件
     * cutSize    分割文件的大小
     */
    @JvmStatic
    fun getMergeFile(fileName: String, targetFile: File, cutSize: Long){
        val tempCount = if (targetFile.length() % cutSize == 0L) (targetFile.length() / cutSize).toInt() else (targetFile.length() / cutSize + 1).toInt()
        //文件名
        val a = targetFile.absolutePath.split(suffixName(targetFile).toRegex()).toTypedArray()[0]
        var raf: RandomAccessFile? = null
        try {
            //申明随机读取文件RandomAccessFile
            raf = RandomAccessFile(File(fileName + suffixName(targetFile)), "rw")
            //开始合并文件，对应切片的二进制文件
            for (i in 0 until tempCount) {
                //读取切片文件
                val mFile = File(a + "_" + i + ".tmp")
                val reader = RandomAccessFile(mFile, "r")
                val b = ByteArray(1024)
                var n = 0
                //先读后写
                while (reader.read(b).also { n = it } != -1) { //读
                    raf.write(b, 0, n) //写
                }
                //合并后删除文件
                mFile.delete()
            }
        } catch (e: Exception) {
        } finally {
            try {
                raf!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun getSignatureFile(file: File?): String? {
        val digest: MessageDigest
        val fileInputStream: FileInputStream
        val buffer = ByteArray(1024)
        var len: Int
        try {
            digest = MessageDigest.getInstance("SHA-256")
            fileInputStream = FileInputStream(file)
            while (fileInputStream.read(buffer, 0, 1024).also { len = it } != -1) {
                digest.update(buffer, 0, len)
            }
            fileInputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
        val bigInt = BigInteger(1, digest.digest())
        var hash = bigInt.toString(16)
        //长度不足64位，首位补0
        if (hash.length < 64) {
            val difference = 64 - hash.length
            for (i in 0 until difference) {
                hash = "0$hash"
            }
        }
        return hash
    }

}