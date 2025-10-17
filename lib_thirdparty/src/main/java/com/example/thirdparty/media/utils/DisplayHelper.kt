package com.example.thirdparty.media.utils

import android.app.Activity.RESULT_OK
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.ActivityResultRegistrar
import com.example.common.utils.function.isExists
import com.example.common.utils.function.pullUpOverlay
import com.example.common.utils.function.pullUpScreen
import com.example.common.utils.function.string
import com.example.common.widget.dialog.AndDialog
import com.example.framework.utils.function.isServiceRunning
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeSize
import com.example.thirdparty.R
import com.example.thirdparty.media.service.DisplayService
import com.example.thirdparty.media.service.ShotObserver

/**
 * @description 录屏工具类
 * @author yan
 */
class DisplayHelper(private val mActivity: FragmentActivity, registrar: ActivityResultRegistrar) : LifecycleEventObserver {
    private var lastRefreshTime = 0L //上一次的录制时间
    private var isRecording = false // 是否正在进行录制，便于区分截图捕获到的图片路径
    private var isZip = false // 当前录屏模式是否需要捕获图片打压缩包
    private var filePath: String? = null // 录制源文件路径
    private var listener: OnDisplayListener? = null // 录屏回调监听
    private val dialog by lazy { AndDialog(mActivity) } // 录屏弹框
    private val list by lazy { ArrayList<String>() } // 截图路径集合
    private val observer by lazy { ShotObserver(mActivity) } // 捕获录屏订阅

    /**
     * 处理录屏的回调
     */
    private val result = registrar.registerResult {
        if (it.resultCode == RESULT_OK) {
            mActivity.startService(DisplayService::class.java, Extra.RESULT_CODE to it.resultCode, Extra.BUNDLE_BEAN to it.data)
            mActivity.moveTaskToBack(true)
        } else {
            R.string.screenCancel.shortToast()
            isRecording = false
            listener?.onCancel()
        }
    }

    companion object {
        /**
         * 用于计算系统弹出弹框到正式开始录屏花费了多少时间（毫秒）
         */
        var waitingTime = 0L

        /**
         * 安全区间内的屏幕录制宽高
         */
        var previewWidth = screenWidth
        var previewHeight = screenHeight

        /**
         * 计算合理的比特率（避免过高或过低）
         *
         * 1. 基础公式：分辨率 × 帧率 × 系数 的行业逻辑
         * 视频的比特率（单位：bps）本质上是「单位时间内需要存储的视频数据量」，它与三个因素正相关：
         * 分辨率（宽 × 高）：画面像素越多，需要的数据量越大（例如 1080P 比 720P 需要更多数据）。
         * 帧率（fps）：每秒画面帧数越多，需要的数据量越大（30fps 比 15fps 需要翻倍数据）。
         * 压缩效率（系数）：视频编码（如 H.264）会通过压缩算法减少冗余数据，系数代表压缩后的「实际数据量占原始数据量的比例」。
         * 行业内对 H.264 编码的经验系数通常在 0.05~0.2 之间：
         * 系数越小（如 0.05）：压缩率越高，画质损失越大，但比特率低（适合低端设备）。
         * 系数越大（如 0.2）：压缩率越低，画质越好，但比特率高（可能超出设备编码能力）。
         * 公式中用 0.1 是取中间值，兼顾画质和兼容性。
         *
         * 2. 帧率固定为 30fps 的原因
         * 30fps 是 Android 设备最通用的稳定帧率：绝大多数手机、平板的屏幕刷新率在 60Hz 以下，30fps 的视频已经能满足「流畅观看」的需求。
         * 避免高帧率的兼容性问题：60fps 对设备编码器性能要求较高，部分中低端设备或模拟器可能不支持，容易导致prepare()失败。
         *
         * 3. 限制比特率范围（2~10Mbps）的必要性
         * 最低 2Mbps：低于这个值时，即使是 720P 分辨率也会出现明显的画质失真（如色块、模糊），影响录屏可用性。
         * 最高 10Mbps：超过这个值后，会带来两个问题：
         * 设备编码压力过大：多数 Android 设备的硬件编码器（尤其是中低端机型）无法稳定输出 10Mbps 以上的 H.264 视频，会导致编码失败（prepare()或start()崩溃）。
         * 文件体积激增：10 分钟的 10Mbps 视频约 750MB，而录屏场景通常需要长时间录制，过大的体积会导致存储不足或写入缓慢。
         *
         * 4. 适配 Android 设备的实际调整
         * 这个公式在实际测试中针对不同设备做了优化：
         * 对于低分辨率（如 720P 及以下）：计算出的比特率通常在 2~5Mbps，符合多数设备的编码能力。
         * 对于高分辨率（如 1080P）：计算出的比特率约 8~10Mbps，处于主流设备的支持上限（不会触发编码过载）。
         * 对于极端分辨率（如 2K/4K）：通过coerceIn限制在 10Mbps 以内，避免因分辨率过高导致比特率溢出。
         */
        @JvmStatic
        fun calculateBitRate(width: Int, height: Int): Int {
            // 降低基础系数至0.07（Android 15对高比特率更敏感）
            val baseBitRate = (width * height * 30 * 0.07).toInt()
            // 根据分辨率动态调整最高上限
            val maxBitRate = when {
                // 2K及以上分辨率（如2560x1440）限制更低
                width * height >= 2560 * 1440 -> 6 * 1024 * 1024  // 6Mbps
                // 1080P分辨率（1920x1080）
                width * height >= 1920 * 1080 -> 8 * 1024 * 1024  // 8Mbps
                // 720P及以下（1280x720）
                else -> 5 * 1024 * 1024  // 5Mbps
            }
            // 限制最低比特率（避免画质过差）
            val minBitRate = 1 * 1024 * 1024  // 1Mbps
            // 最终返回钳位后的值
            return baseBitRate.coerceIn(minBitRate, maxBitRate)
        }

        /**
         * 优化编码器支持性检查函数（更精准模拟实际配置）
         */
        @JvmStatic
        fun isEncoderSupported(width: Int, height: Int, bitRate: Int, frameRate: Int): Boolean {
            return try {
                // 创建与实际录制一致的格式（关键：必须包含颜色格式）
                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                    setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10) // 与实际录制保持一致
                    // 关键：录屏场景必须使用COLOR_FormatSurface
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                }
                // 查找支持该格式的编码器
                val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
                val codecName = codecList.findEncoderForFormat(format)
                codecName != null // 有可用编码器则返回true
            } catch (e: Exception) {
                false // 任何异常都视为不支持
            }
        }

        /**
         * 动态调整参数的逻辑（当检测到不支持时）
         */
        @JvmStatic
        fun getCompatibleParameters(originalWidth: Int, originalHeight: Int): Triple<Int, Int, Int> {
            // 尝试的参数组合（从高到低降级）
            val paramCandidates = listOf(
                // 分辨率宽高比保持不变，按比例缩小
                Pair(originalWidth, originalHeight),
                Pair((originalWidth * 0.8).toInt(), (originalHeight * 0.8).toInt()),
                Pair((originalWidth * 0.6).toInt(), (originalHeight * 0.6).toInt()),
                Pair((originalWidth * 0.5).toInt(), (originalHeight * 0.5).toInt())
            ).map { (w, h) ->
                // 确保宽高为偶数（编码器硬性要求）
                val validW = if (w % 2 == 0) w else w - 1
                val validH = if (h % 2 == 0) h else h - 1
                Triple(validW, validH, 30) // 帧率固定30fps
            }
            // 遍历候选参数，返回第一个支持的组合
            for ((w, h, fps) in paramCandidates) {
                if (w <= 0 || h <= 0) continue
                val bitRate = calculateBitRate(w, h)
                if (isEncoderSupported(w, h, bitRate, fps)) {
                    return Triple(w, h, bitRate)
                }
            }
            // 最终 fallback：使用720P基础配置（兼容性最高）
            return Triple(1280, 720, calculateBitRate(1280, 720))
        }
    }

    init {
        //加入页面生命周期管控
        mActivity.lifecycle.addObserver(this)
        //获取录屏屏幕宽高，高版本进行修正->页面是锁死竖屏的，故而校验只需一次
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var destroy = false
            if (mActivity.isFinishing.orFalse) destroy = true
            if (mActivity.isDestroyed.orFalse) destroy = true
            if (mActivity.windowManager == null) destroy = true
            if (mActivity.window?.decorView == null) destroy = true
            if (mActivity.window?.decorView?.parent == null) destroy = true
            if (!destroy) {
                val decorView = mActivity.window.decorView
                decorView.post {
                    val displayCutout = decorView.rootWindowInsets.displayCutout
                    val rectLists = displayCutout?.boundingRects
                    if (null != rectLists && rectLists.size > 0) {
                        previewWidth = screenWidth - displayCutout.safeInsetLeft - displayCutout.safeInsetRight
                        previewHeight = screenHeight - displayCutout.safeInsetTop - displayCutout.safeInsetBottom
                    }
                }
            }
        }
        //只要在录屏中，截一张图就copy一张到目标目录，但是需要及时清空
        observer.setOnShotListener {
            if (isZip) {
                it ?: return@setOnShotListener
                if (isRecording) {
                    if (!it.isExists()) return@setOnShotListener
                    list.add(it)
                }
            }
        }
        //开始进行录屏
        DisplayService.setOnDisplayListener(object : DisplayService.OnDisplayListener {
            override fun onStart(folderPath: String?) {
                R.string.screenStart.shortToast()
                waitingTime = currentTimeNano - lastRefreshTime
                lastRefreshTime = currentTimeNano
                filePath = folderPath
                list.clear()
                isRecording = true
                listener?.onStart(folderPath)
            }

            override fun onShutter() {
                //此处是开始处理值，需要有加载动画之类的做拦截
                listener?.onShutter()
            }

            override fun onStop() {
                //此处已经处理好了值，直接回调即可
                if (isZip) {
                    //说明未截图
                    if (list.safeSize == 0) {
                        listener?.onStop(listOf(filePath), false)
                    } else {
                        //拿到保存的截屏文件夹地址下的所有文件目录，并将录屏源文件路径也添加进其中
                        list.add(filePath.orEmpty())
                        //放入页面协程操作
//                        // 拿到源文件路径
//                        val folderPath = list.safeLast()
//                        // 压缩包输出路径（会以录屏文件的命名方式来命名）
//                        val zipPath = File(folderPath).name.replace("mp4", "zip")
//                        //开始压包
//                        builder.zipJob(list, zipPath, { mView?.showDialog() }, {
//                            mView?.hideDialog()
//                            folderPath.deleteFile()
//                        })
                        listener?.onStop(listOf(filePath), true)
                    }
                } else {
                    listener?.onStop(listOf(filePath), false)
                }
            }

            override fun onError(e: Exception?) {
                //有转圈动画记得关闭
                R.string.screenError.shortToast()
                isRecording = false
                listener?.onCancel()
            }
        })
    }

    /**
     * 开始录屏
     * 尝试唤起手机录屏弹窗，会在onActivityResult中回调结果
     */
    fun startScreen() {
        if (!Settings.canDrawOverlays(mActivity)) {
            dialog
                .setParams(message = string(R.string.overlayGranted))
                .setDialogListener({
                    mActivity.pullUpOverlay()
                })
                .show()
        } else {
            result.pullUpScreen(mActivity)
        }
    }

    /**
     * 结束录屏
     */
    fun stopScreen() {
        isRecording = false
        mActivity.stopService(DisplayService::class.java)
    }

    /**
     * 设置压缩模式(再次开启压缩的时候开启)
     */
    fun setZipMode(isZip: Boolean) {
        this.isZip = isZip
    }

    /**
     * 录屏监听
     */
    fun setOnDisplayListener(listener: OnDisplayListener) {
        this.listener = listener
    }

    /**
     * 回调监听
     */
    interface OnDisplayListener {
        /**
         * 正式开始录屏
         */
        fun onStart(filePath: String?)

        /**
         * 取消/报错
         */
        fun onCancel()

        /**
         * 停止录屏到完全结束有一个"存储时间"
         */
        fun onShutter()

        /**
         * 录屏停止
         */
        fun onStop(list: List<String?>, isZip: Boolean)

    }

    /**
     * 生命周期监听，不管录屏是否停止，页面销毁时都调取一次停止防止内存泄漏
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                if (mActivity.isServiceRunning(DisplayService::class.java)) {
                    DisplayService.isDestroy = true
                    stopScreen()
                }
                result.unregister()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}
//class DisplayHelper(private val mActivity: FragmentActivity, registrar: ActivityResultRegistrar, private val mView: BaseView, private val isZip: Boolean = false) : LifecycleEventObserver {
//    private var isRecording = false//是否正在进行录制，便于区分截图捕获到的图片路径
//    private var lastRefreshTime = 0L//上一次的刷新时间
//    private var filePath: String? = null
//    private var listener: OnDisplayListener? = null
//    private val list by lazy { ArrayList<String>() }
//    private val dialog by lazy { AndDialog(mActivity) }
//    private val observer by lazy { ShotObserver(mActivity) }
//
//    /**
//     * 处理录屏的回调
//     */
//    private val result = registrar.registerResult {
//        if (it.resultCode == RESULT_OK) {
//            mActivity.startService(DisplayService::class.java, Extra.RESULT_CODE to it.resultCode, Extra.BUNDLE_BEAN to it.data)
//            mActivity.moveTaskToBack(true)
//        } else {
//            R.string.screenCancel.shortToast()
//            isRecording = false
//            listener?.onCancel()
//        }
//    }
//
//    companion object {
//        /**
//         * 用于计算系统弹出弹框到正式开始录屏花费了多少时间（毫秒）
//         */
//        var waitingTime = 0L
//
//        /**
//         * 安全区间内的屏幕录制宽高
//         */
//        var previewWidth = screenWidth
//        var previewHeight = screenHeight
//
//        /**
//         * 计算合理的比特率（避免过高或过低）
//         *
//         * 1. 基础公式：分辨率 × 帧率 × 系数 的行业逻辑
//         * 视频的比特率（单位：bps）本质上是「单位时间内需要存储的视频数据量」，它与三个因素正相关：
//         * 分辨率（宽 × 高）：画面像素越多，需要的数据量越大（例如 1080P 比 720P 需要更多数据）。
//         * 帧率（fps）：每秒画面帧数越多，需要的数据量越大（30fps 比 15fps 需要翻倍数据）。
//         * 压缩效率（系数）：视频编码（如 H.264）会通过压缩算法减少冗余数据，系数代表压缩后的「实际数据量占原始数据量的比例」。
//         * 行业内对 H.264 编码的经验系数通常在 0.05~0.2 之间：
//         * 系数越小（如 0.05）：压缩率越高，画质损失越大，但比特率低（适合低端设备）。
//         * 系数越大（如 0.2）：压缩率越低，画质越好，但比特率高（可能超出设备编码能力）。
//         * 公式中用 0.1 是取中间值，兼顾画质和兼容性。
//         *
//         * 2. 帧率固定为 30fps 的原因
//         * 30fps 是 Android 设备最通用的稳定帧率：绝大多数手机、平板的屏幕刷新率在 60Hz 以下，30fps 的视频已经能满足「流畅观看」的需求。
//         * 避免高帧率的兼容性问题：60fps 对设备编码器性能要求较高，部分中低端设备或模拟器可能不支持，容易导致prepare()失败。
//         *
//         * 3. 限制比特率范围（2~10Mbps）的必要性
//         * 最低 2Mbps：低于这个值时，即使是 720P 分辨率也会出现明显的画质失真（如色块、模糊），影响录屏可用性。
//         * 最高 10Mbps：超过这个值后，会带来两个问题：
//         * 设备编码压力过大：多数 Android 设备的硬件编码器（尤其是中低端机型）无法稳定输出 10Mbps 以上的 H.264 视频，会导致编码失败（prepare()或start()崩溃）。
//         * 文件体积激增：10 分钟的 10Mbps 视频约 750MB，而录屏场景通常需要长时间录制，过大的体积会导致存储不足或写入缓慢。
//         *
//         * 4. 适配 Android 设备的实际调整
//         * 这个公式在实际测试中针对不同设备做了优化：
//         * 对于低分辨率（如 720P 及以下）：计算出的比特率通常在 2~5Mbps，符合多数设备的编码能力。
//         * 对于高分辨率（如 1080P）：计算出的比特率约 8~10Mbps，处于主流设备的支持上限（不会触发编码过载）。
//         * 对于极端分辨率（如 2K/4K）：通过coerceIn限制在 10Mbps 以内，避免因分辨率过高导致比特率溢出。
//         */
//        @JvmStatic
//        fun calculateBitRate(width: Int, height: Int): Int {
////        // 公式：分辨率 * 帧率 * 0.1（经验值，平衡画质和兼容性）
////        val baseBitRate = (width * height * 30 * 0.1).toInt()
////        // 限制范围：最低 2Mbps，最高 10Mbps（避免极端值）
////        return baseBitRate.coerceIn(2 * 1024 * 1024, 10 * 1024 * 1024)
//            // 1. 降低基础系数至0.07（Android 15对高比特率更敏感）
//            val baseBitRate = (width * height * 30 * 0.07).toInt()
//            // 2. 根据分辨率动态调整最高上限
//            val maxBitRate = when {
//                // 2K及以上分辨率（如2560x1440）限制更低
//                width * height >= 2560 * 1440 -> 6 * 1024 * 1024  // 6Mbps
//                // 1080P分辨率（1920x1080）
//                width * height >= 1920 * 1080 -> 8 * 1024 * 1024  // 8Mbps
//                // 720P及以下（1280x720）
//                else -> 5 * 1024 * 1024  // 5Mbps
//            }
//            // 3. 限制最低比特率（避免画质过差）
//            val minBitRate = 1 * 1024 * 1024  // 1Mbps
//            // 4. 最终返回钳位后的值
//            return baseBitRate.coerceIn(minBitRate, maxBitRate)
//        }
//
//        /**
//         * 优化编码器支持性检查函数（更精准模拟实际配置）
//         */
//        @JvmStatic
//        fun isEncoderSupported(width: Int, height: Int, bitRate: Int, frameRate: Int): Boolean {
//            return try {
//                // 创建与实际录制一致的格式（关键：必须包含颜色格式）
//                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
//                    setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
//                    setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
//                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10) // 与实际录制保持一致
//                    // 关键：录屏场景必须使用COLOR_FormatSurface
//                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
//                }
//                // 查找支持该格式的编码器
//                val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
//                val codecName = codecList.findEncoderForFormat(format)
//                codecName != null // 有可用编码器则返回true
//            } catch (e: Exception) {
//                false // 任何异常都视为不支持
//            }
//        }
//
//        /**
//         * 动态调整参数的逻辑（当检测到不支持时）
//         */
//        @JvmStatic
//        fun getCompatibleParameters(originalWidth: Int, originalHeight: Int): Triple<Int, Int, Int> {
//            // 尝试的参数组合（从高到低降级）
//            val paramCandidates = listOf(
//                // 分辨率宽高比保持不变，按比例缩小
//                Pair(originalWidth, originalHeight),
//                Pair((originalWidth * 0.8).toInt(), (originalHeight * 0.8).toInt()),
//                Pair((originalWidth * 0.6).toInt(), (originalHeight * 0.6).toInt()),
//                Pair((originalWidth * 0.5).toInt(), (originalHeight * 0.5).toInt())
//            ).map { (w, h) ->
//                // 确保宽高为偶数（编码器硬性要求）
//                val validW = if (w % 2 == 0) w else w - 1
//                val validH = if (h % 2 == 0) h else h - 1
//                Triple(validW, validH, 30) // 帧率固定30fps
//            }
//            // 遍历候选参数，返回第一个支持的组合
//            for ((w, h, fps) in paramCandidates) {
//                if (w <= 0 || h <= 0) continue
//                val bitRate = calculateBitRate(w, h)
//                if (isEncoderSupported(w, h, bitRate, fps)) {
//                    return Triple(w, h, bitRate)
//                }
//            }
//            // 最终 fallback：使用720P基础配置（兼容性最高）
//            return Triple(1280, 720, calculateBitRate(1280, 720))
//        }
//    }
//
//    init {
//        //加入页面生命周期管控
//        mActivity.lifecycle.addObserver(this)
//        //获取录屏屏幕宽高，高版本进行修正->页面是锁死竖屏的，故而校验只需一次
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            var destroy = false
//            if (mActivity.isFinishing.orFalse) destroy = true
//            if (mActivity.isDestroyed.orFalse) destroy = true
//            if (mActivity.windowManager == null) destroy = true
//            if (mActivity.window?.decorView == null) destroy = true
//            if (mActivity.window?.decorView?.parent == null) destroy = true
//            if (!destroy) {
//                val decorView = mActivity.window.decorView
//                decorView.post {
//                    val displayCutout = decorView.rootWindowInsets.displayCutout
//                    val rectLists = displayCutout?.boundingRects
//                    if (null != rectLists && rectLists.size > 0) {
//                        previewWidth = screenWidth - displayCutout.safeInsetLeft - displayCutout.safeInsetRight
//                        previewHeight = screenHeight - displayCutout.safeInsetTop - displayCutout.safeInsetBottom
//                    }
//                }
//            }
//        }
//        //只要在录屏中，截一张图就copy一张到目标目录，但是需要及时清空
//        observer.setOnShotListener {
//            if (isZip) {
//                it ?: return@setOnShotListener
//                if (isRecording) {
//                    if (!it.isExists()) return@setOnShotListener
//                    list.add(it)
//                }
//            }
//        }
//        //开始进行录屏
//        DisplayService.setOnDisplayListener(object : DisplayService.OnDisplayListener {
//            override fun onStart(folderPath: String?) {
//                R.string.screenStart.shortToast()
//                waitingTime = currentTimeNano - lastRefreshTime
//                lastRefreshTime = currentTimeNano
//                filePath = folderPath
//                list.clear()
//                isRecording = true
//                listener?.onStart(folderPath)
//            }
//
//            override fun onShutter() {
//                //此处是开始处理值，需要有加载动画之类的做拦截
//                mView.showDialog()
//            }
//
//            override fun onStop() {
//                mView.hideDialog()
//                //此处已经处理好了值，直接回调即可
//                if (isZip) {
//                    //说明未截图
//                    if (list.safeSize == 0) {
//                        listener?.onComplete(filePath, false)
//                    } else {
////                        //拿到保存的截屏文件夹地址下的所有文件目录，并将录屏源文件路径也添加进其中
////                        list.add(folderPath)
////                        //压缩包输出路径（会以录屏文件的命名方式来命名）
////                        val zipPath = File(folderPath).name.replace("mp4", "zip")
////                        //开始压包
////                        builder.zipJob(list, zipPath, { mView?.showDialog() }, {
////                            mView?.hideDialog()
////                            folderPath.deleteFile()
////                        })
////                        listener?.onResult(zipPath, true)
//                        listener?.onComplete(filePath, true)
//                    }
//                } else {
//                    listener?.onComplete(filePath, false)
//                }
//            }
//
//            override fun onError(e: Exception?) {
//                R.string.screenError.shortToast()
//                mView.hideDialog()
//                isRecording = false
//                listener?.onCancel()
//            }
//        })
//    }
//
//    /**
//     * 开始录屏
//     * 尝试唤起手机录屏弹窗，会在onActivityResult中回调结果
//     */
//    fun startScreen() {
//        if (!Settings.canDrawOverlays(mActivity)) {
//            dialog
//                .setParams(message = string(R.string.overlayGranted))
//                .setDialogListener({
//                    mActivity.pullUpOverlay()
//                })
//                .show()
//        } else {
//            result.pullUpScreen(mActivity)
//        }
////        if (mActivity.pullUpOverlay()) {
////            result.pullUpScreen(mActivity)
////        } else {
////            R.string.overlayGranted.shortToast()
////        }
//    }
//
//    /**
//     * 结束录屏
//     */
//    fun stopScreen() {
//        isRecording = false
//        mActivity.stopService(DisplayService::class.java)
//    }
//
//    /**
//     * 录屏监听
//     */
//    fun setOnDisplayListener(listener: OnDisplayListener) {
//        this.listener = listener
//    }
//
//    /**
//     * 回调监听
//     */
//    interface OnDisplayListener {
//        /**
//         * 正式开始录屏
//         */
//        fun onStart(filePath: String?)
//
//        /**
//         * 取消/报错
//         */
//        fun onCancel()
//
//        /**
//         * isZip->true是zip文件夹，可能包含录制时的截图
//         */
//        fun onComplete(filePath: String?, isZip: Boolean)
//    }
//
//    /**
//     * 生命周期监听，不管录屏是否停止，页面销毁时都调取一次停止防止内存泄漏
//     */
//    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//        when (event) {
//            Lifecycle.Event.ON_DESTROY -> {
//                if (mActivity.isServiceRunning(DisplayService::class.java)) {
//                    DisplayService.isDestroy = true
//                    stopScreen()
//                }
//                result.unregister()
//                mActivity.lifecycle.removeObserver(this)
//            }
//            else -> {}
//        }
//    }
//
//}