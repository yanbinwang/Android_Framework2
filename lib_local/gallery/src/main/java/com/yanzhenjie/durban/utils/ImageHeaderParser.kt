package com.yanzhenjie.durban.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * 图片 EXIF 信息解析器
 * 解析图片方向、旋转信息，保证裁剪后图片不倒置
 */
public class ImageHeaderParser {
    private static final String TAG = "ImageHeaderParser";
    // JPEG 图片的标志
    private static final int EXIF_MAGIC_NUMBER = 0xFFD8;
    // TIFF 图片（大端序）
    private static final int MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D;
    // TIFF 图片（小端序）
    private static final int INTEL_TIFF_MAGIC_NUMBER = 0x4949;
    // 图像数据开始标记
    private static final int SEGMENT_SOS = 0xDA;
    // 图像结束标记
    private static final int MARKER_EOI = 0xD9;
    // 每个段都以 0xFF 开头
    private static final int SEGMENT_START_ID = 0xFF;
    // EXIF 信息段类型
    private static final int EXIF_SEGMENT_TYPE = 0xE1;
    // 方向标签编号
    private static final int ORIENTATION_TAG_TYPE = 0x0112;
    // EXIF 头固定字符串
    private static final String JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\0\0";
    private static final int[] BYTES_PER_FORMAT = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};
    private static final byte[] JPEG_EXIF_SEGMENT_PREAMBLE_BYTES = JPEG_EXIF_SEGMENT_PREAMBLE.getBytes(StandardCharsets.UTF_8);
    // 读取图片流的工具
    private final Reader reader;
    // 无法识别方向
    public static final int UNKNOWN_ORIENTATION = -1;

    public ImageHeaderParser(InputStream is) {
        reader = new StreamReader(is);
    }

    /**
     * 获取图片旋转方向（0/90/180/270）
     */
    public int getOrientation() throws IOException {
        final int magicNumber = reader.getUInt16();
        if (!handles(magicNumber)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Parser doesn't handle magic number: " + magicNumber);
            }
            return UNKNOWN_ORIENTATION;
        } else {
            int exifSegmentLength = moveToExifSegmentAndGetLength();
            if (exifSegmentLength == -1) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Failed to parse exif segment length, or exif segment not found");
                }
                return UNKNOWN_ORIENTATION;
            }
            byte[] exifData = new byte[exifSegmentLength];
            return parseExifSegment(exifData, exifSegmentLength);
        }
    }

    /**
     * 读取 EXIF 段内容
     */
    private int parseExifSegment(byte[] tempArray, int exifSegmentLength) throws IOException {
        int read = reader.read(tempArray, exifSegmentLength);
        if (read != exifSegmentLength) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unable to read exif segment data" + ", length: " + exifSegmentLength + ", actually read: " + read);
            }
            return UNKNOWN_ORIENTATION;
        }
        boolean hasJpegExifPreamble = hasJpegExifPreamble(tempArray, exifSegmentLength);
        if (hasJpegExifPreamble) {
            return parseExifSegment(new RandomAccessReader(tempArray, exifSegmentLength));
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Missing jpeg exif preamble");
            }
            return UNKNOWN_ORIENTATION;
        }
    }

    /**
     * 检查是不是合法的 EXIF 头
     */
    private boolean hasJpegExifPreamble(byte[] exifData, int exifSegmentLength) {
        boolean result = exifData != null && exifSegmentLength > JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.length;
        if (result) {
            for (int i = 0; i < JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.length; i++) {
                if (exifData[i] != JPEG_EXIF_SEGMENT_PREAMBLE_BYTES[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 在图片里找到 EXIF 段，并返回长度
     */
    private int moveToExifSegmentAndGetLength() throws IOException {
        short segmentId, segmentType;
        int segmentLength;
        while (true) {
            segmentId = reader.getUInt8();
            if (segmentId != SEGMENT_START_ID) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Unknown segmentId=" + segmentId);
                }
                return -1;
            }
            segmentType = reader.getUInt8();
            if (segmentType == SEGMENT_SOS) {
                return -1;
            } else if (segmentType == MARKER_EOI) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found MARKER_EOI in exif segment");
                }
                return -1;
            }
            segmentLength = reader.getUInt16() - 2;
            if (segmentType != EXIF_SEGMENT_TYPE) {
                long skipped = reader.skip(segmentLength);
                if (skipped != segmentLength) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Unable to skip enough data" + ", type: " + segmentType + ", wanted to skip: " + segmentLength + ", but actually skipped: " + skipped);
                    }
                    return -1;
                }
            } else {
                return segmentLength;
            }
        }
    }

    /**
     * 解析 EXIF 里面的方向,从 EXIF 数据里把方向（ORIENTATION）读出来
     * java中 -> 如果一个方法不访问任何成员变量，就应该写成 static
     */
    private static int parseExifSegment(RandomAccessReader segmentData) {
        final int headerOffsetSize = JPEG_EXIF_SEGMENT_PREAMBLE.length();
        short byteOrderIdentifier = segmentData.getInt16(headerOffsetSize);
        final ByteOrder byteOrder;
        if (byteOrderIdentifier == MOTOROLA_TIFF_MAGIC_NUMBER) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (byteOrderIdentifier == INTEL_TIFF_MAGIC_NUMBER) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unknown endianness = " + byteOrderIdentifier);
            }
            byteOrder = ByteOrder.BIG_ENDIAN;
        }
        segmentData.order(byteOrder);
        int firstIfdOffset = segmentData.getInt32(headerOffsetSize + 4) + headerOffsetSize;
        int tagCount = segmentData.getInt16(firstIfdOffset);
        int tagOffset, tagType, formatCode, componentCount;
        for (int i = 0; i < tagCount; i++) {
            tagOffset = calcTagOffset(firstIfdOffset, i);
            tagType = segmentData.getInt16(tagOffset);
            if (tagType != ORIENTATION_TAG_TYPE) {
                continue;
            }
            formatCode = segmentData.getInt16(tagOffset + 2);
            if (formatCode < 1 || formatCode > 12) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got invalid format code = " + formatCode);
                }
                continue;
            }
            componentCount = segmentData.getInt32(tagOffset + 4);
            if (componentCount < 0) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Negative tiff component count");
                }
                continue;
            }
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got tagIndex=" + i + " tagType=" + tagType + " formatCode=" + formatCode + " componentCount=" + componentCount);
            }
            final int byteCount = componentCount + BYTES_PER_FORMAT[formatCode];
            if (byteCount > 4) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got byte count > 4, not orientation, continuing, formatCode=" + formatCode);
                }
                continue;
            }
            final int tagValueOffset = tagOffset + 8;
            if (tagValueOffset < 0 || tagValueOffset > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal tagValueOffset=" + tagValueOffset + " tagType=" + tagType);
                }
                continue;
            }
            if (byteCount < 0 || tagValueOffset + byteCount > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal number of bytes for TI tag data tagType=" + tagType);
                }
                continue;
            }
            return segmentData.getInt16(tagValueOffset);
        }
        return -1;
    }

    /**
     * 计算标签在文件中的偏移位置
     */
    private static int calcTagOffset(int ifdOffset, int tagIndex) {
        return ifdOffset + 2 + 12 * tagIndex;
    }

    /**
     * 判断是不是支持的图片格式
     */
    private static boolean handles(int imageMagicNumber) {
        return (imageMagicNumber & EXIF_MAGIC_NUMBER) == EXIF_MAGIC_NUMBER || imageMagicNumber == MOTOROLA_TIFF_MAGIC_NUMBER || imageMagicNumber == INTEL_TIFF_MAGIC_NUMBER;
    }

    /**
     * 随机读取 EXIF 数据
     */
    private static class RandomAccessReader {
        private final ByteBuffer data;

        public RandomAccessReader(byte[] data, int length) {
            this.data = (ByteBuffer) ByteBuffer.wrap(data)
                    .order(ByteOrder.BIG_ENDIAN)
                    .limit(length);
        }

        public void order(ByteOrder byteOrder) {
            this.data.order(byteOrder);
        }

        public int length() {
            return data.remaining();
        }

        public int getInt32(int offset) {
            return data.getInt(offset);
        }

        public short getInt16(int offset) {
            return data.getShort(offset);
        }
    }

    /**
     * 读取接口
     */
    private interface Reader {
        int getUInt16() throws IOException;

        short getUInt8() throws IOException;

        long skip(long total) throws IOException;

        int read(byte[] buffer, int byteCount) throws IOException;
    }

    /**
     * 把旧图片的 EXIF 复制到新图片
     */
    private static class StreamReader implements Reader {
        private final InputStream is;

        public StreamReader(InputStream is) {
            this.is = is;
        }

        @Override
        public int getUInt16() throws IOException {
            return (is.read() << 8 & 0xFF00) | (is.read() & 0xFF);
        }

        @Override
        public short getUInt8() throws IOException {
            return (short) (is.read() & 0xFF);
        }

        @Override
        public long skip(long total) throws IOException {
            if (total < 0) {
                return 0;
            }
            long toSkip = total;
            while (toSkip > 0) {
                long skipped = is.skip(toSkip);
                if (skipped > 0) {
                    toSkip -= skipped;
                } else {
                    int testEofByte = is.read();
                    if (testEofByte == -1) {
                        break;
                    } else {
                        toSkip--;
                    }
                }
            }
            return total - toSkip;
        }

        @Override
        public int read(byte[] buffer, int byteCount) throws IOException {
            int toRead = byteCount;
            int read;
            while (toRead > 0 && ((read = is.read(buffer, byteCount - toRead, toRead)) != -1)) {
                toRead -= read;
            }
            return byteCount - toRead;
        }
    }

}