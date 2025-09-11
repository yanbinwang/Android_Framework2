package com.example.common.network.repository

import com.example.common.R
import com.example.common.base.bridge.BaseView
import com.example.common.network.repository.ApiCode.FAILURE
import com.example.common.network.repository.ApiCode.SUCCESS
import com.example.common.network.repository.ApiCode.TOKEN_EXPIRED
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.resString
import com.example.common.utils.helper.AccountHelper
import com.example.common.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.cancellation.CancellationException

//------------------------------------针对协程返回的参数(协程只有成功和失败,确保方法在flow内使用，并且实现withHandling扩展)------------------------------------
/**
 * flow处理网络请求的时候的方法外层套一个这个方法，会处理对象并拿取body
 */
suspend fun <T> request(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    err: (ResponseWrapper) -> Unit = {}
): T? {
    return requestLayer(coroutineScope, err).data.let {
        if (it is EmptyBean) {
            EmptyBean()
        } else {
            it
        } as? T
    }
}

suspend fun <T> request(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (T?) -> Unit,
    err: (ResponseWrapper) -> Unit
) {
    val data = request(coroutineScope, err)
    resp.invoke(data)
}

/**
 * 1.列表的网络请求在flow内使用时，如果请求失败，我需要在上抛异常前把此次页数减1，并且对recyclerview做一些操作，故而需要有个err回调
 * 2.整体的err可以被catch到，通过withHandling扩展函数来实现调用
 */
suspend fun <T> requestLayer(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    err: (ResponseWrapper) -> Unit = {}
): ApiResponse<T> {
    return try {
        withContext(IO) { coroutineScope() }
    } catch (e: Throwable) {
        // 忽略 CancellationException，直接重新抛出
        if (e is CancellationException) {
            throw e
        }
        val wrapper = wrapper(e)
        err(wrapper)
        throw wrapper
    }.also { response ->
        if (!response.successful() || response.tokenExpired()) {
            val wrapper = ResponseWrapper(response.code, response.msg)
            err(wrapper)
            throw wrapper
        }
    }
}

/**
 * 如果外层嵌套了flow，且flow也写了catch，会导致内部的catch不自信，外层抢先一步获取
 * 直接在flow的emit这catch，抓取到此次异常
 * try {
 *     emit(requestLayer(coroutineScope, err))
 *  } catch (e: Throwable) {
 *    // 可以在这里做额外处理
 *     throw e
 * }
 */
suspend fun <T> requestLayer(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (ApiResponse<T>) -> Unit,
    err: (ResponseWrapper) -> Unit
) {
    val response = requestLayer(coroutineScope, err)
    resp.invoke(response)
}

suspend fun <T> requestAffair(
    coroutineScope: suspend CoroutineScope.() -> T
): T {
    return try {
        withContext(IO) { coroutineScope() }
    } catch (e: Throwable) {
        throw e
    }
}

/**
 * flow如果不调用collect是不会执行数据流通的
 * 1）如果 Flow 没有更多元素可发射（例如 flowOf(1, 2, 3) 发射完最后一个元素后自动结束）。
 * 或者在自定义 flow 构建器中，**主动调用 cancel()** 终止流
 * 2）如果在 Flow 执行过程中（包括上游操作符或 emit 本身）抛出未捕获的异常，Flow 会被取消
 * 3）如果 Flow 所在的协程被取消（例如 Activity/Fragment 被销毁），Flow 会自动终止，launch的job直接cancel，flow就终止了
 * 4) onCompletion操作符的lambda参数cause: Throwable?用于表示流完成时的状态。当流因异常终止时，cause会被设置为对应的Throwable对象；
 * 若流正常完成（无异常），则cause为null，多写几次onCompletion后添加的先执行，需要注意
 */
fun <T> Flow<T>.withHandling(
    view: BaseView? = null,
    err: (ResponseWrapper) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false,
    isShowDialog: Boolean = true
): Flow<T> {
    return withHandling(err, {
        if (isShowDialog) view?.hideDialog()
        end()
    }, isShowToast).onStart {
        if (isShowDialog) view?.showDialog()
    }
}

fun <T> Flow<T>.withHandling(
    err: (ResponseWrapper) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false,
): Flow<T> {
    return flowOn(Main.immediate).catch { exception ->
        // 忽略 CancellationException，不做处理
        if (exception is CancellationException) {
            throw exception
        }
        val wrapper = wrapper(exception)
        if (isShowToast) wrapper.errMessage?.responseToast()
        err(wrapper)
    }.onCompletion { cause ->
        if (cause !is CancellationException) {
            end()
        }
    }
}

private fun wrapper(exception: Throwable): ResponseWrapper {
    val wrapper: ResponseWrapper = when (exception) {
        is ResponseWrapper -> exception
        else -> ResponseWrapper(FAILURE, "", RuntimeException("Unhandled error: ${exception::class.java.simpleName} - ${exception.message}", exception))
    }
    return wrapper
}

/**
 * 请求转换
 * map扩展，如果只需传入map则使用
 * hashMapOf("" to "")不需要写此扩展
 */
fun <K, V> HashMap<K, V>?.requestBody() =
    this?.toJson().orEmpty().toRequestBody("application/json; charset=utf-8".toMediaType())

fun reqBodyOf(vararg pairs: Pair<String, Any?>): RequestBody {
    val map = hashMapOf<String, Any>()
    pairs.forEach {
        it.second?.let { v ->
            map[it.first] = v
        }
    }
    return map.requestBody()
}

/**
 * 取得async异步协程集合后，拿取对应的值强转
 * reified:保留类型参数 T 的具体类型信息
 */
inline fun <reified T> List<Any?>?.safeAs(position: Int): T? {
    if (this == null || position < 0 || position >= size) return null
    val value = get(position)
    return if (value is T) value else null
}

inline fun <reified T> Any?.safeAs(): T? {
    if (this == null) return null
    return if (this is T) this else null
}

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseToast() =
    (if (!NetWorkUtil.isNetworkAvailable()) resString(R.string.responseNetError) else {
        if (isNullOrEmpty()) resString(R.string.responseError) else this
    }).shortToast()

/**
 * 判断此次请求是否成功
 */
fun <T> ApiResponse<T>?.successful(): Boolean {
    if (this == null) return false
    return SUCCESS == code
}

/**
 * 判断此次请求是否token过期
 */
fun <T> ApiResponse<T>?.tokenExpired(): Boolean {
    if (this == null) return false
    if (TOKEN_EXPIRED == code) {
        AccountHelper.signOut()
        return true
    }
    return false
}