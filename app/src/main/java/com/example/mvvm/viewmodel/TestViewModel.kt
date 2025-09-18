package com.example.mvvm.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View.generateViewId
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.viewModelScope
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.async
import com.example.common.base.bridge.launch
import com.example.common.network.CommonApi
import com.example.common.network.repository.request
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.safeAs
import com.example.common.network.repository.withHandling
import com.example.common.utils.builder.shortToast
import com.example.common.utils.builder.suspendingSavePic
import com.example.common.utils.builder.suspendingSaveView
import com.example.common.utils.function.byServerUrl
import com.example.common.utils.function.decodeAsset
import com.example.common.utils.function.decodeDimensions
import com.example.common.utils.function.decodeResource
import com.example.common.utils.function.getBitmap
import com.example.common.utils.function.insertImageResolver
import com.example.common.utils.function.pt
import com.example.common.utils.function.safeRecycle
import com.example.framework.utils.function.view.applyConstraints
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.bottomToBottomOf
import com.example.framework.utils.function.view.bottomToTopOf
import com.example.framework.utils.function.view.centerVertically
import com.example.framework.utils.function.view.endToEndOf
import com.example.framework.utils.function.view.endToStartOf
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.safeRecycle
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.startToStartOf
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.topToBottomOf
import com.example.framework.utils.function.view.topToTopOf
import com.example.mvvm.R
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder

/**
 * 串行/并发是否需要dialog需要主动调取，单纯一次性发起不需要
 */
class TestViewModel : BaseViewModel() {
//    val token by lazy { MutableLiveData<String?>() }
    /**
     * 1、flow数据处理分StateFlow和SharedFlow，后者适合事件流或多值发射，网络请求用前者
     * 2、StateFlow需要声明默认值，并且和协程高度重合
     * MutableStateFlow 本身是热流（Hot Flow），其生命周期独立于订阅者。
     * 但实际使用中需通过 协程作用域（如 lifecycleScope 或 viewModelScope）启动流收集，以便在组件销毁时自动取消协程：
     * kotlin
     * // 在 Activity/Fragment 中使用 lifecycleScope
     * lifecycleScope.launch {
     *     viewModel.uiState.collect { state ->
     *         // 更新 UI
     *     }
     * } // 组件销毁时自动取消协程[2](@ref)
     */
//    val token by lazy { MutableStateFlow("") }

//    sealed class PageInfoResult {
//        data class DealDetailResult(val bean: DealBean?) : PageInfoResult()
//        data class PaymentListResult(val list: List<PaymentBean>?) : PageInfoResult()
//    }
//
//    fun getPageInfo(orderId: String?) {
//        launch {
//            flow {
//                //详情页数据
//                val dealDetailAsync = async {
//                    request({ OrderSubscribe.getDealDetailApi(reqBodyOf("orderId" to orderId)) }).apply {
//                        PageInfoResult.DealDetailResult(this)
//                    }
//                }
//                //底部筛选支付数据
//                val paymentListAsync = async {
//                    requestLayer({ OrderSubscribe.getPaymentListApi() }).data.apply {
//                        PageInfoResult.PaymentListResult(this)
//                    }
//                }
//                //并行发起
//                val asyncList = awaitAll(dealDetailAsync, paymentListAsync)
//                //发射数据
//                emit(asyncList)
//            }.withHandling({
//                //轮询失败直接报错遮罩，并且停止轮询倒计时
//                error()
//                reason.postValue(null)
//            }).collect {
//                reset(false)
//                var bean: DealBean? = null
//                var list: List<PaymentBean>? = null
//                it.forEach { result ->
//                    when (result) {
//                        is PageInfoResult.DealDetailResult -> bean = result.bean
//                        is PageInfoResult.PaymentListResult -> list = result.list
//                    }
//                }
//                //后端坑，没详情数据还返回成功，故而增加后续判断
//                if (null != bean && list.safeSize > 0) {
//                    pageInfo.postValue(DealBundle(bean, list))
//                }
//            }
//        }.manageJob()
//    }


    /**
     * 串行
     * task1/task2按照顺序依次执行
     */
    fun serialTask() {
        launch {
            flow {
                val task1 = request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })
                val task2 = request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })
                emit(Unit)
            }.withHandling(mView, {
                //每个请求如果失败了都会回调当前的err监听
            }).collect {

            }
//            /**
//             * 将一个监听回调的处理变为挂起函数的形式
//             * suspendCoroutine<T>---》T为返回的类型
//             * it.resume()---》回调的时候调取该方法，用于嵌套旧的一些api
//             */
//            suspendCoroutine {
////                it.resume()
//                //加try/catch接受
////                it.resumeWithException()
//            }
//            /**
//             * 区别于suspendCoroutine，代表此次转换的挂机方法是能够被cancel的
//             */
//            suspendCancellableCoroutine {
//
//            }
        }
        /**
         * 区别于常规协程，不需要类实现CoroutineScope，并且会阻塞当前线程
         * 在代码块中的逻辑执行完后才会执行接下来的代码
         */
        runBlocking { }
    }

    /**
     * 并发
     * task1/task2一起执行(req2会稍晚一点执行“被挂起的时间”)
     */
    fun concurrencyTask() {
        launch {
            flow {
                val task1 = async { request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })?.apply { } }
                val task2 = async { request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })?.apply { } }
                emit(awaitAll(task1, task2))
            }.withHandling(mView).collect {
                it.safeAs<Any>(0)
                it.safeAs<Any>(1)
            }
        }
    }

    private fun getUserDataAsync(): Deferred<Any?> {
        return async { request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) }) }
    }

    /**
     * 普通一次性
     */
    fun task() {
        flow<Unit> {
            //拿对象
            val bean = request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })
        }.withHandling().launchIn(viewModelScope)
    }

    fun getShare() {
        launch {
            flow {
                // 通过不同if判断生成对应的bitmap
                emit(requestAffair { suspendingKolShare() })
            }.withHandling(mView, {
                "分享失败".shortToast()
            }).collect { sourcePath ->
                mContext?.insertImageResolver(File(sourcePath.orEmpty()))
                "插入成功".shortToast()
            }
        }.manageJob()
    }

    /**
     * 生成一个配置好的图片,直接在原图上绘制文字/图案
     * 1.拿取UI提供的最高清的3倍图
     * 2.需求规定不同手机分享出去的大小都为335*300(像素)
     */
    suspend fun suspendingKolShare(): String? {
        return withContext(IO) {
            mContext?.let {
                // 获取分享背景图片
                val shareBg = it.decodeAsset("share/bg_kol_invite_info.webp", BitmapFactory.Options().apply {
                    // 强制用最高精度格式（支持透明+全色域，4字节/像素）
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    // 禁用系统自动缩放（避免加载时就压缩像素）
                    inScaled = false
                    // 禁用内存复用（避免复用低精度 Bitmap 的内存，导致细节丢失）
                    inMutable = false
                })?.toDrawable(it.resources)
                // 生成父布局
                val rootView = ConstraintLayout(it)
                rootView.size(335.pt, 300.pt)
                rootView.background = shareBg
                // 生成律师名称
                val tvNick = TextView(it)
                tvNick.id = generateViewId()
                tvNick.text = "王律师"
                tvNick.bold(true)
                tvNick.textSize(R.dimen.textSize16)
                tvNick.textColor(R.color.textWhite)
                rootView.addView(tvNick)
                rootView.applyConstraints {
                    val viewId = tvNick.id
                    startToStartOf(viewId)
                    topToTopOf(viewId)
                }
                tvNick.margin(start = 60.pt, top = 20.pt)
                // 生成律师下方斜线图片
                val ivLine = ImageView(it)
                ivLine.id = generateViewId()
                ivLine.size(60.pt, 3.pt)
                ivLine.background(R.mipmap.ic_kol_line)
                rootView.addView(ivLine)
                rootView.applyConstraints {
                    val viewId = ivLine.id
                    startToStartOf(viewId, tvNick.id)
                    topToBottomOf(viewId, tvNick.id)
                }
                ivLine.margin(top = 1.pt)
                // 生成二维码
                val content = "/app/sign-up?inviteCode=${URLEncoder.encode("10086", "UTF-8")}".byServerUrl
                val qrBit = QRCodeEncoder.syncEncodeQRCode(content, 400, Color.BLACK, Color.WHITE, mContext.decodeResource(R.mipmap.ic_qr_code))
                val ivQrCode = ImageView(it)
                ivQrCode.id = generateViewId()
                ivQrCode.size(60.pt, 60.pt)
                ivQrCode.setImageBitmap(qrBit)
                rootView.addView(ivQrCode)
                rootView.applyConstraints {
                    val viewId = ivQrCode.id
                    bottomToBottomOf(viewId)
                    endToEndOf(viewId)
                }
                ivQrCode.margin(end = 28.pt, bottom = 12.pt)
                // 开始生成bitmap
                val shareBit = suspendingSaveView(rootView, 335, 300, true)
//                shareBit = suspendingSaveView(rootView, 335.pt, 300.pt)
                // 将bitmap存至本地
                val filePath = suspendingSavePic(shareBit)
                // 回收所有引用的bitmap
                rootView.background.getBitmap()?.safeRecycle()
                ivQrCode.safeRecycle()
                shareBg?.bitmap?.safeRecycle()
                qrBit?.safeRecycle()
                shareBit.safeRecycle()
                // 返回本地地址
                filePath
            } ?: ""
        }
    }

    /**
     * 通过camera拍摄了一张图片,如果需要分享,需要对原图做一个修改
     */
    suspend fun suspendingCameraShare(sourcePath: String): String? {
        return withContext(IO) {
            mContext?.let {
                // 获取拍摄的照片
                val shareBg = BitmapFactory.decodeFile(sourcePath, BitmapFactory.Options().apply {
                    // 强制用最高精度格式（支持透明+全色域，4字节/像素）
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    // 禁用系统自动缩放（避免加载时就压缩像素）
                    inScaled = false
                    // 禁用内存复用（避免复用低精度 Bitmap 的内存，导致细节丢失）
                    inMutable = false
                })?.toDrawable(it.resources)
                // 获取照片的实际宽高
                val shotDimensions = sourcePath.decodeDimensions() ?: intArrayOf(0, 0)
                // 生成父布局
                val rootView = ConstraintLayout(it)
                rootView.size(shotDimensions[0], shotDimensions[1])
                rootView.background = shareBg
                // 生成底部经纬度布局
                val latLngView = ConstraintLayout(it)
                latLngView.id = generateViewId()
                latLngView.size(shotDimensions[0], 107.pt)
                latLngView.background(R.mipmap.bg_menu_shadow)
                latLngView.padding(start = 24.pt, end = 24.pt)
                rootView.addView(latLngView)
                rootView.applyConstraints {
                    val viewId = latLngView.id
                    bottomToBottomOf(viewId)
                }
                // 生成二维码
                val content = "/app/sign-up?inviteCode=${URLEncoder.encode("10086", "UTF-8")}".byServerUrl
                val qrBit = QRCodeEncoder.syncEncodeQRCode(content, 400, Color.BLACK, Color.WHITE, mContext.decodeResource(R.mipmap.ic_qr_code))
                val ivQrCode = ImageView(it)
                ivQrCode.id = generateViewId()
                ivQrCode.size(60.pt, 60.pt)
                ivQrCode.setImageBitmap(qrBit)
                latLngView.addView(ivQrCode)
                latLngView.applyConstraints {
                    val viewId = ivQrCode.id
                    centerVertically(viewId)
                    endToEndOf(viewId)
                }
                // 生成经纬度
                val tvLatLng = TextView(it)
                tvLatLng.id = generateViewId()
                tvLatLng.text = "经度：120.161893  纬度：30.28989"
                tvLatLng.setTextAppearance(it, R.style.TextShadow)
                tvLatLng.textSize(R.dimen.textSize10)
                tvLatLng.textColor(R.color.textWhite)
                latLngView.addView(tvLatLng)
                latLngView.applyConstraints {
                    val viewId = tvLatLng.id
                    centerVertically(viewId)
                    startToStartOf(viewId)
                    endToEndOf(viewId, ivQrCode.id)
                }
                tvLatLng.margin(end = 14.pt)
                // 生成日期
                val tvDate = TextView(it)
                tvDate.id = generateViewId()
                tvDate.text = "2021年6月20日 21:32:45"
                tvDate.setTextAppearance(it, R.style.TextShadow)
                tvDate.textSize(R.dimen.textSize10)
                tvDate.textColor(R.color.textWhite)
                latLngView.addView(tvDate)
                latLngView.applyConstraints {
                    val viewId = tvDate.id
                    bottomToTopOf(viewId, tvLatLng.id)
                    endToStartOf(viewId, ivQrCode.id)
                    startToStartOf(viewId)
                }
                tvDate.margin(bottom = 4.pt)
                // 生成地址
                val tvAddress = TextView(it)
                tvAddress.id = generateViewId()
                tvAddress.text = "浙江省杭州市余杭区"
                tvAddress.setTextAppearance(it, R.style.TextShadow)
                tvAddress.textSize(R.dimen.textSize10)
                tvAddress.textColor(R.color.textWhite)
                latLngView.addView(tvAddress)
                latLngView.applyConstraints {
                    val viewId = tvAddress.id
                    topToBottomOf(viewId, tvLatLng.id)
                    endToStartOf(viewId, ivQrCode.id)
                    startToStartOf(viewId)
                }
                tvAddress.margin(top = 4.pt)
                // 开始生成bitmap
                val shareBit = suspendingSaveView(rootView, shotDimensions[0], shotDimensions[1])
                // 将bitmap存至本地
                val filePath = suspendingSavePic(shareBit)
                // 回收所有引用的bitmap
                rootView.background.getBitmap()?.safeRecycle()
                ivQrCode.safeRecycle()
                shareBg?.bitmap?.safeRecycle()
                qrBit?.safeRecycle()
                shareBit.safeRecycle()
                // 返回本地地址
                filePath
            } ?: ""
        }
    }

}