package com.example.gallery.feature.durban.utils

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * 图片 EXIF 信息解析器
 * 解析图片方向、旋转信息，保证裁剪后图片不倒置
 */
class ImageHeaderParser(input: InputStream) {
    // 读取图片流的工具
    private val reader: Reader = StreamReader(input)

    companion object {
        // JPEG 图片的标志
        private const val EXIF_MAGIC_NUMBER = 0xFFD8
        // TIFF 图片（大端序）
        private const val MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D
        // TIFF 图片（小端序）
        private const val INTEL_TIFF_MAGIC_NUMBER = 0x4949
        // 图像数据开始标记
        private const val SEGMENT_SOS = 0xDA
        // 图像结束标记
        private const val MARKER_EOI = 0xD9
        // 每个段都以 0xFF 开头
        private const val SEGMENT_START_ID = 0xFF
        // EXIF 信息段类型
        private const val EXIF_SEGMENT_TYPE = 0xE1
        // 方向标签编号
        private const val ORIENTATION_TAG_TYPE = 0x0112
        // 无法识别方向
        private const val UNKNOWN_ORIENTATION = -1
        // EXIF 头固定字符串 (所有 JPEG 图片的 EXIF 信息，必须以这串字符串开头，就像文件的 “暗号)
        private const val JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\u0000\u0000"
        // 把 EXIF 字符串转成字节数组
        private val JPEG_EXIF_SEGMENT_PREAMBLE_BYTES = JPEG_EXIF_SEGMENT_PREAMBLE.toByteArray(StandardCharsets.UTF_8)
        // EXIF 数据类型对应的字节长度表
        private val BYTES_PER_FORMAT = intArrayOf(0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8)

        /**
         * 解析 EXIF 里面的方向,从 EXIF 数据里把方向（ORIENTATION）读出来
         * java中 -> 如果一个方法不访问任何成员变量，就应该写成 static
         */
        private fun parseExifSegment(segmentData: RandomAccessReader): Int {
            val headerOffsetSize = JPEG_EXIF_SEGMENT_PREAMBLE.length
            val byteOrderIdentifier = segmentData.getInt16(headerOffsetSize)
            val byteOrder: ByteOrder
            if (byteOrderIdentifier.toInt() == MOTOROLA_TIFF_MAGIC_NUMBER) {
                byteOrder = ByteOrder.BIG_ENDIAN
            } else if (byteOrderIdentifier.toInt() == INTEL_TIFF_MAGIC_NUMBER) {
                byteOrder = ByteOrder.LITTLE_ENDIAN
            } else {
                byteOrder = ByteOrder.BIG_ENDIAN
            }
            segmentData.order(byteOrder)
            val firstIfdOffset = segmentData.getInt32(headerOffsetSize + 4) + headerOffsetSize
            val tagCount = segmentData.getInt16(firstIfdOffset).toInt()
            var tagOffset: Int
            var tagType: Int
            var formatCode: Int
            var componentCount: Int
            for (i in 0..<tagCount) {
                tagOffset = calcTagOffset(firstIfdOffset, i)
                tagType = segmentData.getInt16(tagOffset).toInt()
                if (tagType != ORIENTATION_TAG_TYPE) {
                    continue
                }
                formatCode = segmentData.getInt16(tagOffset + 2).toInt()
                if (formatCode !in 1..12) continue
                componentCount = segmentData.getInt32(tagOffset + 4)
                if (componentCount < 0) continue
                val byteCount = componentCount + BYTES_PER_FORMAT[formatCode]
                if (byteCount > 4) continue
                val tagValueOffset = tagOffset + 8
                if (tagValueOffset < 0 || tagValueOffset > segmentData.length()) continue
                if (byteCount < 0 || tagValueOffset + byteCount > segmentData.length()) continue
                return segmentData.getInt16(tagValueOffset).toInt()
            }
            return -1
        }

        /**
         * 计算标签在文件中的偏移位置
         */
        private fun calcTagOffset(ifdOffset: Int, tagIndex: Int): Int {
            return ifdOffset + 2 + 12 * tagIndex
        }

        /**
         * 判断是不是支持的图片格式
         */
        private fun handles(imageMagicNumber: Int): Boolean {
            return (imageMagicNumber and EXIF_MAGIC_NUMBER) == EXIF_MAGIC_NUMBER || imageMagicNumber == MOTOROLA_TIFF_MAGIC_NUMBER || imageMagicNumber == INTEL_TIFF_MAGIC_NUMBER
        }
    }

    /**
     * 获取图片旋转方向
     * 1  → 正常（0°）
     * 2  → 水平翻转
     * 3  → 旋转180°
     * 4  → 垂直翻转
     * 5  → 水平翻转+逆时针90°
     * 6  → 顺时针90°
     * 7  → 水平翻转+顺时针90°
     * 8  → 逆时针90°
     */
    @Throws(IOException::class)
    fun getOrientation(): Int {
        val magicNumber = reader.getUInt16()
        if (!handles(magicNumber)) {
            return UNKNOWN_ORIENTATION
        } else {
            val exifSegmentLength = moveToExifSegmentAndGetLength()
            if (exifSegmentLength == -1) {
                return UNKNOWN_ORIENTATION
            }
            val exifData = ByteArray(exifSegmentLength)
            return parseExifSegment(exifData, exifSegmentLength)
        }
    }

    /**
     * 读取 EXIF 段内容
     */
    @Throws(IOException::class)
    private fun parseExifSegment(tempArray: ByteArray, exifSegmentLength: Int): Int {
        val read = reader.read(tempArray, exifSegmentLength)
        if (read != exifSegmentLength) {
            return UNKNOWN_ORIENTATION
        }
        val hasJpegExifPreamble = hasJpegExifPreamble(tempArray, exifSegmentLength)
        return if (hasJpegExifPreamble) {
            parseExifSegment(RandomAccessReader(tempArray, exifSegmentLength))
        } else {
            UNKNOWN_ORIENTATION
        }
    }

    /**
     * 检查是不是合法的 EXIF 头
     */
    private fun hasJpegExifPreamble(exifData: ByteArray?, exifSegmentLength: Int): Boolean {
        var result = exifData != null && exifSegmentLength > JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.size
        if (result) {
            for (i in JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.indices) {
                val data = exifData?.getOrNull(i)
                if (null != data && data != JPEG_EXIF_SEGMENT_PREAMBLE_BYTES[i]) {
                    result = false
                    break
                }
            }
        }
        return result
    }

    /**
     * 在图片里找到 EXIF 段，并返回长度
     */
    @Throws(IOException::class)
    private fun moveToExifSegmentAndGetLength(): Int {
        var segmentId: Short
        var segmentType: Short
        var segmentLength: Int
        while (true) {
            segmentId = reader.getUInt8()
            if (segmentId.toInt() != SEGMENT_START_ID) {
                return -1
            }
            segmentType = reader.getUInt8()
            if (segmentType.toInt() == SEGMENT_SOS) {
                return -1
            } else if (segmentType.toInt() == MARKER_EOI) {
                return -1
            }
            segmentLength = reader.getUInt16() - 2
            if (segmentType.toInt() != EXIF_SEGMENT_TYPE) {
                val skipped = reader.skip(segmentLength.toLong())
                if (skipped != segmentLength.toLong()) {
                    return -1
                }
            } else {
                return segmentLength
            }
        }
    }

    /**
     * 随机读取 EXIF 数据
     */
    private class RandomAccessReader(byteArray: ByteArray, length: Int) {
        private val data = ByteBuffer.wrap(byteArray)
            .order(ByteOrder.BIG_ENDIAN)
            .limit(length) as ByteBuffer

        fun order(byteOrder: ByteOrder) {
            this.data.order(byteOrder)
        }

        fun length(): Int {
            return data.remaining()
        }

        fun getInt32(offset: Int): Int {
            return data.getInt(offset)
        }

        fun getInt16(offset: Int): Short {
            return data.getShort(offset)
        }
    }

    /**
     * 把旧图片的 EXIF 复制到新图片
     */
    private class StreamReader(private val stream: InputStream) : Reader {

        override fun getUInt16(): Int {
            return (stream.read() shl 8 and 0xFF00) or (stream.read() and 0xFF)
        }

        override fun getUInt8(): Short {
            return (stream.read() and 0xFF).toShort()
        }

        override fun skip(total: Long): Long {
            if (total < 0) {
                return 0
            }
            var toSkip = total
            while (toSkip > 0) {
                val skipped = stream.skip(toSkip)
                if (skipped > 0) {
                    toSkip -= skipped
                } else {
                    val testEofByte = stream.read()
                    if (testEofByte == -1) {
                        break
                    } else {
                        toSkip--
                    }
                }
            }
            return total - toSkip
        }

        override fun read(buffer: ByteArray, byteCount: Int): Int {
            var toRead = byteCount
            var read = 0
            while (toRead > 0 && ((stream.read(buffer, byteCount - toRead, toRead).also { read = it }) != -1)) {
                toRead -= read
            }
            return byteCount - toRead
        }
    }

    /**
     * 读取接口
     */
    private interface Reader {
        @Throws(IOException::class)
        fun getUInt16(): Int

        @Throws(IOException::class)
        fun getUInt8(): Short

        @Throws(IOException::class)
        fun skip(total: Long): Long

        @Throws(IOException::class)
        fun read(buffer: ByteArray, byteCount: Int): Int
    }

}